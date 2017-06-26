package me.andreww7985.owopserver.helper;

import java.util.concurrent.ConcurrentHashMap;

public class TimingsHelper {
	private static final ConcurrentHashMap<String, TimingsEntry> entries = new ConcurrentHashMap<String, TimingsEntry>();
	private static final ConcurrentHashMap<TimingsEntry, Long> recording = new ConcurrentHashMap<TimingsEntry, Long>();
	private static final TimingsHelper instance = new TimingsHelper();
	private static final long START = System.currentTimeMillis();

	public static void start(final String name) {
		recording.put(getEntry(name), System.currentTimeMillis());
	}

	public static void stop(final String name) {
		final TimingsEntry entry = getEntry(name);
		entry.update(System.currentTimeMillis() - recording.get(entry));
	}

	public static long getTotal() {
		return System.currentTimeMillis() - START;
	}

	public static TimingsEntry getEntry(final String name) {
		if (!entries.containsKey(name.toLowerCase())) {
			entries.put(name.toLowerCase(), instance.new TimingsEntry(name));
		}
		return entries.get(name.toLowerCase());
	}

	public static Object[] getEntries() {
		return entries.values().toArray();
	}

	public class TimingsEntry {
		private long minimal = Long.MAX_VALUE, maximal, average, total;
		private final String name;

		public TimingsEntry(final String name) {
			this.name = name;
		}

		public void update(final long millis) {
			if (millis < minimal) {
				minimal = millis;
			}
			if (millis > maximal) {
				maximal = millis;
			}
			average = (average + millis) / 2;
			total += millis;
		}

		public long getTotal() {
			return total;
		}

		public long getMinimal() {
			return minimal;
		}

		public long getMaximal() {
			return maximal;
		}

		public long getAverage() {
			return average;
		}

		public String getName() {
			return name;
		}
	}
}
