package me.andreww7985.owopserver.server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Logger {
	private static String getTimestamp() {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss"));
	}

	public void info(final String str) {
		System.out.println("[" + Logger.getTimestamp() + " INFO] " + str);
		OWOPServer.getInstance().broadcast(ChatHelper.DEV_CONSOLE + ChatHelper.CYAN + "[INFO] " + str, true);
	}

	public void warn(final String str) {
		System.out.println("[" + Logger.getTimestamp() + " WARN] " + str);
		OWOPServer.getInstance().broadcast(ChatHelper.DEV_CONSOLE + ChatHelper.YELLOW + "[WARN] " + str, true);
	}

	public void err(final String str) {
		System.err.println("[" + Logger.getTimestamp() + " ERROR] " + str);
		OWOPServer.getInstance().broadcast(ChatHelper.DEV_CONSOLE + ChatHelper.RED + "[ERROR] " + str, true);
	}

	public void exception(final Exception e) {
		err("Exception " + e.getMessage());
		e.printStackTrace();
	}

	public void chat(final String str) {
		System.out.print("[" + (Logger.getTimestamp() + " CHAT] " + str));
	}

	public void command(final String str) {
		System.out.println("[" + (Logger.getTimestamp() + " COMMAND] " + str));
		OWOPServer.getInstance().broadcast(ChatHelper.DEV_CONSOLE + ChatHelper.CYAN + "[COMMAND] " + str, true);
	}
}
