package me.andreww7985.owopserver.network;

import java.nio.ByteBuffer;

import me.nagalun.jwebsockets.WebSocket;

public abstract class NetworkState {
	protected final WebSocket socket;
	
	protected NetworkState(final WebSocket socket) {
		this.socket = socket;
	}
	
	protected abstract void onUpgrade();
	protected abstract void upgrade();
	
	public abstract void processMessage(final ByteBuffer msg);
	public abstract void processMessage(final String msg);
}
