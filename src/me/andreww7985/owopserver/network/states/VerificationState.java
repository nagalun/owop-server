package me.andreww7985.owopserver.network.states;

import java.nio.ByteBuffer;

import me.andreww7985.owopserver.network.NetworkState;
import me.andreww7985.owopserver.network.SocketVerifier;
import me.nagalun.jwebsockets.HttpRequest;
import me.nagalun.jwebsockets.WebSocket;

public class VerificationState extends NetworkState {
	private final static String ORIGIN_URL = "ourworldofpixels.com"; 
	private static boolean verifyOrigin = false;
	
	protected VerificationState(final WebSocket socket, final SocketVerifier sv) {
		super(socket);
		onUpgrade();
	}
	
	@Override
	public void onUpgrade() {
		
	}
	
	@Override
	public void upgrade() {
		//socket.userData = new LoginState();
	}

	@Override
	public void processMessage(ByteBuffer msg) {
		
	}

	@Override
	public void processMessage(String msg) {
		/* Nothing */
	}
	
	/* Returns true if headers are OK */
	public static boolean verifyHeaders(final HttpRequest req) {
		final String origin = req.getHeader("origin");
		if (origin == null) {
			return false;
		}
		int idx = origin.startsWith("http://www.") ? 11 : 7;
		return !(verifyOrigin && !origin.regionMatches(idx, origin, 0, ORIGIN_URL.length()));
	}
}
