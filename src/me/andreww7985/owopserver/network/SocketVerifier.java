package me.andreww7985.owopserver.network;

import java.util.LinkedList;
import java.util.List;

import me.nagalun.jwebsockets.WebSocket;

public class SocketVerifier {
	public static enum verifyStatus {
		PASS, FAIL, DEFERRED
	}
	
	private List<VerificationStep> steps = new LinkedList<>();
	
	public SocketVerifier() {
		
	}
	
	public void verify(final WebSocket socket) {
		
	}
}
