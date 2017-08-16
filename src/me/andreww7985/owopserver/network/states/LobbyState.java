package me.andreww7985.owopserver.network.states;

import java.nio.ByteBuffer;

import me.andreww7985.owopserver.network.LoginInfo;
import me.andreww7985.owopserver.network.NetworkState;
import me.andreww7985.owopserver.network.StateId;
import me.nagalun.jwebsockets.WebSocket;

public class LobbyState extends NetworkState {
	/* Client opcodes */
	private final static byte COP_GET_WORLD = 0x01;
	
	/* Server opcodes */
	private final static byte SOP_PLAYERCOUNT = 0x01; /* Server wide player (in-world) count */
	private final static byte SOP_MOTD = 0x02; /* String */
	private final static byte SOP_SET_WORLD = 0x03;
	
	private final LoginInfo loginInfo;

	protected LobbyState(final WebSocket socket, final LoginInfo info) { /* TODO: World manager */
		super(socket, StateId.LOBBY);
		this.loginInfo = info;
	}

	protected void upgrade() {
		socket.userData = new PlayState(socket, null);
	}

	protected void downgrade() { /* Logged out */
		socket.userData = new LoginState(socket);
	}

	@Override
	public void processMessage(final ByteBuffer msg) {
		switch (msg.get()) {
		case COP_GET_WORLD:
			break;
		}
	}

	@Override
	public void processMessage(String msg) {
		// TODO Auto-generated method stub

	}

	@Override
	public void socketDisconnected() {
		// TODO Auto-generated method stub
		
	}

}
