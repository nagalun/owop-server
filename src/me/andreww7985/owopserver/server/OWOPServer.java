package me.andreww7985.owopserver.server;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import me.andreww7985.owopserver.command.ACommand;
import me.andreww7985.owopserver.command.AdminCommand;
import me.andreww7985.owopserver.command.HelpCommand;
import me.andreww7985.owopserver.command.InfoCommand;
import me.andreww7985.owopserver.command.KickCommand;
import me.andreww7985.owopserver.command.TeleportCommand;
import me.andreww7985.owopserver.command.TimingsCommand;
import me.andreww7985.owopserver.helper.ChatHelper;
import me.andreww7985.owopserver.helper.TimingsHelper;

// TODO: Make plugin API and loader

public class OWOPServer extends WebSocketServer {
	private static OWOPServer instance;
	private int totalChunksLoaded, totalOnline;
	private final ConcurrentHashMap<InetSocketAddress, Player> players = new ConcurrentHashMap<InetSocketAddress, Player>();
	private final ConcurrentHashMap<String, World> worlds = new ConcurrentHashMap<String, World>();
	private final String adminPassword;
	private final CommandManager commandManager;
	private final Logger logger;

	public OWOPServer(final String adminPassword, final int port) throws Exception {
		super(new InetSocketAddress(port));
		commandManager = new CommandManager();
		logger = new Logger();
		this.adminPassword = adminPassword;
		OWOPServer.instance = this;
		logger.info("Admin password is '" + adminPassword + "'");
		logger.info("Starting server on port " + this.getAddress().getPort());

		commandManager.registerCommand(new HelpCommand());
		commandManager.registerCommand(new TeleportCommand());
		commandManager.registerCommand(new AdminCommand());
		commandManager.registerCommand(new InfoCommand());
		commandManager.registerCommand(new ACommand());
		commandManager.registerCommand(new KickCommand());
		commandManager.registerCommand(new TimingsCommand());
	}

	public static OWOPServer getInstance() {
		return OWOPServer.instance;
	}

	public Logger getLogger() {
		return logger;
	}

	public CommandManager getCommandManager() {
		return commandManager;
	}

	@Override
	public void onOpen(final WebSocket ws, final ClientHandshake handshake) {
		final InetSocketAddress addr = ws.getRemoteSocketAddress();
		logger.info("Connected new player from " + addr);
	}

	@Override
	public void onClose(final WebSocket ws, final int code, final String reason, final boolean remote) {
		final InetSocketAddress addr = ws.getRemoteSocketAddress();
		final Player player = players.get(addr);
		logger.info("Disconnected player from " + addr);
		if (player != null) {
			final World world = player.getWorld();
			world.playerLeft(player);
			players.remove(addr);
			totalOnline--;
			if (world.getOnline() == 0) {
				final String worldname = world.getName();
				world.save();
				worlds.remove(worldname);
				logger.info("Unloaded world '" + worldname + "'");
			}
		}
	}

	@Override
	public void onMessage(final WebSocket ws, final ByteBuffer message) {
		TimingsHelper.start("onPacket");
		final InetSocketAddress addr = ws.getRemoteSocketAddress();
		Player player = players.get(addr);
		message.order(ByteOrder.LITTLE_ENDIAN);
		if (player == null) {
			final byte[] bytes = message.array();
			if (message.getShort(bytes.length - 2) != 1337) {
				logger.warn("Join verification failed for: " + addr);
				ws.close();
				return;
			}
			String worldname = "";

			for (int i = 0; i < bytes.length - 2; i++) {
				worldname += (char) bytes[i];
			}

			// TODO: Make world name check

			World world = worlds.get(worldname);
			if (world == null) {
				world = new World(worldname);
				worlds.put(worldname, world);
				logger.info("Loaded world '" + worldname + "'");
			}

			player = new Player(world.getNextID(), world, ws);
			players.put(addr, player);
			world.playerJoined(player);
			player.sendMessage(ChatHelper.LIME + "Joined world '" + worldname + "'. Your ID: " + player.getID());
			logger.info("Joined player from " + addr + " to world '" + worldname + "' with ID " + player.getID());
			totalOnline++;
		} else {
			switch (message.array().length) {
			case 8: {
				final int x = message.getInt(0), y = message.getInt(4);
				player.getChunk(x, y);
				break;
			}
			case 9: {
				if (!player.isAdmin()) {
					player.kick();
				} else {
					final int x = message.getInt(0), y = message.getInt(4);
					player.getWorld().clearChunk(x, y);
				}
				break;
			}
			case 11: {
				final int x = message.getInt(0), y = message.getInt(4);
				final int rgb = message.getInt(7) >> 8 & 0xFFFFFF;
				player.putPixel(x, y, rgb);
				break;
			}
			case 12: {
				final int x = message.getInt(0), y = message.getInt(4), tool = message.get(11);
				final int rgb = message.getInt(8) & 0xFFFFFF;
				player.update(x, y, (byte) tool, rgb);
				break;
			}
			default:
				logger.warn("Unknown packet from " + player.getID() + " with " + message.array().length + " bytes!");
				break;
			}
		}
		TimingsHelper.stop("onPacket");
	}

	@Override
	public void onError(final WebSocket conn, final Exception ex) {
		logger.exception(ex);
	}

	@Override
	public void onStart() {
		final Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				TimingsHelper.start("sendingUpdates");
				worlds.forEach((k, world) -> world.updateCache());
				players.forEach((k, player) -> player.getWorld().sendUpdates(player));
				worlds.forEach((k, world) -> world.clearUpdates());
				TimingsHelper.stop("sendingUpdates");
			}
		}, 0, 50);
	}

	@Override
	public void onMessage(final WebSocket ws, String message) {
		TimingsHelper.start("onChat");
		// TODO: Implement timeout
		final Player player = players.get(ws.getRemoteSocketAddress());
		final String trimmed = message.trim();
		final int size = trimmed.length();
		if (player != null && size <= 80 && size > 1 && message.codePointAt(message.length() - 1) == 10) {
			final int id = player.getID();
			if (!player.isAdmin()) {
				message = ChatHelper.format(trimmed);
			}
			if (!message.isEmpty()) {
				if (trimmed.startsWith("/")) {
					final String[] parameters = trimmed.substring(1).toLowerCase().split(" ");
					logger.command(player.getID() + " issued " + Arrays.toString(parameters));
					commandManager.executeCommand(parameters[0], Arrays.copyOfRange(parameters, 1, parameters.length),
							player);
				} else {
					logger.chat(message);
					broadcast((player.isAdmin() ? ChatHelper.ORANGE : "") + id + ": " + message, false);
				}
			}
		}
		TimingsHelper.stop("onChat");
	}

	public void broadcast(final String text, final boolean adminOnly) {
		if (adminOnly) {
			players.forEach((k, player) -> {
				if (player.isAdmin()) {
					player.sendMessage(text);
				}
			});
		} else {
			players.forEach((k, player) -> player.sendMessage(text));
		}
	}

	public World getWorld(final String world) {
		return worlds.get(world);
	}

	public void chunksLoaded(final int num) {
		totalChunksLoaded += num;
	}

	public void chunksUnloaded(final int num) {
		totalChunksLoaded -= num;
	}

	public int getTotalChunksLoaded() {
		return totalChunksLoaded;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public int getTotalOnline() {
		return totalOnline;
	}

	public Player getPlayer(final int id) {
		for (final Object player : players.values().toArray()) {
			if (((Player) player).getID() == id) {
				return (Player) player;
			}
		}
		return null;
	}
}
