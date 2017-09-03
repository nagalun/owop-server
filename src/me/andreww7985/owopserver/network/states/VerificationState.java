package me.andreww7985.owopserver.network.states;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import me.andreww7985.owopserver.helper.HttpRequestHelper;
import me.andreww7985.owopserver.helper.HttpRequestHelper.RequestCallback;
import me.andreww7985.owopserver.network.NetworkState;
import me.andreww7985.owopserver.network.StateId;
import me.andreww7985.owopserver.server.OWOPServer;
import me.nagalun.async.ITaskScheduler;
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
	private final static byte SOP_PROTOCOL_VERSION = 0x01;
	private final static byte SOP_CAPTCHA_STATUS = 0x02;
	
	private final static String ORIGIN_URL = OWOPServer.getProperty("origin-url");
	private final static String CAPTCHA_API_URL = "https://www.google.com/recaptcha/api/siteverify";
	private final static String CAPTCHA_API_SECRET = OWOPServer.getProperty("captcha-secret");
	private static boolean verifyOrigin = OWOPServer.getBoolProperty("verify-origin");
	private static boolean requireCaptcha = OWOPServer.getBoolProperty("captcha-enable") && CAPTCHA_API_SECRET != null;
	private byte captchaStatus = CAS_CAPTCHA_INVALID;
	
	public VerificationState(final WebSocket socket) {
		super(socket, StateId.VERIFICATION);
		sendProtocolVersion();
	}
	
	private void sendCaptchaState(final byte s) {
		socket.send(new byte[] { SOP_CAPTCHA_STATUS, s });
	}
	
	private void sendProtocolVersion() {
		socket.send(new byte[] { SOP_PROTOCOL_VERSION, PROTOCOL_VERSION });
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
		if (msg.remaining() != 1 || captchaStatus != CAS_CAPTCHA_INVALID) {
			socket.close();
			return;
		}
		
		final int clientVersion = msg.get() & 0xFF;
		
		if (clientVersion != PROTOCOL_VERSION) {
			socket.close();
			return;
		}
		
		if (requireCaptcha) {
			captchaStatus = CAS_CAPTCHA_WAITING;
			sendCaptchaState(CAS_CAPTCHA_WAITING);
		} else {
			upgrade();
		}
	}
	
	private void clientCaptchaToken(final ByteBuffer msg) {
		if (captchaStatus != CAS_CAPTCHA_WAITING || msg.remaining() == 0 || msg.remaining() > 512) {
			socket.close();
			return;
		}
		
		final String token = StandardCharsets.UTF_8.decode(msg).toString();
		captchaStatus = CAS_CAPTCHA_VERIFYING;
		verifyCaptchaToken(token);
	}
	
	private void verifyCaptchaToken(final String token) {
		sendCaptchaState(CAS_CAPTCHA_VERIFYING);
		final ITaskScheduler ts = server.getTaskScheduler();
		try {
			HttpRequestHelper.doRequest(CAPTCHA_API_URL, String.format("secret=%s&response=%s", 
					URLEncoder.encode(CAPTCHA_API_SECRET, StandardCharsets.UTF_8.name()),
					URLEncoder.encode(token, StandardCharsets.UTF_8.name())),
					new RequestCallback() { /* keep in mind that these methods are executed from another thread */
						@Override
						public void done(final String data, final HttpURLConnection conn) {
							try {
								System.out.println(conn.getResponseCode() + ", " + data);
								ts.idleCallback(() -> {
									sendCaptchaState(CAS_CAPTCHA_VERIFIED);
									upgrade();
								});
							} catch (IOException e) {
								e.printStackTrace();
							}
						}

						@Override
						public void error(final Exception e) {
							ts.idleCallback(() -> {
								sendCaptchaState(CAS_CAPTCHA_INVALID);
								socket.close();
							});
							e.printStackTrace();
						}	
					}
			);
		} catch (final UnsupportedEncodingException e) {
			sendCaptchaState(CAS_CAPTCHA_INVALID);
			socket.close();
			e.printStackTrace();
		}
	}
}
