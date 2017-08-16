package me.andreww7985.owopserver.server;

import java.nio.ByteBuffer;
import java.util.HashMap;

import me.andreww7985.owopserver.game.World;

public class WorldManager {
	private final HashMap<String, World> worlds = new HashMap<>();
	private final WorldReader worldReader = new WorldReader();
	
	public WorldManager() {
		
	}
	
	public static boolean isWorldNameValid(final ByteBuffer nameBytes) {
		/* Validate world name, allowed chars are a..z, 0..9, '_' and '.' */
		final int size = nameBytes.capacity();

		if (size < 3 || size - 2 > 24 || nameBytes.getShort(size - 2) != 1337) {
			return false;
		}

		nameBytes.limit(size - 2);
		for (int i = 0; i < nameBytes.limit(); i++) {
			final byte b = nameBytes.get(i);
			if (!((b > 96 && b < 123) || (b > 47 && b < 58) || b == 95 || b == 46)) {
				return false;
			}
		}
		return true;
	}
}
