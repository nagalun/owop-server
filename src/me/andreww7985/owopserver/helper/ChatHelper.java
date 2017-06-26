package me.andreww7985.owopserver.helper;

public class ChatHelper {
	public static final String LIME = "<div style=\"color:#8FC637\">";
	public static final String CYAN = "<div style=\"color:#10AAE2\">";
	public static final String RED = "<div style=\"color:#EE1C25\">";
	public static final String YELLOW = "<div style=\"color:#FDD200\">";
	public static final String ORANGE = "<div style=\"color:#FE8100\">";
	public static final String BLUE = "<div style=\"color:#0C5EBA\">";
	public static final String CENTER = "<div style=\"text-align:center\">";
	public static final String DEV_CONSOLE = "DEV";

	public static String format(final String input) {
		return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
				.replace("'", "&#x27;").replace("/", "&#x2F;");
	}
}
