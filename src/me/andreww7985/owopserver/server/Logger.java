package me.andreww7985.owopserver.server;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Logger {
	private static String getTimestamp() {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss"));
	}

	public void info(final String str) {
		System.out.println("[" + getTimestamp() + " INFO] " + str);
		OWOPServer.getInstance().broadcast("DEV<font style=\"color:#10AAE2;\">[INFO] " + str, true);
	}

	public void warn(final String str) {
		System.out.println("[" + getTimestamp() + " WARN] " + str);
		OWOPServer.getInstance().broadcast("DEV<font style=\"color:#FDD200;\">[WARN] " + str, true);
	}

	public void err(final String str) {
		System.err.println("[" + getTimestamp() + " ERROR] " + str);
		OWOPServer.getInstance().broadcast("DEV<font style=\"color:#EE1C25;\">[ERROR] " + str, true);
	}

	public void exception(final Exception e) {
		err("Exception " + e.getMessage());
		e.printStackTrace();
	}

	public void chat(final String str) {
		System.out.print("[" + getTimestamp() + " CHAT] " + str);
	}

	public void command(final String str) {
		System.out.println("[" + getTimestamp() + " COMMAND] " + str);
		OWOPServer.getInstance().broadcast("DEV<font style=\"color:#10AAE2;\">[COMMAND] " + str, true);
	}
}
