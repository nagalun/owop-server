package me.andreww7985.owopserver.timings;

public class TimingsEntry {
	private final String name;
	private long minimal = Long.MAX_VALUE, maximal, average, total;

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
