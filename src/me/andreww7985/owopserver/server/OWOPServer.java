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
import me.andreww7985.owopserver.command.ShutdownCommand;
import me.andreww7985.owopserver.command.TeleportCommand;
import me.andreww7985.owopserver.command.TimingsCommand;
import me.andreww7985.owopserver.game.Player;
import me.andreww7985.owopserver.game.World;
import me.andreww7985.owopserver.helper.ChatHelper;
import me.andreww7985.owopserver.timings.TimingsRecord;

public class OWOPServer extends WebSocketServer {
	private static OWOPServer instance;
	private int totalChunksLoaded, totalOnline;
	private final ConcurrentHashMap<InetSocketAddress, Player> players = new ConcurrentHashMap<InetSocketAddress, Player>();
	private final ConcurrentHashMap<String, World> worlds = new ConcurrentHashMap<String, World>();
	private final Timer updatesTimer = new Timer(), AFKTimer = new Timer();
	private final CommandManager commandManager;
	private final TimingsManager timingsManager;
	private final LogManager logManager;
	private final String adminPassword;
	private boolean working;

	public OWOPServer(final String adminPassword, final int port) throws Exception {
		super(new InetSocketAddress(port));
		commandManager = new CommandManager();
		timingsManager = new TimingsManager();
		logManager = new LogManager();
		this.adminPassword = adminPassword;
		OWOPServer.instance = this;

		commandManager.registerCommand(new HelpCommand());
		commandManager.registerCommand(new TeleportCommand());
		commandManager.registerCommand(new AdminCommand());
		commandManager.registerCommand(new InfoCommand());
		commandManager.registerCommand(new ACommand());
		commandManager.registerCommand(new KickCommand());
		commandManager.registerCommand(new TimingsCommand());
		commandManager.registerCommand(new ShutdownCommand());

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				OWOPServer.getInstance().shutdown();
			}
		});

		working = true;

		logManager.info("Admin password is '" + adminPassword + "'");
		logManager.info("Starting server on port " + this.getAddress().getPort());
	}

	public static OWOPServer getInstance() {
		return OWOPServer.instance;
	}

	public LogManager getLogManager() {
		return logManager;
	}

	public CommandManager getCommandManager() {
		return commandManager;
	}

	@Override
	public void onOpen(final WebSocket ws, final ClientHandshake handshake) {
		logManager.info("Connected new socket from " + ws.getRemoteSocketAddress());
	}

	@Override
	public void onClose(final WebSocket ws, final int code, final String reason, final boolean remote) {
		final InetSocketAddress addr = ws.getRemoteSocketAddress();
		final Player player = players.get(addr);
		if (player != null) {
			if (remote) {
				logManager.info("Player " + player + " disconnected");
			}
			final World world = player.getWorld();
			world.playerLeft(player);
			players.remove(addr);
			totalOnline--;
			if (world.getOnline() < 1) {
				world.save();
				worlds.remove(world.getName());
				logManager.info("Unloaded world '" + world + "'");
			}
		} else {
			logManager.info("Socket from " + addr + " disconnected");
		}
	}

	@Override
	public void onMessage(final WebSocket ws, final ByteBuffer message) {
		final TimingsRecord tr = TimingsRecord.start("onPacket");
		final InetSocketAddress addr = ws.getRemoteSocketAddress();
		Player player = players.get(addr);
		message.order(ByteOrder.LITTLE_ENDIAN);
		if (player == null) {
			if (message.capacity() < 3 || message.getShort(message.capacity() - 2) != 1337) {
				logManager.warn("Join verification failed for socket from " + addr);
				ws.close();
				return;
			}
			final byte[] bytes = message.array();
			String worldname = "";

			for (int i = 0; i < bytes.length - 2; i++) {
				worldname += (char) bytes[i];
			}

			// TODO: Make world name check

			World world = worlds.get(worldname);
			if (world == null) {
				world = new World(worldname);
				worlds.put(worldname, world);
				logManager.info("Loaded world '" + world + "'");
			}

			player = new Player(world.getNextID(), world, ws);
			players.put(addr, player);
			world.playerJoined(player);
			player.sendMessage(ChatHelper.LIME + "Joined world '" + world + "'. Your ID: " + player.getID());
			logManager.info("Joined player " + player + " from " + addr);
			totalOnline++;
		} else {
			switch (message.array().length) {
			case 8: {
				final int x = message.getInt(0), y = message.getInt(4);
				player.getChunk(x, y);
				break;
			}
			case 10: {
				if (!player.isAdmin()) {
					player.kick();
				} else {
					final int x = message.getInt(0), y = message.getInt(4);
					final short rgb565 = message.getShort(8);
					player.getWorld().clearChunk(x, y, rgb565);
				}
				break;
			}
			case 11: {
				final int x = message.getInt(0), y = message.getInt(4);
				final short rgb565 = message.getShort(8);
				player.putPixel(x, y, rgb565);
				break;
			}
			case 12: {
				final int x = message.getInt(0), y = message.getInt(4), tool = message.get(11);
				final short rgb565 = message.getShort(8);
				player.update(x, y, (byte) tool, rgb565);
				break;
			}
			default:
				logManager.warn("Unknown packet from " + player + " with " + message.capacity() + " bytes!");
				player.kick();
				break;
			}
		}
		timingsManager.add(tr);
	}

	@Override
	public void onError(final WebSocket conn, final Exception ex) {
		logManager.exception(ex);
	}

	@Override
	public void onStart() {
		updatesTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				final TimingsRecord tr = TimingsRecord.start("sendUpdates");
				for (final Object world : worlds.values().toArray()) {
					((World) world).updateCache();
					((World) world).sendUpdates();
				}
				timingsManager.add(tr);
			}
		}, 0, 50);
		AFKTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				final TimingsRecord tr = TimingsRecord.start("AFKCheck");
				players.forEach((k, player) -> {
					if (System.currentTimeMillis() - player.getLastMoveTime() > Player.AFKMIN * 60000) {
						logManager.warn("Player " + player + " is inactive too long");
						player.sendMessage(ChatHelper.RED + "Kicked for inactivity!");
						player.kick();
					}
				});
				timingsManager.add(tr);
			}
		}, 0, 60000);
	}

	@Override
	public void onMessage(final WebSocket ws, String message) {
		final TimingsRecord tr = TimingsRecord.start("onChat");
		final Player player = players.get(ws.getRemoteSocketAddress());
		if (!message.isEmpty() && player != null && message.length() <= 80 && message.length() > 1
				&& message.codePointAt(message.length() - 1) == 10) {
			message = message.trim();

			final int id = player.getID();
			if (!player.isAdmin()) {
				message = ChatHelper.format(message);
			}
			if (!message.isEmpty()) {
				if (message.startsWith("/")) {
					final String[] arguments = message.substring(1).toLowerCase().split(" ");
					logManager.command(player + " issued " + Arrays.toString(arguments));
					commandManager.executeCommand(arguments[0], Arrays.copyOfRange(arguments, 1, arguments.length),
							player);
				} else {
					player.chatMessage((player.isAdmin() ? ChatHelper.ORANGE : "") + id + ": " + message);
				}
			}
		}
		timingsManager.add(tr);
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

	public void shutdown() {
		try {
			if (!working) {
				return;
			}
			working = false;
			logManager.info("Shutting down server...");
			broadcast(ChatHelper.RED + "Shutting down server...", false);
			worlds.forEach((k, world) -> {
				world.save();
				logManager.info("Unloaded world '" + world + "'");
			});
			updatesTimer.cancel();
			AFKTimer.cancel();
			stop();
		} catch (final Exception e) {
			working = false;
			logManager.err("Fatal error! Can't shutdown! Exiting using System.exit(1)");
			logManager.exception(e);
			System.exit(1);
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

	public TimingsManager getTimingsManager() {
		return timingsManager;
	}
}
