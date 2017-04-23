package me.andreww7985.owopserver;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class Server extends WebSocketServer {
	private static HashMap<InetSocketAddress, Player> players = new HashMap<InetSocketAddress, Player>();
	private static HashMap<String, World> worlds = new HashMap<String, World>();
	private static String admPass;

	public Server(final String admPass, final int port) throws Exception {
		super(new InetSocketAddress(port));
		Server.admPass = admPass;
		System.out.println("[INFO] Admin password is " + Server.admPass);
		System.out.println("[INFO] Starting server on port " + this.getAddress().getPort() + ", IP: "
				+ this.getAddress().getHostName());
	}

	@Override
	public void onOpen(final WebSocket ws, final ClientHandshake handshake) {
		System.out.println("[INFO] Connected new player from " + ws.getRemoteSocketAddress());
		if (players.containsKey(ws.getRemoteSocketAddress())) {
			System.err.println("[ERROR] Connected player from used IP!");
			return;
		}
	}

	@Override
	public void onClose(final WebSocket ws, final int code, final String reason, final boolean remote) {
		System.out.println("[INFO] Disconnected player from " + ws.getRemoteSocketAddress());
		if (players.containsKey(ws.getRemoteSocketAddress())) {
			players.remove(ws.getRemoteSocketAddress());
		}
	}

	@Override
	public void onMessage(final WebSocket ws, final ByteBuffer message) {
		if (!players.containsKey(ws.getRemoteSocketAddress())) {
			final byte[] bytes = message.array();
			String world = "";

			for (int i = 0; i < bytes.length - 2; i++) {
				world += (char) bytes[i];
			}

			if (!worlds.containsKey(world)) {
				worlds.put(world, new World());
			}

			players.put(ws.getRemoteSocketAddress(), new Player(worlds.get(world).getNextID(), world, ws));
			players.get(ws.getRemoteSocketAddress()).send("Hi, you are on BETA server!");
			players.get(ws.getRemoteSocketAddress()).send("If you found bugs, please let us know!");
			System.out.println("[INFO] Joined player from " + ws.getRemoteSocketAddress() + " to world " + world);
		} else {
			switch (message.array().length) {
			case 8: {
				message.order(ByteOrder.LITTLE_ENDIAN);
				final int x = message.getInt(0), y = message.getInt(4);
				players.get(ws.getRemoteSocketAddress()).getChunk(x, y);
				break;
			}
			case 11: {
				message.order(ByteOrder.LITTLE_ENDIAN);
				final ByteBuffer temp = ByteBuffer.allocate(14);
				temp.order(ByteOrder.LITTLE_ENDIAN);
				temp.put(message);
				final int x = temp.getInt(0), y = temp.getInt(4);
				final int rgb = ((temp.getShort(8) & 0xFF) << 16) | ((temp.getShort(9) & 0xFF) << 8)
						| ((temp.getShort(10)) & 0xFF);
				players.get(ws.getRemoteSocketAddress()).putPixel(x, y, rgb);
				break;
			}
			case 12: {
				message.order(ByteOrder.LITTLE_ENDIAN);
				final ByteBuffer temp = ByteBuffer.allocate(15);
				temp.order(ByteOrder.LITTLE_ENDIAN);
				temp.put(message);
				final int x = temp.getInt(0), y = temp.getInt(4), tool = (temp.getShort(8) & 0xFF);
				final int rgb = ((temp.getShort(9) & 0xFF) << 16) | ((temp.getShort(10) & 0xFF) << 8)
						| ((temp.getShort(11)) & 0xFF);
				players.get(ws.getRemoteSocketAddress()).update(x, y, (byte) tool, rgb);
				break;
			}
			default:
				players.forEach((k, v) -> v.send(String.valueOf(message.asCharBuffer().array())));
			}
		}
	}

	@Override
	public void onError(final WebSocket conn, final Exception ex) {
		players.forEach((k, v) -> v.send("SERVER " + ex.toString()));
	}

	@Override
	public void onStart() {
		final Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				players.forEach((k, v) -> v.sendUpdates());
				worlds.forEach((k, v) -> v.clearUpdates());
			}
		}, 0, 50);
	}

	@Override
	public void onMessage(final WebSocket ws, final String message) {
		players.forEach((k, v) -> v.send(players.get(ws.getRemoteSocketAddress()).getID() + ": " + message));
	}

	public static World getWorld(final String world) {
		return worlds.get(world);
	}
}
