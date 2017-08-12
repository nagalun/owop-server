package me.andreww7985.owopserver.network.states;

import java.nio.ByteBuffer;

import me.andreww7985.owopserver.network.NetworkState;
import me.nagalun.jwebsockets.WebSocket;

public class LoginState extends NetworkState {

	protected LoginState(WebSocket socket) {
		super(socket);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onUpgrade() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void upgrade() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processMessage(ByteBuffer msg) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processMessage(String msg) {
		// TODO Auto-generated method stub
		
	}
	
}
