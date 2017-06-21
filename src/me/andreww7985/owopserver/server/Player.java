package me.andreww7985.owopserver.server;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.java_websocket.WebSocket;

public class Player {
	private short tool;
	private final World world;
	private final WebSocket webSocket;
	private int x;
	private int y;
	private int rgb;
	private final int id;
	private boolean admin;

	public Player(final int id, final World world, final WebSocket webSocket) {
		this.id = id;
		this.world = world;
		this.webSocket = webSocket;
		final ByteBuffer buffer = ByteBuffer.allocate(9);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put((byte) 0);
		buffer.putInt(id);
		send(buffer.array());
	}

	public void getChunk(final int x, final int y) {
		final ByteBuffer buffer = ByteBuffer.allocate(777);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		final Chunk chunk = world.getChunk(x, y);
		buffer.put((byte) 2);
		buffer.putInt(x);
		buffer.putInt(y);
		buffer.position(9);
		buffer.put(chunk.getByteArray(), 0, 768);
		send(buffer.array());
	}

	public void putPixel(final int x, final int y, final int rgb) {
		world.setPixel(x, y, rgb);
	}

	public void update(final int x, final int y, final byte tool, final int rgb) {
		this.rgb = rgb;
		this.tool = tool;
		this.x = x;
		this.y = y;

		world.playerMoved(this);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getRGB() {
		return rgb;
	}

	public short getTool() {
		return tool;
	}

	public World getWorld() {
		return world;
	}

	public int getID() {
		return id;
	}

	public boolean isConnected() {
		return webSocket.isOpen();
	}

	public void send(final byte[] data) {
		if (isConnected()) {
			webSocket.send(data);
		}
	}

	public void sendMessage(final String data) {
		if (isConnected()) {
			webSocket.send(data);
		}
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(final boolean admin) {
		this.admin = admin;
		if (admin) {
			send(new byte[] { 4 });
		}
	}

	public void teleport(final int x, final int y) {
		final ByteBuffer buffer = ByteBuffer.allocate(9);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put((byte) 3);
		buffer.putInt(x);
		buffer.putInt(y);
		send(buffer.array());
	}

	public void kick() {
		OWOPServer.getInstance().getLogger().warn("Kicked player with ID " + id);
		webSocket.close();
	}
}
