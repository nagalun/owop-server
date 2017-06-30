package me.andreww7985.owopserver.server;

import java.util.concurrent.ConcurrentHashMap;

import me.andreww7985.owopserver.timings.TimingsEntry;
import me.andreww7985.owopserver.timings.TimingsRecord;

public class TimingsManager {
	private final ConcurrentHashMap<String, TimingsEntry> entries = new ConcurrentHashMap<String, TimingsEntry>();
	private final long start = System.currentTimeMillis();

	public void add(final TimingsRecord tr) {
		tr.stop();
		getEntry(tr.getName()).update(tr.getTime());
	}

	public long getTotal() {
		return System.currentTimeMillis() - start;
	}

	public TimingsEntry getEntry(final String name) {
		if (!entries.containsKey(name.toLowerCase())) {
			entries.put(name.toLowerCase(), new TimingsEntry(name));
		}
		return entries.get(name.toLowerCase());
	}

	public Object[] getEntries() {
		return entries.values().toArray();
	}
}
