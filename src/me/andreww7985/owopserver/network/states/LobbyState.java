package me.andreww7985.owopserver.network.states;

import java.nio.ByteBuffer;

import me.andreww7985.owopserver.network.NetworkState;
import me.andreww7985.owopserver.network.StateId;
import me.nagalun.jwebsockets.WebSocket;

public class LobbyState extends NetworkState {
	
	/* Server opcodes */
	private final static byte SOP_PLAYERCOUNT = 0x01; /* Server wide player (in-world) count */
	private final static byte SOP_MOTD = 0x02; /* String */

	protected LobbyState(final WebSocket socket) { /* TODO: World manager */
		super(socket, StateId.LOBBY);

	}

	protected void upgrade() {
		socket.userData = new PlayState(socket, null);
	}

	protected void downgrade() { /* Logged out */
		socket.userData = new LoginState(socket);
	}

	@Override
	public void processMessage(ByteBuffer msg) {
		// TODO Auto-generated method stub

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
