package me.andreww7985.owopserver.network.states;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import me.andreww7985.owopserver.network.LoginInfo;
import me.andreww7985.owopserver.network.NetworkState;
import me.andreww7985.owopserver.network.StateId;
import me.nagalun.jwebsockets.WebSocket;

public class LoginState extends NetworkState {
	/* Client opcodes */
	private final static byte COP_LOGIN_GUEST = 0x01; /* Guest name included */
	private final static byte COP_LOGIN = 0x02;
	private final static byte COP_REGISTER = 0x03;

	/* Server opcodes */
	private final static byte SOP_LOGIN_INFO = 0x01; /* Sets client name */
	private final static byte SOP_LOGIN_STATUS = 0x02;

	protected LoginState(final WebSocket socket) {
		super(socket, StateId.LOGIN);
	}

	protected void upgrade(final LoginInfo info) {
		final byte[] name = info.name.getBytes(StandardCharsets.UTF_8);
		final byte[] packet = new byte[1 + name.length];
		packet[0] = SOP_LOGIN_INFO;

		System.arraycopy(name, 0, packet, 1, name.length);

		socket.send(packet);
		socket.userData = new LobbyState(socket, info);
	}

	@Override
	public void processMessage(final ByteBuffer msg) {
		switch (msg.get()) {
		case COP_LOGIN_GUEST:
			clientLoginGuest(msg);
			break;

		case COP_LOGIN:
			clientLogin(msg);
			break;

		case COP_REGISTER:
			clientRegister(msg);
			break;

		default:
			socket.close();
		}

	}

	@Override
	public void processMessage(final String msg) {
		socket.close();
	}

	@Override
	public void socketDisconnected() {

	}

	/*
	 * Message: uint8 (name string size) [Min: 2, Max: 32] String (size must match
	 * the uint8)
	 **/
	private void clientLoginGuest(final ByteBuffer msg) {
		if (msg.remaining() < 3) {
			socket.close();
			return;
		}

		final byte size = msg.get();

		/* NOTE: size limit will not be accurate on utf8, but we only want ascii names anyways */
		if (!(size >= 2 && size <= 32 && msg.remaining() == size)) {
			socket.close();
			return;
		}

		final String name = StandardCharsets.ISO_8859_1.decode(msg).toString();
		final LoginInfo info = new LoginInfo(name);
		upgrade(info);
	}

	private void clientLogin(final ByteBuffer msg) {
		socket.close();
	}

	private void clientRegister(final ByteBuffer msg) {
		socket.close();
	}
}
