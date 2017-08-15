package me.andreww7985.owopserver.network;

import java.nio.ByteBuffer;

import me.nagalun.jwebsockets.WebSocket;

public abstract class NetworkState {
	protected final static int PROTOCOL_VERSION = 0x00000000;
	
	protected final WebSocket socket;
	protected final StateId stateId;

	/* General opcodes (available any time) */
	private final static byte SWITCHING_NETSTATE = 0x00;

	protected NetworkState(final WebSocket socket, final StateId stateId) {
		this.socket = socket;
		this.stateId = stateId;
		sendStateId();
	}

	private void sendStateId() {
		socket.send(new byte[] { SWITCHING_NETSTATE, stateId.code });
	}

	public abstract void processMessage(final ByteBuffer msg);

	public abstract void processMessage(final String msg);

	public abstract void socketDisconnected();
}
