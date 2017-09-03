package me.andreww7985.owopserver.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;

import me.andreww7985.owopserver.command.ACommand;
import me.andreww7985.owopserver.command.AdminCommand;
import me.andreww7985.owopserver.command.HelpCommand;
import me.andreww7985.owopserver.command.InfoCommand;
import me.andreww7985.owopserver.command.KickCommand;
import me.andreww7985.owopserver.command.ShutdownCommand;
import me.andreww7985.owopserver.command.TeleportCommand;
import me.andreww7985.owopserver.helper.ChatHelper;
import me.andreww7985.owopserver.helper.ConfigHelper;
import me.andreww7985.owopserver.network.LoginInfo.Rank;
import me.andreww7985.owopserver.network.NetworkState;
import me.andreww7985.owopserver.network.states.VerificationState;

import me.nagalun.async.ITaskScheduler;
import me.nagalun.jwebsockets.HttpRequest;
import me.nagalun.jwebsockets.PreparedMessage;
import me.nagalun.jwebsockets.WebSocket;
import me.nagalun.jwebsockets.WebSocketServer;

public class OWOPServer extends WebSocketServer {
	private static OWOPServer instance;
	private static final LogManager log = LogManager.getInstance();
	private static final ConfigHelper conf;
	private static final int port;
	
	private int totalChunksLoaded, totalOnline;
	private final HashSet<WebSocket> connections = new HashSet<>();
	private final CommandManager commandManager = new CommandManager();
	private final WorldManager worldManager = new WorldManager();
	private final String adminPassword;
	private String MOTD = "Pixel perfect!";
	private int AFKTimerID;

	private boolean isClosed = false; /* ...or closing */
	
