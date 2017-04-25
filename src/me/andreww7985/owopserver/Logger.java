package me.andreww7985.owopserver;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Logger {
	private static String getTimestamp() {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss"));
	}

	public static void info(final String str) {
		System.out.println("[" + Logger.getTimestamp() + " INFO] " + str);
	}

	public static void warn(final String str) {
		System.out.println("[" + Logger.getTimestamp() + " WARNING] " + str);
	}

	public static void err(final String str) {
		System.err.println("[" + Logger.getTimestamp() + " ERROR] " + str);
	}

	public static void chat(final String str) {
		System.out.println("[" + (Logger.getTimestamp() + " CHAT] " + str));
	}
}
