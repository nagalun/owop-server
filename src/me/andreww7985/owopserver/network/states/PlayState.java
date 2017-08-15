package me.andreww7985.owopserver.network.states;

import java.nio.ByteBuffer;

import me.andreww7985.owopserver.game.Player;
import me.andreww7985.owopserver.network.NetworkState;
import me.andreww7985.owopserver.network.StateId;
import me.nagalun.jwebsockets.WebSocket;

public class PlayState extends NetworkState {
	private Player player;

	protected PlayState(final WebSocket socket, final Player player) {
		super(socket, StateId.PLAY);
		this.player = player;
	}

	@Override
	protected void upgrade() {
		/* No more states */
	}

	@Override
	protected void downgrade() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processMessage(ByteBuffer msg) {
		
	}

	@Override
	public void processMessage(String msg) {
		// TODO Auto-generated method stub

	}

}
