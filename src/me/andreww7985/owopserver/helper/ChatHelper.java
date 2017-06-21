package me.andreww7985.owopserver.helper;

public class ChatHelper {
	public static final String LIME = "<font style=\"color:#8FC637;\">";
	public static final String CYAN = "<font style=\"color:#10AAE2;\">";
	public static final String RED = "<font style=\"color:#EE1C25;\">";
	public static final String YELLOW = "<font style=\"color:#FDD200;\">";
	public static final String ORANGE = "<font style=\"color:#FE8100;\">";
	public static final String BLUE = "<font style=\"color:#0C5EBA;\">";
	public static final String DEV_CONSOLE = "DEV";

	public static String format(final String input) {
		return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
				.replace("'", "&#x27;").replace("/", "&#x2F;");
	}
}