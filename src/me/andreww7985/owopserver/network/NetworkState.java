package me.andreww7985.owopserver.network;

import java.nio.ByteBuffer;

import me.andreww7985.owopserver.network.LoginInfo.Rank;
import me.andreww7985.owopserver.network.states.VerificationState;
import me.andreww7985.owopserver.server.OWOPServer;
import me.nagalun.jwebsockets.WebSocket;

public abstract class NetworkState {
	protected final static byte PROTOCOL_VERSION = 0x01;

	protected static OWOPServer server;

	protected final WebSocket socket;
	protected final StateId stateId;

	/* General opcodes (available any time, server only) */
	private final static byte SWITCHING_NETSTATE = 0x00;

	protected NetworkState(final WebSocket socket, final StateId stateId) {
		this.socket = socket;
		this.stateId = stateId;
		sendStateId();
	}

	public static void init(final OWOPServer server) {
		NetworkState.server = server;
	}

	private void sendStateId() {
		socket.send(new byte[] { SWITCHING_NETSTATE, stateId.code });
	}

	public abstract void processMessage(final ByteBuffer msg);

	public abstract void processMessage(final String msg);

	public abstract void socketDisconnected();
	
	public Rank getRank() {
		return Rank.NOBODY;
	}

	public final static NetworkState getInitialConnectionState(final WebSocket socket) {
		return new VerificationState(socket);
	}
}
