package me.andreww7985.owopserver;

import java.util.ArrayList;
import java.util.HashMap;

public class World {
	private final HashMap<Integer, HashMap<Integer, Chunk>> chunks = new HashMap<Integer, HashMap<Integer, Chunk>>();
	public ArrayList<Player> playerUpdates = new ArrayList<Player>();
	public ArrayList<Integer> playerDisconnects = new ArrayList<Integer>();
	public ArrayList<PixelUpdate> pixelUpdates = new ArrayList<PixelUpdate>();
	private int playersId;

	public int getNextNickname() {
		playersId++;
		return playersId - 1;
	}

	public Chunk getChunk(final int x, final int y) {
		if (!chunks.containsKey(x) || !chunks.get(x).containsKey(y))
			loadChunk(x, y);
		return chunks.get(x).get(y);
	}

	private void loadChunk(final int x, final int y) {
		if (!chunks.containsKey(x))
			chunks.put(x, new HashMap<Integer, Chunk>());
		final Chunk chunk = new Chunk();
		for (int yy = 0; yy < 16; yy++)
			for (int xx = 0; xx < 16; xx++)
				chunk.setPixel(xx, yy, 0xFFFFFF);
		chunks.get(x).put(y, chunk);
	}

	public void clearUpdates() {
		playerUpdates.clear();
		playerDisconnects.clear();
		pixelUpdates.clear();
	}
}
