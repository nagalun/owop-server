package me.andreww7985.owopserver;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.java_websocket.WebSocket;

public class Player {
	private byte lastXMod, lastYMod, sameMod;
	private short tool;
	private final World world;
	private final WebSocket webSocket;
	private int x, y, rgb, lastX, lastY;
	private final int id;

	public Player(final int id, final World world, final WebSocket webSocket) {
		this.id = id;
		this.world = world;
		this.webSocket = webSocket;
		final ByteBuffer buffer = ByteBuffer.allocate(9);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(0, (byte) 0x00);
		buffer.putInt(1, id);
		send(buffer.array());
	}

	public void getChunk(final int x, final int y) {
		final ByteBuffer buffer = ByteBuffer.allocate(777);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		final Chunk chunk = world.getChunk(x, y);
		buffer.put(0, (byte) 0x02);
		buffer.putInt(1, x);
		buffer.putInt(5, y);
		/*
		 * for (int yy = 0; yy < 16; yy++) { for (int xx = 0; xx < 16; xx++) {
		 * int rgb = chunk.getPixel(xx, yy); buffer.put(9 + (yy * 16 + xx) * 3,
		 * (byte) (rgb & 0xFF)); buffer.put(10 + (yy * 16 + xx) * 3, (byte) (rgb
		 * >> 8 & 0xFF)); buffer.put(11 + (yy * 16 + xx) * 3, (byte) (rgb >> 16
		 * & 0xFF)); } }
		 */
		buffer.position(9);
		buffer.put(chunk.getByteArray(), 0, 16 * 16 * 3);
		send(buffer.array());
	}

	public void putPixel(final int x, final int y, final int rgb) {
		// TODO: Implement timeout
		world.putPixel(x, y, rgb);
	}

	public void update(final int x, final int y, final byte tool, final int rgb) {
		this.rgb = rgb;
		this.tool = tool;
		this.x = x;
		this.y = y;
		if (((x % 16) + 16) % 16 == lastXMod && x != lastX) {
			sameMod++;
		} else {
			sameMod = 0;
		}
		if (((y % 16) + 16) % 16 == lastYMod && y != lastY) {
			sameMod++;
		} else {
			sameMod = 0;
		}
		lastX = x;
		lastY = y;
		if (sameMod >= 20) {
			// TODO: Kick bots
			Logger.warn("Found BOT with id " + id + "! Disconnecting...");
		}
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

	public void send(final String data) {
		if (isConnected()) {
			webSocket.send(data);
		}
	}
}
