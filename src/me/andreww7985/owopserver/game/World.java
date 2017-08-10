package me.andreww7985.owopserver.game;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import me.andreww7985.owopserver.helper.ChatHelper;
import me.andreww7985.owopserver.server.OWOPServer;
import me.nagalun.jwebsockets.PreparedMessage;

public class World {
	private final HashMap<Integer, Player> players = new HashMap<>();
	private final HashMap<Long, Chunk> chunks = new HashMap<>();
	private final HashSet<Player> playerUpdates = new HashSet<>();
	private final ArrayList<PixelUpdate> pixelUpdates = new ArrayList<>();
	private final HashSet<Integer> playerDisconnects = new HashSet<>();
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

	public void putPixel(final int x, final int y, final short rgb565) {
		final Chunk chunk = getChunk(x >> 8, y >> 8);
		if (chunk.getPixel((byte) x, (byte) y) == rgb565) {
			return;
		}
		chunk.putPixel((byte) x, (byte) y, rgb565);
		pixelUpdates.add(new PixelUpdate(x, y, rgb565));
	}

	public void playerMoved(final Player player) {
		playerUpdates.add(player);
	}

	public void playerJoined(final Player player) {
		online++;
		player.sendMessage(ChatHelper.LIME + "Joined world " + this + ". Your ID: " + player.getID());
		players.put(player.getID(), player);
	}

	public void playerLeft(final Player player) {
		online--;
		players.remove(player.getID());
		playerDisconnects.add(player.getID());
	}

	public void sendUpdates() {
		final int players = playerUpdates.size(), pixels = pixelUpdates.size(), disconnects = playerDisconnects.size();

		if (players + pixels + disconnects < 1) {
			return;
		}

		final ByteBuffer buffer = ByteBuffer.allocate(5 + players * 15 + pixels * 10 + disconnects * 4);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer.put((byte) 1);

		// TODO: Fix possible error with 255+ player updates
		buffer.put((byte) players);
		playerUpdates.forEach(p -> {
			buffer.putInt(p.getID());
			buffer.putInt(p.getX());
			buffer.putInt(p.getY());
			buffer.putShort(p.getRGB565());
			buffer.put(p.getTool());
		});

		// TODO: Fix possible error with 65535+ pixel updates
		buffer.putShort((short) pixels);
		pixelUpdates.forEach(p -> {
			buffer.putInt(p.x);
			buffer.putInt(p.y);
			buffer.putShort(p.rgb565);
		});

		// TODO: Fix possible error with 255+ player disconnects
		buffer.put((byte) disconnects);
		playerDisconnects.forEach(id -> {
			buffer.putInt(id);
		});

		buffer.flip();
		final PreparedMessage data = OWOPServer.getInstance().prepareMessage(buffer);

		playerUpdates.clear();
		playerDisconnects.clear();
		pixelUpdates.clear();

		this.players.forEach((k, player) -> player.send(data));
		
		data.finalizeMessage();
	}

	public int getOnline() {
		return online;
	}

	public String getName() {
		return name;
	}

	public void save() {
		OWOPServer.getInstance().chunksUnloaded(chunks.values().size());
		chunks.forEach((key, chunk) -> {
			if (chunk.shouldSave()) {
				wr.saveChunk(chunk);
			}
		});
	}

	public void clearChunk(final int chunk16X, final int chunk16Y, final short rgb565) {
		final Chunk chunk = getChunk(chunk16X >> 4, chunk16Y >> 4);
		for (int x = 0; x < 16; x++) {
			for (int y = 0; y < 16; y++) {
				final byte pixelX = (byte) ((chunk16X << 4) + x), pixelY = (byte) ((chunk16Y << 4) + y);
				if (chunk.getPixel(pixelX, pixelY) != rgb565) {
					pixelUpdates.add(new PixelUpdate((chunk16X << 4) + x, (chunk16Y << 4) + y, rgb565));
				}
				chunk.putPixel(pixelX, pixelY, rgb565);
			}
		}
	}

	public void broadcast(final String text) {
		OWOPServer.getInstance().getLogManager().chat(this + " " + text);
		players.forEach((k, player) -> player.sendMessage(text));
	}

	public Player getPlayer(final int id) {
		return players.get(id);
	}

	@Override
	public String toString() {
		return "'" + name + "'";
	}
}