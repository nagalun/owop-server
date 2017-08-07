package me.andreww7985.owopserver.game;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.java_websocket.WebSocket;

import me.andreww7985.owopserver.helper.CompressionHelper;
import me.andreww7985.owopserver.server.OWOPServer;

public class Player {
	public static final int PRATE = 32, PTIME = 4, CRATE = 4, CTIME = 6, AFKMIN = 5;
	private long plastCheck = System.currentTimeMillis(), clastCheck = plastCheck, lastMoveTime = plastCheck;
	private float pallowance = PRATE, callowance = CRATE;
	private final WebSocket webSocket;
	private final World world;
	private final int id;
	private int x, y;
	private short rgb565;
	private byte tool;
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
		final Chunk chunk = world.getChunk(x, y);
		final byte[] compressedChunk = CompressionHelper.compress(chunk.getByteArray());
		final ByteBuffer buffer = ByteBuffer.allocate(9 + compressedChunk.length);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put((byte) 2);
		buffer.putInt(x);
		buffer.putInt(y);
		buffer.put(compressedChunk);
		send(buffer.array());
	}

	public void putPixel(final int x, final int y, final short rgb565) {
		if (!admin && !canPutPixel()) {
			OWOPServer.getInstance().getLogManager().warn("Player " + this + " exceeded pixel limit");
			kick();
			return;
		}
		world.putPixel(x, y, rgb565);
	}

	public void chatMessage(final String text) {
		if (!admin && !canChat()) {
			OWOPServer.getInstance().getLogManager().warn("Player " + this + " exceeded chat limit");
			kick();
			return;
		}
		world.broadcast(text);
	}

	public void update(final int x, final int y, final byte tool, final short rgb565) {
		if (this.x != x || this.y != y) {
			lastMoveTime = System.currentTimeMillis();
		}
		this.rgb565 = rgb565;
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

	public short getRGB565() {
		return rgb565;
	}

	public byte getTool() {
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
		if (isConnected() && data != null) {
			webSocket.send(data);
		}
	}

	public void sendMessage(final String text) {
		if (isConnected()) {
			webSocket.send(text);
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

	@Override
	public String toString() {
		return "(ID " + id + ", '" + world.getName() + "')";
	}

	public void kick() {
		OWOPServer.getInstance().getLogManager().warn("Kicked player " + this);
		world.playerLeft(this);
		webSocket.close();
	}

	public boolean canChat() {
		callowance += (System.currentTimeMillis() - clastCheck) / 1000f * ((float) CRATE / CTIME);
		clastCheck = System.currentTimeMillis();
		if (callowance > CRATE) {
			callowance = CRATE;
		}
		if (callowance < 1) {
			return false;
		}
		callowance--;
		return true;
	}

	public boolean canPutPixel() {
		pallowance += (System.currentTimeMillis() - plastCheck) / 1000f * ((float) PRATE / PTIME);
		plastCheck = System.currentTimeMillis();
		if (pallowance > PRATE) {
			pallowance = PRATE;
		}
		if (pallowance < 1) {
			return false;
		}
		pallowance--;
		return true;
	}

	public long getLastMoveTime() {
		return lastMoveTime;
	}
}
