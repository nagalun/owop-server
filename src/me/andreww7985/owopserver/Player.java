package me.andreww7985.owopserver;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.java_websocket.WebSocket;

public class Player {
	private byte lastXMod, lastYMod, sameMod;
	private short tool;
	private final String world;
	private final WebSocket webSocket;
	private int x, y, rgb, lastX, lastY;
	private final int id;

	public Player(final int id, final String world, final WebSocket webSocket) {
		this.id = id;
		this.world = world;
		this.webSocket = webSocket;
		final ByteBuffer buffer = ByteBuffer.allocate(9);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(0, (byte) 0x00);
		buffer.putInt(1, id);
		webSocket.send(buffer);
	}

	public void getChunk(final int x, final int y) {
		final ByteBuffer buffer = ByteBuffer.allocate(801);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		final Chunk chunk = Server.getWorld(world).getChunk(x, y);
		buffer.put(0, (byte) 0x02);
		buffer.putInt(1, x);
		buffer.putInt(5, y);
		for (int yy = 0; yy < 16; yy++)
			for (int xx = 0; xx < 16; xx++) {
				buffer.put(9 + (yy * 16 + xx) * 3, (byte) (chunk.getPixel(xx, yy) >> 16 & 0xFF));
				buffer.put(10 + (yy * 16 + xx) * 3, (byte) (chunk.getPixel(xx, yy) >> 8 & 0xFF));
				buffer.put(11 + (yy * 16 + xx) * 3, (byte) (chunk.getPixel(xx, yy) & 0xFF));
			}
		send(buffer.array());
	}

	public void putPixel(final int x, final int y, final int rgb) {
		final Chunk chunk = Server.getWorld(world).getChunk(x >> 4, y >> 4);
		chunk.setPixel(x, y, rgb);
		Server.getWorld(world).pixelUpdates.add(new PixelUpdate(x, y, rgb));
	}

	public void update(final int x, final int y, final byte tool, final int rgb) {
		this.rgb = rgb;
		this.tool = tool;
		this.x = x;
		this.y = y;
		if (((x % 16) + 16) % 16 == lastXMod && x != lastX)
			sameMod++;
		else
			sameMod = 0;
		if (((y % 16) + 16) % 16 == lastYMod && y != lastY)
			sameMod++;
		else
			sameMod = 0;
		lastX = x;
		lastY = y;
		if (sameMod >= 20)
			System.out.println("[WARNING] Found BOT with id " + id + "! Disconnecting...");
		Server.getWorld(world).playerUpdates.add(new PlayerUpdate(x, y, rgb, tool, this.id));
	}

	public void sendUpdates() {
		final World w = Server.getWorld(world);
		final int players = w.playerUpdates.size(), pixels = w.pixelUpdates.size(),
				disconnects = w.playerDisconnects.size();
		final ByteBuffer buffer = ByteBuffer.allocate(5 + players * 16 + pixels * 11 + disconnects * 4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put(0, (byte) 0x01);
		buffer.put(1, (byte) players);
		for (int i = 0; i < players; i++) {
			buffer.putInt(2 + i * 16, w.playerUpdates.get(i).id);
			buffer.putInt(2 + i * 16 + 4, w.playerUpdates.get(i).x);
			buffer.putInt(2 + i * 16 + 8, w.playerUpdates.get(i).y);
			buffer.put(2 + i * 16 + 12, (byte) (w.playerUpdates.get(i).rgb >> 16 & 0xFF));
			buffer.put(2 + i * 16 + 13, (byte) (w.playerUpdates.get(i).rgb >> 8 & 0xFF));
			buffer.put(2 + i * 16 + 14, (byte) (w.playerUpdates.get(i).rgb & 0xFF));
			buffer.put(2 + i * 16 + 15, (byte) (w.playerUpdates.get(i).tool & 0xFF));
		}
		buffer.putShort(2 + players * 16, (short) pixels);
		for (int i = 0; i < pixels; i++) {
			buffer.putInt(4 + players * 16 + i * 11, w.pixelUpdates.get(i).x);
			buffer.putInt(4 + players * 16 + i * 11 + 4, w.pixelUpdates.get(i).y);
			buffer.put(4 + players * 16 + i * 11 + 8, (byte) (w.pixelUpdates.get(i).rgb >> 16 & 0xFF));
			buffer.put(4 + players * 16 + i * 11 + 9, (byte) (w.pixelUpdates.get(i).rgb >> 8 & 0xFF));
			buffer.put(4 + players * 16 + i * 11 + 10, (byte) (w.pixelUpdates.get(i).rgb & 0xFF));
		}
		buffer.put(4 + players * 16 + pixels * 11, (byte) disconnects);
		send(buffer.array());
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

	public int getID() {
		return id;
	}

	public boolean isConnected() {
		return webSocket.isOpen();
	}

	public void send(final byte[] data) {
		if (isConnected())
			webSocket.send(data);
	}
}
