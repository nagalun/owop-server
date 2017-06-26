package me.andreww7985.owopserver.server;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class World {
	private final ConcurrentHashMap<Long, Chunk> chunks = new ConcurrentHashMap<Long, Chunk>();
	private final ReentrantLock updateLock = new ReentrantLock();
	private final HashSet<Player> playerUpdates = new HashSet<Player>();
	private final ArrayList<PixelUpdate> pixelUpdates = new ArrayList<PixelUpdate>();
	private final HashSet<Integer> playerDisconnects = new HashSet<Integer>();
	private byte[] updateCache;
	private final String name;
	private final WorldReader wr;
	private int playersId, online;

	public World(final String name) {
		this.name = name;
		this.wr = new WorldReader(name);
	}

	private static long getChunkKey(final int x, final int y) {
		return ((long) x << 32) + y;
	}

	public int getNextID() {
		playersId++;
		return playersId - 1;
	}

	public Chunk getChunk(final int x, final int y) {
		Chunk chunk = chunks.get(World.getChunkKey(x, y));
		if (chunk == null) {
			chunk = loadChunk(x, y);
		}
		return chunk;
	}

	private Chunk loadChunk(final int x, final int y) {
		final Chunk chunk = wr.readChunk(x, y);
		chunks.put(World.getChunkKey(x, y), chunk);
		OWOPServer.getInstance().chunksLoaded(1);
		return chunk;
	}

	public void setPixel(final int x, final int y, final int rgb565) {
		final Chunk chunk = getChunk(x >> 8, y >> 8);
		if (chunk.getPixel(x & 0xFF, y & 0xFF) == rgb565) {
			return;
		}
		chunk.setPixel(x & 0xFF, y & 0xFF, rgb565);
		updateLock.lock();
		pixelUpdates.add(new PixelUpdate(x, y, rgb565));
		updateLock.unlock();
	}

	public void playerMoved(final Player player) {
		updateLock.lock();
		playerUpdates.add(player);
		updateLock.unlock();
	}

	public void playerJoined(final Player player) {
		online++;
	}

	public void playerLeft(final Player player) {
		online--;
		updateLock.lock();
		playerDisconnects.add(player.getID());
		updateLock.unlock();
	}

	public void sendUpdates(final Player player) {
		player.send(updateCache);
	}

	public void updateCache() {
		updateLock.lock();
		final int players = playerUpdates.size(), pixels = pixelUpdates.size(), disconnects = playerDisconnects.size();

		if (players + pixels + disconnects == 0) {
			updateLock.unlock();
			return;
		}

		final ByteBuffer buffer = ByteBuffer.allocate(5 + players * 16 + pixels * 11 + disconnects * 4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put((byte) 1);

		// TODO: Fix possible error with 255+ player updates
		buffer.put((byte) players);
		playerUpdates.forEach(p -> {
			final int rgb = p.getRGB();
			buffer.putInt(p.getID());
			buffer.putInt(p.getX());
			buffer.putInt(p.getY());
			buffer.put((byte) (rgb & 0xFF));
			buffer.put((byte) (rgb >> 8 & 0xFF));
			buffer.put((byte) (rgb >> 16 & 0xFF));
			buffer.put((byte) (p.getTool() & 0xFF));
		});

		// TODO: Fix possible error with 65535+ pixel updates
		buffer.putShort((short) pixels);
		pixelUpdates.forEach(p -> {
			buffer.putInt(p.x);
			buffer.putInt(p.y);
			buffer.put((byte) (p.rgb & 0xFF));
			buffer.put((byte) (p.rgb >> 8 & 0xFF));
			buffer.put((byte) (p.rgb >> 16 & 0xFF));
		});

		// TODO: Fix possible error with 255+ player disconnects
		buffer.put((byte) disconnects);
		playerDisconnects.forEach(id -> {
			buffer.putInt(id);
		});

		updateLock.unlock();
		updateCache = buffer.array();
	}

	public int getOnline() {
		return online;
	}

	public String getName() {
		return name;
	}

	public void save() {
		chunks.forEach((key, chunk) -> {
			if (chunk.shouldSave()) {
				wr.writeChunk(chunk, chunk.getX(), chunk.getY());
			}
			OWOPServer.getInstance().chunksUnloaded(1);
		});
	}

	public void clearChunk(final int chunkX, final int chunkY) {
		final Chunk chunk = getChunk(chunkX, chunkY);
		for (int x = 0; x < 256; x++) {
			for (int y = 0; y < 256; y++) {
				if (chunk.getPixel(x, y) != 0xFFFF) {
					pixelUpdates.add(new PixelUpdate(chunkX * 256 + x, chunkY * 256 + y, 0xFFFF));
				}
				chunk.setPixel(x, y, 0xFFFF);
			}
		}
	}

	public void clearUpdates() {
		updateLock.lock();
		playerUpdates.clear();
		playerDisconnects.clear();
		pixelUpdates.clear();
		updateLock.unlock();
	}
}