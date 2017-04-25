package me.andreww7985.owopserver;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Logger {
	private static String getTimestamp() {
		return LocalDateTime.now()
				.format(DateTimeFormatter.ofPattern("HH:mm"));
	}
	
	public static void info(String str) {
		System.out.println(Logger.getTimestamp() + " [INFO] " + str);
	}
	
	public static void warn(String str) {
		System.out.println(Logger.getTimestamp() + " [WARNING] " + str);
	}
	
	public static void err(String str) {
		System.err.println(Logger.getTimestamp() + " [ERROR] " + str);
	}
}
