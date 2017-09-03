package me.andreww7985.owopserver.network.states;

import java.nio.ByteBuffer;

import me.andreww7985.owopserver.game.Player;
import me.andreww7985.owopserver.game.World;
import me.andreww7985.owopserver.network.LoginInfo;
import me.andreww7985.owopserver.network.NetworkState;
import me.andreww7985.owopserver.network.StateId;
import me.nagalun.jwebsockets.WebSocket;

public class PlayState extends NetworkState {
	/* Client opcodes */
	private final static byte COP_LOAD_CHUNK = 0x01;
	private final static byte COP_POS_UPDATE = 0x02;
	private final static byte COP_SET_TOOL = 0x03;
	private final static byte COP_USE_TOOL = 0x04; /* Packet content varies depending on tool */
	private final static byte COP_CHAT = 0x05;
	private final static byte COP_EXIT_WORLD = 0x06;

	/* Server opcodes */
	private final static byte SOP_SET_ID = 0x01;
	private final static byte SOP_LOAD_CHUNK = 0x02;
	private final static byte SOP_UNLOAD_CHUNK = 0x03;
	private final static byte SOP_CLIENT_SYNC = 0x04; /* Includes client state: tool selected, position x/y, permissions, ... */
	private final static byte SOP_ACTION_REJECTED = 0x05; /* Over pixel limit, tool not allowed, etc. */
	private final static byte SOP_WORLD_STATE = 0x06;
	private final static byte SOP_CHAT = 0x07;
	private final static byte SOP_DEV_CHAT = 0x08;

	private final Player player;

	protected PlayState(final WebSocket socket, final World world, final LoginInfo loginInfo) {
		super(socket, StateId.PLAY);
		this.player = new Player(world.getNextID(), world, loginInfo, socket);
		world.playerJoined(player);
	}

	protected void downgrade() {
		socket.userData = new LobbyState(socket, player.getLoginInfo());
	}

	@Override
	public void processMessage(final ByteBuffer msg) {
		switch (msg.get()) {
		case COP_LOAD_CHUNK:
			break;

		case COP_POS_UPDATE:
			break;

		case COP_SET_TOOL:
			break;

		case COP_USE_TOOL:
			break;

		case COP_CHAT:
			break;

		case COP_EXIT_WORLD:
			leaveWorld();
			downgrade();
			break;

		default:
			socket.close();
		}
	}

	@Override
	public void processMessage(String msg) {

	}

	private void leaveWorld() {
		final World world = player.getWorld();
		world.playerLeft(player);
		if (world.getOnline() == 0) {
			server.getWorldManager().unloadWorld(world);
		}
	}

	@Override
	public void socketDisconnected() {
		leaveWorld();
	}

}
