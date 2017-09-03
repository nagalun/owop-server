package me.andreww7985.owopserver.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import me.andreww7985.owopserver.helper.ChatHelper;

public final class LogManager {
	private static LogManager instance = null;
	private final File logFile = new File("logs" + File.separator
			+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy-M-dd_HH-mm")) + ".txt");
	private final PrintWriter logPrinter;

	private LogManager() throws FileNotFoundException  {
		logFile.getParentFile().mkdirs();
		logPrinter = new PrintWriter(logFile);
	}

	private String getTimestamp() {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yy.MM.dd HH:mm:ss"));
	}

	private void print(final String str) {
		System.out.println(str);
		logPrinter.println(str);
		logPrinter.flush();
	}

	public void info(final String str) {
		print("[" + getTimestamp() + " INFO] " + str);
		OWOPServer.getInstance().broadcast(ChatHelper.DEV_CONSOLE + ChatHelper.BLUE + "[INFO] " + str, true);
	}

	public void warn(final String str) {
		print("[" + getTimestamp() + " WARN] " + str);
		OWOPServer.getInstance().broadcast(ChatHelper.DEV_CONSOLE + ChatHelper.YELLOW + "[WARN] " + str, true);
	}

	public void err(final String str) {
		print("[" + getTimestamp() + "  ERR] " + str);
		OWOPServer.getInstance().broadcast(ChatHelper.DEV_CONSOLE + ChatHelper.RED + "[ ERR] " + str, true);
	}

	public void exception(final Exception e) {
		err("Exception " + e.getMessage());
		e.printStackTrace();
		e.printStackTrace(logPrinter);
	}

	public void chat(final String str) {
		print("[" + getTimestamp() + " CHAT] " + str);
	}

	public void command(final String str) {
		print("[" + getTimestamp() + " CMND] " + str);
		OWOPServer.getInstance().broadcast(ChatHelper.DEV_CONSOLE + ChatHelper.BLUE + "[COMMAND] " + str, true);
	}
	
	public static LogManager getInstance() {
		if (instance == null) {
			try {
				instance = new LogManager();
			} catch (FileNotFoundException e) {
				throw new RuntimeException("Couldn't initialize LogManager!", e);
			}
		}
		return instance;
	}
}
