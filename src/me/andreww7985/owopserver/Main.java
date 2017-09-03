package me.andreww7985.owopserver;

import me.andreww7985.owopserver.server.OWOPServer;

public class Main {
	public static void main(final String[] args) {
		try {
			final OWOPServer server = new OWOPServer();
			server.run();
		} catch (final NumberFormatException e) {
			System.err.println("Bad port number!");
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}