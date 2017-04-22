package me.andreww7985.owopserver;

public class Main {
	public static void main(final String[] args) {
		if (args.length < 2)
			System.err.println("[ERROR] Not enough arguments!");
		else
			try {
				final Server server = new Server(args[1], Integer.parseInt(args[0]));
				server.run();
			} catch (final NumberFormatException e) {
				System.err.println("[ERROR] Bad port number!");
			} catch (final Exception e) {
				e.printStackTrace();
			}
	}
}
