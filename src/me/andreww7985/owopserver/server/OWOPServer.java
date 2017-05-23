package me.andreww7985.owopserver.server;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

// TODO: Make plugin API and loader

public class OWOPServer extends WebSocketServer {
	private static OWOPServer instance;

	private final ConcurrentHashMap<InetSocketAddress, Player> players = new ConcurrentHashMap<InetSocketAddress, Player>();
	private final ConcurrentHashMap<String, World> worlds = new ConcurrentHashMap<String, World>();
	private final String admPass;
	private int totalChunksLoaded, totalOnline;
	private final EventManager eventManager;
	private final Logger logger;

	public OWOPServer(final String admPass, final int port) throws Exception {
		super(new InetSocketAddress(port));
		this.admPass = admPass;
		eventManager = new EventManager();
		logger = new Logger();
		OWOPServer.instance = this;
		logger.info("Admin password is '" + admPass + "'");
		logger.info("Starting server on port " + this.getAddress().getPort());
	}

	public static OWOPServer getInstance() {
		return OWOPServer.instance;
	}

	public Logger getLogger() {
		return logger;
	}

	public EventManager getEventManager() {
		return eventManager;
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
			player.send(ChatHelper.LIME + "Joined world '" + worldname + "'. Your ID: " + player.getID());
			logger.info("Joined player from " + addr + " to world '" + worldname + "' with ID " + player.getID());
			totalOnline++;
		} else {
			switch (message.array().length) {
			// TODO: Implement more tools
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
				players.forEach((k, player) -> player.getWorld().sendUpdates(player));
				worlds.forEach((k, world) -> world.clearUpdates());
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
					logger.command(player.getID() + " issued " + Arrays.toString(parameters));
					// TODO: More commands
					if (parameters[0].equals("admin")) {
						if (parameters.length > 1) {
							if (parameters[1].equals(admPass)) {
								player.send(
										ChatHelper.LIME + "Admin mode enabled!  Type '/help' for a list of commands.");
								logger.warn(player.getID() + " is now admin");
								player.setAdmin(true);
							} else {
								logger.warn(player.getID() + " used wrong password. Disconnecting...");
								player.kick();
							}
						} else {
							player.send(ChatHelper.RED + "Usage: /admin &ltpassword&gt");
						}
					} else if (parameters[0].equals("kick") && player.isAdmin()) {
						if (parameters.length > 1) {
							final int kickId = Integer.parseInt(parameters[1]);
							final Iterator<Entry<InetSocketAddress, Player>> iter = players.entrySet().iterator();
							boolean done = false;
							while (iter.hasNext()) {
								final Player curr = iter.next().getValue();
								if (curr.getID() == kickId) {
									curr.kick();
									done = true;
									player.send(ChatHelper.LIME + "Kicked " + kickId + "!");
									break;
								}
							}
							if (!done) {
								player.send(ChatHelper.RED + "Can't find player with ID " + kickId + "!");
							}
						} else {
							player.send(ChatHelper.RED + "Usage: /kick &ltID&gt");
						}
					} else if (parameters[0].equals("info") && player.isAdmin()) {
						player.send(ChatHelper.LIME + "Total online: " + totalOnline + ". Total chunks loaded: "
								+ totalChunksLoaded);
					} else if (parameters[0].equals("a") && player.isAdmin()) {
						if (parameters.length < 2) {
							player.send(ChatHelper.RED + "Usage: /a &lttext&gt");
						} else {
							String text = ChatHelper.ORANGE + "[A] " + player.getID() + ": ";
							for (int i = 1; i < parameters.length; i++) {
								text += parameters[i] + " ";
							}
							broadcast(text, true);
						}
					} else if (parameters[0].equals("tp") && player.isAdmin()) {
						if (parameters.length < 2) {
							player.send(ChatHelper.RED + "Usage: /tp &ltID&gt OR /tp &ltX&gt &ltY&gt");
						} else if (parameters.length == 2) {
							final int tpId = Integer.parseInt(parameters[1]);
							final Iterator<Entry<InetSocketAddress, Player>> iter = players.entrySet().iterator();
							boolean done = false;
							while (iter.hasNext()) {
								final Player curr = iter.next().getValue();
								if (curr.getID() == tpId) {
									player.teleport(curr.getX() >> 4, curr.getY() >> 4);
									done = true;
									player.send(ChatHelper.LIME + "Teleported to player with ID " + tpId + "!");
									break;
								}
							}
							if (!done) {
								player.send(ChatHelper.RED + "Can't find player with ID " + tpId + "!");
							}
						} else if (parameters.length > 2) {
							player.teleport(Integer.parseInt(parameters[1]), Integer.parseInt(parameters[2]));
						}
					} else if (parameters[0].equals("help") && player.isAdmin()) {
						player.send(ChatHelper.LIME + "/help - show help");
						player.send(ChatHelper.LIME + "/info - show total online and loaded chunks.");
						player.send(ChatHelper.LIME + "/tp - teleport to player OR coordinates.");
						player.send(ChatHelper.LIME + "/a - say something only to admins.");
						player.send(ChatHelper.LIME + "/admin &ltpassword&gt - enable admin mode.");
						player.send(ChatHelper.LIME + "/kick &ltID&gt - kick player with ID.");
					} else if (parameters[0].equals("spawn")) {
						player.teleport(0, 0);
					} else if (player.isAdmin()) {
						player.send(ChatHelper.RED + "Unknown command! Type '/help' for a list of commands.");
					}
				} else {
					logger.chat(message);
					broadcast((player.isAdmin() ? ChatHelper.ORANGE : "") + id + ": " + message, false);
				}
			}
		}

	}

	public void broadcast(final String text, final boolean adminOnly) {
		if (adminOnly) {
			players.forEach((k, player) -> {
				if (player.isAdmin()) {
					player.send(text);
				}
			});
		} else {
			players.forEach((k, player) -> player.send(text));
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

	public int getChunksLoaded() {
		return totalChunksLoaded;
	}
}
