package me.andreww7985.owopserver.server;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class World {
	private final ConcurrentHashMap<Long, Chunk> chunks;
	private final ReentrantLock updateLock;
	private final HashSet<Player> playerUpdates;
	private final ArrayList<PixelUpdate> pixelUpdates;
	private final HashSet<Integer> playerDisconnects;
	private final String name;
	private final WorldReader wr;
	private int playersId;
	private int online;

	public World(final String name) {
		this.chunks = new ConcurrentHashMap<Long, Chunk>();
		this.updateLock = new ReentrantLock();
		this.playerUpdates = new HashSet<Player>();
		this.pixelUpdates = new ArrayList<PixelUpdate>();
		this.playerDisconnects = new HashSet<Integer>();
		this.name = name;
		this.wr = new WorldReader(name);
	}

	private static long getChunkKey(final int x, final int y) {
		return (x << 32) + y;
	}

	public int getNextID() {
		++this.playersId;
		return this.playersId - 1;
	}

	public Chunk getChunk(final int x, final int y) {
		Chunk chunk = this.chunks.get(getChunkKey(x, y));
		if (chunk == null) {
			chunk = this.loadChunk(x, y);
		}
		return chunk;
	}

	private Chunk loadChunk(final int x, final int y) {
		final Chunk chunk = this.wr.readChunk(x, y);
		this.chunks.put(getChunkKey(x, y), chunk);
		OWOPServer.getInstance().chunksLoaded(1);
		return chunk;
	}

	public void setPixel(final int x, final int y, final int rgb) {
		final Chunk chunk = this.getChunk(x >> 4, y >> 4);
		if (chunk.getPixel(x & 0xF, y & 0xF) == rgb) {
			return;
		}
		chunk.setPixel(x & 0xF, y & 0xF, rgb);
		this.updateLock.lock();
		this.pixelUpdates.add(new PixelUpdate(x, y, rgb));
		this.updateLock.unlock();
	}

	public void playerMoved(final Player player) {
		this.updateLock.lock();
		this.playerUpdates.add(player);
		this.updateLock.unlock();
	}

	public void playerJoined(final Player player) {
		++this.online;
	}

	public void playerLeft(final Player player) {
		--this.online;
		this.updateLock.lock();
		this.playerDisconnects.add(player.getID());
		this.updateLock.unlock();
	}

	public void sendUpdates(final Player player) {
		this.updateLock.lock();
		final int players = this.playerUpdates.size();
		final int pixels = this.pixelUpdates.size();
		final int disconnects = this.playerDisconnects.size();
		if (players + pixels + disconnects == 0) {
			this.updateLock.unlock();
			return;
		}
		final ByteBuffer buffer = ByteBuffer.allocate(5 + players * 16 + pixels * 11 + disconnects * 4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put((byte) 1);
		buffer.put((byte) players);
		this.playerUpdates.forEach(p -> {
			final int rgb = p.getRGB();
			buffer.putInt(p.getID());
			buffer.putInt(p.getX());
			buffer.putInt(p.getY());
			buffer.put((byte) (rgb & 0xFF));
			buffer.put((byte) (rgb >> 8 & 0xFF));
			buffer.put((byte) (rgb >> 16 & 0xFF));
			buffer.put((byte) (p.getTool() & 0xFF));
			return;
		});
		buffer.putShort((short) pixels);
		this.pixelUpdates.forEach(p -> {
			buffer.putInt(p.x);
			buffer.putInt(p.y);
			buffer.put((byte) (p.rgb & 0xFF));
			buffer.put((byte) (p.rgb >> 8 & 0xFF));
			buffer.put((byte) (p.rgb >> 16 & 0xFF));
			return;
		});
		buffer.put((byte) disconnects);
		this.playerDisconnects.forEach(id -> buffer.putInt(id));
		this.updateLock.unlock();
		player.send(buffer.array());
	}

	public int getOnline() {
		return this.online;
	}

	public String getName() {
		return this.name;
	}

	public void save() {
		this.chunks.forEach((key, chunk) -> {
			if (chunk.shouldSave()) {
				this.wr.writeChunk(chunk, chunk.getX(), chunk.getY());
			}
			OWOPServer.getInstance().chunksUnloaded(1);
		});
	}

	public void clearChunk(final int x, final int y) {
		final Chunk chunk = this.getChunk(x, y);
		for (int yy = 0; yy < 16; ++yy) {
			for (int xx = 0; xx < 16; ++xx) {
				if (chunk.getPixel(xx, yy) != 16777215) {
					this.pixelUpdates.add(new PixelUpdate(x * 16 + xx, y * 16 + yy, 16777215));
				}
				chunk.setPixel(xx, yy, 16777215);
			}
		}
	}

	public void clearUpdates() {
		this.updateLock.lock();
		this.playerUpdates.clear();
		this.playerDisconnects.clear();
		this.pixelUpdates.clear();
		this.updateLock.unlock();
	}
}
