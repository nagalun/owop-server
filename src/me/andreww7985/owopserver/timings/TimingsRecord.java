package me.andreww7985.owopserver.timings;

public class TimingsRecord {
	private final long start = System.currentTimeMillis();
	private final String name;
	private long time;

	public TimingsRecord(final String name) {
		this.name = name;
	}

	public static TimingsRecord start(final String name) {
		return new TimingsRecord(name);
	}

	public void stop() {
		time = System.currentTimeMillis() - start;
	}

	public long getTime() {
		return time;
	}

	public String getName() {
		return name;
	}
}
