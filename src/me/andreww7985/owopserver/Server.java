package me.andreww7985.owopserver;

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
			// TODO: Enable world unloading when saving is done
			/*
			 * if (world.getOnline() == 0) { final String worldname =
			 * world.getName(); world.save(); worlds.remove(worldname);
			 * Logger.info("Unloaded world '" + worldname + "'"); // TODO: Fix
			 * memory leaks }
			 */
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
			player.send(
					ChatHelper.YELLOW + "Hi, you are on " + ChatHelper.BLUE + "BETA" + ChatHelper.YELLOW + " server!");
			player.send(ChatHelper.YELLOW + "If you found bugs, please let us know!");
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
				Logger.warn("Unknown packet from " + player.getID() + " with " + message.array().length + " bytes!");
				break;
			}
		}
	}

	@Override
	public void onError(final WebSocket conn, final Exception ex) {
		Logger.err("Exception " + ex.getMessage() + " " + ex.getStackTrace()[ex.getStackTrace().length - 1].toString());
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
	public void onMessage(final WebSocket ws, String message) {
		// TODO: Implement timeout
		final Player player = players.get(ws.getRemoteSocketAddress());
		final String trimmed = message.trim();
		final int size = trimmed.length();
		if (player != null && size <= 80 && size > 1 && message.codePointAt(message.length() - 1) == 10) {
			final int id = player.getID();
			if (!player.isAdmin()) {
				message = trimmed.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
						.replace("\"", "&quot;").replace("'", "&#x27;").replace("/", "&#x2F;");
			}
			if (!message.isEmpty()) {
				if (trimmed.startsWith("/")) {
					final String[] parameters = trimmed.substring(1).toLowerCase().split(" ");
					Logger.command(player.getID() + " issued " + Arrays.toString(parameters));
					// TODO: More commands
					if (parameters[0].equals("admin")) {
						if (parameters.length > 1) {
							if (parameters[1].equals(admPass)) {
								player.send(ChatHelper.LIME + "Admin mode enabled!");
								Logger.warn(player.getID() + " is now admin");
								player.setAdmin(true);
							} else {
								// TODO: Kick player
							}
						} else {
							player.send(ChatHelper.RED + "Usage: /admin <password>");
						}
					} else {
						player.send(ChatHelper.RED + "Unknown command!");
					}
				} else {
					Logger.chat(message);
					broadcast((player.isAdmin() ? ChatHelper.ORANGE : "") + id + ": " + message, false);
				}
			}
		}
	}

	public static void broadcast(final String text, final boolean adminOnly) {
		if (adminOnly) {
			players.forEach((k, v) -> {
				if (v.isAdmin()) {
					v.send(text);
				}
			});
		} else {
			players.forEach((k, v) -> v.send(text));
		}
	}

	public static World getWorld(final String world) {
		return worlds.get(world);
	}
}
