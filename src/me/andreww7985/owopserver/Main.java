package me.andreww7985.owopserver;

import me.andreww7985.owopserver.server.OWOPServer;

public class Main {
	public static void main(final String[] args) {
		if (args.length < 2) {
			System.err.println("Not enough arguments!");
		} else {
			try {
				final OWOPServer server = new OWOPServer(args[1], Integer.parseInt(args[0]));
				server.run();
			} catch (final NumberFormatException e) {
				System.err.println("Bad port number!");
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}
}