	static {
		try {
			conf = new ConfigHelper("server.conf", true, "OWOP Server configuration file");
			conf.setDefaultProperty("server-port", Integer.toString(1234));
			conf.setDefaultProperty("captcha-secret", "dummy-secret-token");
			conf.setDefaultProperty("captcha-enable", "false");
			conf.setDefaultProperty("verify-origin", "false");
			conf.setDefaultProperty("origin-url", "ourworldofpixels.com");
			if (!conf.getProperties().containsKey("admin-password")) {
				final String weakPassword = Long.toHexString((new SecureRandom()).nextLong());
				conf.setDefaultProperty("admin-password", weakPassword);
				System.out.println("WARNING: Generated a weak admin password, you should change it ASAP.");
			}
			conf.writeProps();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		port = Integer.parseInt(getProperty("server-port"));
	}

	public OWOPServer() throws IOException {
		/*
		 * NOTE: Maximum message size set to 128 (should be changed for the captcha
		 * token...)
		 */
		super(port, Arrays.asList(StandardSocketOptions.TCP_NODELAY, StandardSocketOptions.SO_REUSEADDR), 128);
		
		NetworkState.init(this);

		OWOPServer.instance = this;
		adminPassword = getProperty("admin-password");

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

		log.info("Admin password is '" + adminPassword + "'");
		log.info("Starting server on port " + port);
	}
	
	@Override
	public void onStart() {
		final ITaskScheduler ts = getTaskScheduler();

		worldManager.onStart(ts);
		//AFKTimerID = ts.setInterval(() -> checkAFK(), 60000);
	}

	@Override
	public void onOpen(final WebSocket ws) {
		connections.add(ws);
		log.info("Connected new socket from " + ws.getRemoteSocketAddress());
		ws.userData = NetworkState.getInitialConnectionState(ws);
		totalOnline++;
	}

	@Override
	public void onClose(final WebSocket ws, final int code, final String reason, final boolean remote) {
		connections.remove(ws);
		final NetworkState ns = (NetworkState) ws.userData;
		ns.socketDisconnected();
		final SocketAddress addr = ws.getRemoteSocketAddress();
		log.info("Socket from " + addr + " disconnected");
		totalOnline--;
		/*final SocketAddress addr = ws.getRemoteSocketAddress();
		final Player player = players.get(addr);
		if (player != null) {
			if (remote) {
				log.info("Player " + player + " disconnected");
			}
			final World world = player.getWorld();
			world.playerLeft(player);
			players.remove(addr);
			totalOnline--;
			if (world.getOnline() < 1) {
				worldManager.unloadWorld(world);
			}
		} else {
			log.info("Socket from " + addr + " disconnected");
		}*/
	}

	@Override
	public void onMessage(final WebSocket ws, final ByteBuffer message) {
		final NetworkState ns = (NetworkState) ws.userData;
		ns.processMessage(message);
		/*final SocketAddress addr = ws.getRemoteSocketAddress();
		Player player = players.get(addr);
		message.order(ByteOrder.LITTLE_ENDIAN);
		if (player == null) {*/
			/*if (!isWorldNameValid(message)) {
				log.warn("Join verification failed for socket from " + addr);
				ws.close();
				return;
			}

			final String worldName = StandardCharsets.US_ASCII.decode(message).toString();
			final World world = worldManager.getWorld(worldName);

			player = new Player(world.getNextID(), world, ws);
			players.put(addr, player);
			world.playerJoined(player);
			log.info("Joined player " + player + " from " + addr);*/
		/*} else {
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
				log.warn("Unknown packet from " + player + " with " + message.capacity() + " bytes!");
				player.kick();
				break;
			}
		}*/
	}

	@Override
	public void onMessage(final WebSocket ws, String message) {
		final NetworkState ns = (NetworkState) ws.userData;
		ns.processMessage(message);
		/*final Player player = players.get(ws.getRemoteSocketAddress());*/
		/* length + 1 for verification byte */
		/*if (!message.isEmpty() && player != null && message.length() <= 80 + 1 && message.length() > 1
				&& message.codePointAt(message.length() - 1) == 10) {
			message = message.trim();

			final int id = player.getID();
			if (!player.isAdmin()) {
				message = ChatHelper.format(message);
			}
			if (!message.isEmpty()) {
				if (message.startsWith("/")) {
					final String[] arguments = message.substring(1).toLowerCase().split(" ");
					log.command(player + " issued " + Arrays.toString(arguments));
					commandManager.executeCommand(arguments[0], Arrays.copyOfRange(arguments, 1, arguments.length),
							player);
				} else {
					player.chatMessage((player.isAdmin() ? ChatHelper.ORANGE : "") + id + ": " + message);
				}
			}
		}*/
	}

	public void broadcast(final String text, final boolean adminOnly) {
		final PreparedMessage data = prepareMessage(text);
		connections.forEach((ws) -> {
			final NetworkState ns = (NetworkState) ws.userData;
			if (!adminOnly || (ns != null && ns.getRank() == Rank.ADMIN)) {
				ws.sendPrepared(data);
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
			log.info("Shutting down server...");
			broadcast(ChatHelper.RED + "Shutting down server...", false);

			final ITaskScheduler ts = getTaskScheduler();
			ts.clear(AFKTimerID);
			worldManager.onStop(ts);
			conf.writeProps();
			stop();
		} catch (final Exception e) {
			log.err("Something happened while shutting down!");
			log.exception(e);
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

	public CommandManager getCommandManager() {
		return commandManager;
	}
	
	public WorldManager getWorldManager() {
		return worldManager;
	}
	
	public String getMOTD() {
		return MOTD;
	}
	
	public static String getProperty(final String key) {
		return conf.getProperties().getProperty(key);
	}
	
	public static boolean getBoolProperty(final String key) {
		return conf.getProperties().getProperty(key, "").equals("true");
	}
	
	public static OWOPServer getInstance() {
		return OWOPServer.instance;
	}

	/*public void checkAFK() {
		final long time = System.currentTimeMillis();
		for (final Player player : players.values()) {
			if (time - player.getLastMoveTime() > Player.AFKMIN * 60000 && !player.isAdmin()) {
				log.warn("Player " + player + " is inactive too long");
				player.sendMessage(ChatHelper.RED + "Kicked for inactivity!");
				player.kick();
			}
		}
	}*/

	@Override
	public void onStop() {

	}

	@Override
	public boolean onHttpRequest(final SocketChannel sock, final HttpRequest req) {
		return VerificationState.verifyHeaders(req);
	}
}
