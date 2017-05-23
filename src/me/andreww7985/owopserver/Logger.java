package me.andreww7985.owopserver;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Logger {
	private static String getTimestamp() {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss"));
	}

	public static void info(final String str) {
		System.out.println("[" + Logger.getTimestamp() + " INFO] " + str);
		Server.broadcast(ChatHelper.DEV_CONSOLE + ChatHelper.CYAN + "[INFO] " + str, true);
	}

	public static void warn(final String str) {
		System.out.println("[" + Logger.getTimestamp() + " WARN] " + str);
		Server.broadcast(ChatHelper.DEV_CONSOLE + ChatHelper.YELLOW + "[WARN] " + str, true);
	}

	public static void err(final String str) {
		System.err.println("[" + Logger.getTimestamp() + " ERROR] " + str);
		Server.broadcast(ChatHelper.DEV_CONSOLE + ChatHelper.RED + "[ERROR] " + str, true);
	}

	public static void exception(final Exception e) {
		Logger.err("Exception " + e.getMessage());
		e.printStackTrace();
	}

	public static void chat(final String str) {
		System.out.print("[" + (Logger.getTimestamp() + " CHAT] " + str));
	}

	public static void command(final String str) {
		System.out.println("[" + (Logger.getTimestamp() + " COMMAND] " + str));
		Server.broadcast(ChatHelper.DEV_CONSOLE + ChatHelper.CYAN + "[COMMAND] " + str, true);
	}
}
