package me.andreww7985.owopserver.game;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import me.andreww7985.owopserver.helper.CompressionHelper;
import me.andreww7985.owopserver.network.LoginInfo;
import me.andreww7985.owopserver.server.LogManager;
import me.nagalun.jwebsockets.PreparedMessage;
import me.nagalun.jwebsockets.WebSocket;

public class Player {
	private final LogManager log = LogManager.getInstance();
	public static final int PRATE = 99999999, PTIME = 4, CRATE = 4, CTIME = 6, AFKMIN = 5;
	private long plastCheck = System.currentTimeMillis(), clastCheck = plastCheck, lastMoveTime = plastCheck;
	private float pallowance = PRATE, callowance = CRATE;
	private final WebSocket webSocket;
	private final LoginInfo loginInfo;
	private final World world;
	private final int id;
	private int x, y;
	private short color;
	private byte tool;
	private boolean admin;

	public Player(final int id, final World world, final LoginInfo loginInfo, final WebSocket webSocket) {
		this.id = id;
		this.world = world;
		this.webSocket = webSocket;
		this.loginInfo = loginInfo;
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

	public void putPixel(final int x, final int y, final short color) {
		if (!admin && !canPutPixel()) {
			log.warn("Player " + this + " exceeded pixel limit");
			kick();
			return;
		}
		world.putPixel(x, y, color);
	}

	public void chatMessage(final String text) {
		if (!admin && !canChat()) {
			log.warn("Player " + this + " exceeded chat limit");
			kick();
			return;
		}
		world.broadcast(text);
	}

	public void update(final int x, final int y, final byte tool, final short color) {
		if (this.x != x || this.y != y) {
			lastMoveTime = System.currentTimeMillis();
		}
		this.color = color;
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

	public short getColor() {
		return color;
	}

	public byte getTool() {
		return tool;
	}

	public World getWorld() {
		return world;
	}
	
	public LoginInfo getLoginInfo() {
		return loginInfo;
	}

	public int getID() {
		return id;
	}

	public boolean isConnected() {
		return webSocket.isConnected();
	}

	public void send(final byte[] data) {
		if (isConnected() && data != null) {
			webSocket.send(data);
		}
	}

	public void send(final PreparedMessage data) {
		if (isConnected() && data != null) {
			webSocket.sendPrepared(data);
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
		log.warn("Kicked player " + this);
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
