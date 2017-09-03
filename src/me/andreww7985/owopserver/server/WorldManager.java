package me.andreww7985.owopserver.server;

import java.nio.ByteBuffer;
import java.util.HashMap;

import me.andreww7985.owopserver.game.World;
import me.nagalun.async.ITaskScheduler;

public class WorldManager {
	private final HashMap<String, World> worlds = new HashMap<>();
	private final WorldReader worldReader = new WorldReader();
	private final LogManager log = LogManager.getInstance();
	private int updatesTimerID;
	
	public WorldManager() {
		
	}
	
	public void onStart(final ITaskScheduler ts) {
		updatesTimerID = ts.setInterval(() -> sendUpdates(), 50);
	}
	
	public void onStop(final ITaskScheduler ts) {
		ts.clear(updatesTimerID);
	}
	
	public World getWorld(final String worldName) {
		World world = worlds.get(worldName);
		if (world == null) {
			world = new World(worldName, worldReader);
			worlds.put(worldName, world);
			log.info("Loaded world " + world);
		}
		return world;
	}

	public void unloadWorld(final String worldName) {
		final World world = worlds.get(worldName);
		unloadWorld(world);
	}

	public void unloadWorld(final World world) {
		if (world != null) {
			world.save();
			worlds.remove(world.getName());
			log.info("Unloaded world " + world);
		}
	}
	
	public void unloadAllWorlds() {
		worlds.values().removeIf(world -> {
			world.save();
			log.info("Unloaded world " + world);
			return true;
		});
	}
	
	public void sendUpdates() {
		for (final World world : worlds.values()) {
			world.sendUpdates();
		}
	}
	
	public static boolean isWorldNameValid(final ByteBuffer nameBytes) {
		/* Validate world name, allowed chars are a..z, 0..9, '_' and '.' */
		final int size = nameBytes.remaining();

		if (size <= 0 || size > 24) {
			return false;
		}
		
		try {
			nameBytes.mark();
			while (nameBytes.hasRemaining()) {
				final byte b = nameBytes.get();
				if (!((b > 96 && b < 123) || (b > 47 && b < 58) || b == 95 || b == 46)) {
					return false;
				}
			}
			return true;
		} finally {
			nameBytes.reset();
		}
	}
}
