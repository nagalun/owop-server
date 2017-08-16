package me.andreww7985.owopserver.network.states;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import me.andreww7985.owopserver.network.NetworkState;
import me.andreww7985.owopserver.network.StateId;
import me.nagalun.jwebsockets.HttpRequest;
import me.nagalun.jwebsockets.WebSocket;

public class VerificationState extends NetworkState {
	/* Captcha status codes */
	private final static byte CAS_CAPTCHA_WAITING = 0x00;
	private final static byte CAS_CAPTCHA_VERIFYING = 0x01;
	private final static byte CAS_CAPTCHA_VERIFIED = 0x02;
	private final static byte CAS_CAPTCHA_INVALID = 0x04;
	
	/* Client opcodes */
	private final static byte COP_PROTOCOL_VERSION = 0x01; /* Must match server's version */
	private final static byte COP_CAPTCHA_TOKEN = 0x02;
	
	/* Server opcodes */
	private final static byte SOP_PROTOCOL_VERSION = 0x01; /* int */
	private final static byte SOP_CAPTCHA_STATUS = 0x02;
	
	private final static String ORIGIN_URL = "ourworldofpixels.com"; 
	private static boolean verifyOrigin = false;
	private static boolean requireCaptcha = false;
	private static boolean waitingForCaptcha = false;
	
	protected VerificationState(final WebSocket socket) {
		super(socket, StateId.VERIFICATION);
		socket.send(new byte[] {SOP_PROTOCOL_VERSION, PROTOCOL_VERSION});
	}
	
	public void upgrade() {
		socket.userData = new LoginState(socket);
	}

	@Override
	public void processMessage(final ByteBuffer msg) {
		switch (msg.get()) {
		case COP_PROTOCOL_VERSION:
			clientProtocolVersion(msg);
			break;
			
		case COP_CAPTCHA_TOKEN:
			clientCaptchaToken(msg);
			break;
			
		default:
			socket.close();
		}
	}

	@Override
	public void processMessage(final String msg) {
		/* Nothing */
		socket.close();
	}
	
	@Override
	public void socketDisconnected() {
		
	}
	
	/* Returns true if headers are OK */
	public static boolean verifyHeaders(final HttpRequest req) {
		final String origin = req.getHeader("origin");
		if (origin == null) {
			return !verifyOrigin;
		}
		int idx = origin.startsWith("http://www.") ? 11 : 7;
		return !(verifyOrigin && !origin.regionMatches(idx, origin, 0, ORIGIN_URL.length()));
	}
	
	private void clientProtocolVersion(final ByteBuffer msg) {
		if (msg.remaining() != 4) {
			socket.close();
			return;
		}
		
		final int clientVersion = msg.getInt();
		
		if (clientVersion != PROTOCOL_VERSION) {
			socket.close();
			return;
		}
		
		if (requireCaptcha) {
			socket.send(new byte[] {SOP_CAPTCHA_STATUS, CAS_CAPTCHA_WAITING});
		} else {
			upgrade();
		}
	}
	
	private void clientCaptchaToken(final ByteBuffer msg) {
		if (!waitingForCaptcha || msg.remaining() == 0 || msg.remaining() > 512) {
			socket.close();
			return;
		}
		
		final String token = StandardCharsets.UTF_8.decode(msg).toString();
		/* Do something ... */
	}
}
