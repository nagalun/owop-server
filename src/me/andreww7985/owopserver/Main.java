package me.andreww7985.owopserver;

public class Main {
	public static void main(final String[] args) {
		if (args.length < 2) {
			Logger.err("Not enough arguments!");
		} else {
			try {
				final Server server = new Server(args[1], Integer.parseInt(args[0]));
				server.run();
			} catch (final NumberFormatException e) {
				Logger.err("Bad port number!");
			} catch (final Exception e) {
				Logger.exception(e);
			}
		}
	}
}
