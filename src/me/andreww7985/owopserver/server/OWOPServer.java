package me.andreww7985.owopserver.server;

import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import me.andreww7985.owopserver.command.ACommand;
import me.andreww7985.owopserver.command.AdminCommand;
import me.andreww7985.owopserver.command.HelpCommand;
import me.andreww7985.owopserver.command.InfoCommand;
import me.andreww7985.owopserver.command.KickCommand;
import me.andreww7985.owopserver.command.ShutdownCommand;
import me.andreww7985.owopserver.command.TeleportCommand;
import me.andreww7985.owopserver.game.Player;
import me.andreww7985.owopserver.game.World;
import me.andreww7985.owopserver.helper.ChatHelper;
import me.andreww7985.owopserver.network.states.VerificationState;

import me.nagalun.async.ITaskScheduler;
import me.nagalun.jwebsockets.HttpRequest;
import me.nagalun.jwebsockets.PreparedMessage;
import me.nagalun.jwebsockets.WebSocket;
import me.nagalun.jwebsockets.WebSocketServer;

public class OWOPServer extends WebSocketServer {
	private static OWOPServer instance;
	private int totalChunksLoaded, totalOnline;
	private final ConcurrentHashMap<SocketAddress, Player> players = new ConcurrentHashMap<>();
	private final HashMap<String, World> worlds = new HashMap<>();
	private int updatesTimerID, AFKTimerID;
	private final CommandManager commandManager;
	private final LogManager logManager;
	private final String adminPassword;

	private boolean isClosed = false; /* ...or closing */

	public OWOPServer(final String adminPassword, final int port) throws Exception {
		/* NOTE: Maximum message size set to 128 */
		super(port, Arrays.asList(StandardSocketOptions.TCP_NODELAY, StandardSocketOptions.SO_REUSEADDR), 128);
		commandManager = new CommandManager();
		logManager = new LogManager();
		
		this.adminPassword = adminPassword;
		OWOPServer.instance = this;

		commandManager.registerCommand(new HelpCommand());
		commandManager.registerCommand(new TeleportCommand());
		commandManager.registerCommand(new AdminCommand());
		commandManager.registerCommand(new InfoCommand());
		commandManager.registerCommand(new ACommand());
		commandManager.registerCommand(new KickCommand());
		commandManager.registerCommand(new ShutdownCommand());

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				final Thread exitThread = Thread.currentThread();
				final OWOPServer server = OWOPServer.getInstance();
				final ITaskScheduler ts = server.getTaskScheduler();
				/*
				 * If the task scheduler isn't running then it means something else called
				 * server.shutdown(), like the /shutdown command.
				 **/
				if (ts.isRunning()) {
					ts.idleCallback(() -> {
						server.shutdown();
						exitThread.interrupt();
					});

					try {
						Thread.sleep(15000);
						/* Shutdown took too long */
					} catch (final InterruptedException e) {
						/* Exited normally */
					}
				}
			}
		});

		logManager.info("Admin password is '" + adminPassword + "'");
		logManager.info("Starting server on port " + port);
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
	public void onOpen(final WebSocket ws) {
		logManager.info("Connected new socket from " + ws.getRemoteSocketAddress());
	}

	@Override
	public void onClose(final WebSocket ws, final int code, final String reason, final boolean remote) {
		final SocketAddress addr = ws.getRemoteSocketAddress();
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
				unloadWorld(world);
			}
		} else {
			logManager.info("Socket from " + addr + " disconnected");
		}
	}

	@Override
	public void onMessage(final WebSocket ws, final ByteBuffer message) {
		final SocketAddress addr = ws.getRemoteSocketAddress();
		Player player = players.get(addr);
		message.order(ByteOrder.LITTLE_ENDIAN);
		if (player == null) {
			if (!isWorldNameValid(message)) {
				logManager.warn("Join verification failed for socket from " + addr);
				ws.close();
				return;
			}

			final String worldName = StandardCharsets.US_ASCII.decode(message).toString();
			final World world = getWorld(worldName);

			player = new Player(world.getNextID(), world, ws);
			players.put(addr, player);
			world.playerJoined(player);
			logManager.info("Joined player " + player + " from " + addr);
			totalOnline++;
		} else {
			switch (message.capacity()) {
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
	}

	@Override
	public void onStart() {
		final ITaskScheduler ts = getTaskScheduler();

		updatesTimerID = ts.setInterval(() -> sendUpdates(), 50);
		AFKTimerID = ts.setInterval(() -> checkAFK(), 60000);
	}

	@Override
	public void onMessage(final WebSocket ws, String message) {
		final Player player = players.get(ws.getRemoteSocketAddress());
		/* length + 1 for verification byte */
		if (!message.isEmpty() && player != null && message.length() <= 80 + 1 && message.length() > 1
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
	}

	public void broadcast(final String text, final boolean adminOnly) {
		final PreparedMessage data = prepareMessage(text);
		players.forEach((k, player) -> {
			if (!adminOnly || player.isAdmin()) {
				player.send(data);
			}
		});
		data.finalizeMessage();
	}

	public void shutdown() {
		if (isClosed) {
			return;
		}

		try {
			isClosed = true;
			logManager.info("Shutting down server...");
			broadcast(ChatHelper.RED + "Shutting down server...", false);
			for (final World world : worlds.values()) {
				unloadWorld(world);
			}
			final ITaskScheduler ts = getTaskScheduler();
			ts.clear(AFKTimerID);
			ts.clear(updatesTimerID);
			stop();
		} catch (final Exception e) {
			logManager.err("Something happened while shutting down!");
			logManager.exception(e);
		}
	}

	public World getWorld(final String worldName) {
		World world = worlds.get(worldName);
		if (world == null) {
			world = new World(worldName);
			worlds.put(worldName, world);
			logManager.info("Loaded world " + world);
		}
		return world;
	}

	public void unloadWorld(final String worldName) {
		final World world = worlds.get(worldName);
		if (world != null) {
			world.save();
			worlds.remove(worldName);
			logManager.info("Unloaded world " + world);
		}
	}

	public void unloadWorld(final World world) {
		if (world != null) {
			world.save();
			worlds.remove(world.getName());
			logManager.info("Unloaded world " + world);
		}
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

	public static boolean isWorldNameValid(final ByteBuffer nameBytes) {
		/* Validate world name, allowed chars are a..z, 0..9, '_' and '.' */
		final int size = nameBytes.capacity();

		if (size < 3 || size - 2 > 24 || nameBytes.getShort(size - 2) != 1337) {
			return false;
		}

		nameBytes.limit(size - 2);
		for (int i = 0; i < nameBytes.limit(); i++) {
			final byte b = nameBytes.get(i);
			if (!((b > 96 && b < 123) || (b > 47 && b < 58) || b == 95 || b == 46)) {
				return false;
			}
		}
		return true;
	}

	public void sendUpdates() {
		for (final World world : worlds.values()) {
			world.sendUpdates();
		}
	}

	public void checkAFK() {
		final long time = System.currentTimeMillis();
		for (final Player player : players.values()) {
			if (time - player.getLastMoveTime() > Player.AFKMIN * 60000 && !player.isAdmin()) {
				logManager.warn("Player " + player + " is inactive too long");
				player.sendMessage(ChatHelper.RED + "Kicked for inactivity!");
				player.kick();
			}
		}
	}

	@Override
	public void onStop() {

	}

	@Override
	public boolean onHttpRequest(final SocketChannel sock, final HttpRequest req) {
		return VerificationState.verifyHeaders(req);
	}
}
