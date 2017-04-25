package me.andreww7985.owopserver;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class Server extends WebSocketServer {
	private static ConcurrentHashMap<InetSocketAddress, Player> players = new ConcurrentHashMap<InetSocketAddress, Player>();
	private static ConcurrentHashMap<String, World> worlds = new ConcurrentHashMap<String, World>();
	private static String admPass;

	public Server(final String admPass, final int port) throws Exception {
		super(new InetSocketAddress(port));
		Server.admPass = admPass;
		Logger.info("Admin password is '" + Server.admPass + "'");
		Logger.info("Starting server on port " + this.getAddress().getPort());
	}

	@Override
	public void onOpen(final WebSocket ws, final ClientHandshake handshake) {
		final InetSocketAddress addr = ws.getRemoteSocketAddress();
		Logger.info("Connected new player from " + addr);
		/*
		 * if (players.containsKey(addr)) { /* Should never happen Logger.err(
		 * "Connected player from used IP!"); return; }
		 */
	}

	@Override
	public void onClose(final WebSocket ws, final int code, final String reason, final boolean remote) {
		final InetSocketAddress addr = ws.getRemoteSocketAddress();
		final Player player = players.get(addr);
		Logger.info("Disconnected player from " + addr);
		if (player != null) {
			final World world = player.getWorld();
			world.playerLeft(player);
			players.remove(addr);
			if (world.getOnline() == 0) {
				final String worldname = world.getName();
				world.save();
				worlds.remove(worldname);
				Logger.info("Unloaded world '" + worldname + "'");
				// TODO: Fix memory leaks
			}
		}
	}

	@Override
	public void onMessage(final WebSocket ws, final ByteBuffer message) {
		final InetSocketAddress addr = ws.getRemoteSocketAddress();
		Player player = players.get(addr);
		message.order(ByteOrder.LITTLE_ENDIAN);
		if (player == null) {
			final byte[] bytes = message.array();
			String worldname = "";

			for (int i = 0; i < bytes.length - 2; i++) {
				worldname += (char) bytes[i];
			}

			// TODO: Make world name and last 2 bytes check

			World world = worlds.get(worldname);
			if (world == null) {
				/* Create the world if it doesn't exist */
				world = new World(worldname);
				worlds.put(worldname, world);
				Logger.info("Loaded world '" + worldname + "'");
			}

			player = new Player(world.getNextID(), world, ws);
			players.put(addr, player);
			world.playerJoined(player);
			player.send("<font style=\"color:blue;\">Hi, you are on BETA server!");
			player.send("<font style=\"color:green;\">If you found bugs, please let us know!");
			player.send("<img src=\"http://tny.im/8Vr\">");
			Logger.info("Joined player from " + addr + " to world '" + worldname + "'");
		} else {
			switch (message.array().length) {
			// TODO: Implement more tools
			case 8: {
				final int x = message.getInt(0), y = message.getInt(4);
				player.getChunk(x, y);
				break;
			}
			case 11: {
				final int x = message.getInt(0), y = message.getInt(4);
				final int rgb = message.getInt(7) >> 8 & 0xFFFFFF;
				player.putPixel(x, y, rgb);
				break;
			}
			case 12: {
				final int x = message.getInt(0), y = message.getInt(4), tool = message.get(8);
				final int rgb = message.getInt(8) >> 8 & 0xFFFFFF;
				player.update(x, y, (byte) tool, rgb);
				break;
			}
			default:
				/*
				 * players.forEach((k, v) -> v .send(
				 * "<font style=\"color:red;\">SERVER me.andreww7985.owopserver.UnsupportedToolException"
				 * )); Just ignore
				 */
				break;
			}
		}
	}

	@Override
	public void onError(final WebSocket conn, final Exception ex) {
		players.forEach((k, v) -> v
				.send("<font style=\"color:red;\">SERVER " + ex.getCause().getMessage() + " " + ex.toString()));
	}

	@Override
	public void onStart() {
		final Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				players.forEach((k, v) -> v.getWorld().sendUpdates(v));
				worlds.forEach((k, v) -> v.clearUpdates());
			}
		}, 0, 50);
	}

	@Override
	public void onMessage(final WebSocket ws, final String message) {
		// TODO: Implement commands, timeout
		final Player player = players.get(ws.getRemoteSocketAddress());
		final int size = message.length();
		if (player != null && size <= 80 && message.codePointAt(size - 1) == 10) {
			final int id = player.getID();
			final String escapedmsg = message.trim().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
					.replace("\"", "&quot;").replace("'", "&#x27;").replace("/", "&#x2F;");
			if (!escapedmsg.isEmpty()) {
				Logger.chat(message);
				players.forEach((k, v) -> v.send(id + ": " + escapedmsg));
			}
		}
	}

	public static World getWorld(final String world) {
		return worlds.get(world);
	}
}
