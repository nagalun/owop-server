package me.andreww7985.owopserver.network.states;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import me.andreww7985.owopserver.game.World;
import me.andreww7985.owopserver.network.LoginInfo;
import me.andreww7985.owopserver.network.NetworkState;
import me.andreww7985.owopserver.network.StateId;
import me.andreww7985.owopserver.server.LogManager;
import me.andreww7985.owopserver.server.WorldManager;
import me.nagalun.jwebsockets.WebSocket;

public class LobbyState extends NetworkState {
	/* Client opcodes */
	private final static byte COP_JOIN_WORLD = 0x01;
	private final static byte COP_LOG_OUT = 0x02;

	/* Server opcodes */
	private final static byte SOP_PLAYERCOUNT = 0x01; /* Server wide player (in-world) count */
	private final static byte SOP_MOTD = 0x02; /* String */
	private final static byte SOP_SET_WORLD = 0x03;

	private final LogManager log = LogManager.getInstance();
	private final LoginInfo loginInfo;
	
	private final int playerCountTimer;

	protected LobbyState(final WebSocket socket, final LoginInfo info) { /* TODO: World manager */
		super(socket, StateId.LOBBY);
		this.loginInfo = info;
		sendMOTD();
		sendPlayerCount();
		this.playerCountTimer = server.getTaskScheduler().setInterval(() -> sendPlayerCount(), 60000);
	}
	
	private void sendPlayerCount() {
		final ByteBuffer buf = ByteBuffer.allocate(5);
		buf.put(SOP_PLAYERCOUNT);
		buf.putInt(server.getTotalOnline());
		buf.flip();
		
		socket.send(buf);
	}
	
	private void sendMOTD() {
		final byte[] text = server.getMOTD().getBytes(StandardCharsets.UTF_8);
		final byte[] packet = new byte[1 + text.length];
		packet[0] = SOP_MOTD;
		System.arraycopy(text, 0, packet, 1, text.length);
		
		socket.send(packet);
	}

	protected void upgrade(final World world) {
		server.getTaskScheduler().clear(playerCountTimer);
		socket.userData = new PlayState(socket, world, loginInfo);
	}

	protected void downgrade() { /* Logged out */
		server.getTaskScheduler().clear(playerCountTimer);
		socket.userData = new LoginState(socket);
	}

	@Override
	public void processMessage(final ByteBuffer msg) {
		switch (msg.get()) {
		case COP_JOIN_WORLD:
			clientJoinWorld(msg);
			break;

		case COP_LOG_OUT:
			downgrade();
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
		loginInfo.logout();
	}

	public void clientJoinWorld(final ByteBuffer msg) {
		if (WorldManager.isWorldNameValid(msg)) {
			final String worldName = StandardCharsets.ISO_8859_1.decode(msg).toString();
			final World world = server.getWorldManager().getWorld(worldName);
			
			final byte[] packet = new byte[1 + msg.remaining()];
			packet[0] = SOP_SET_WORLD;
			msg.put(packet);
			socket.send(packet);
			
			upgrade(world);
		} else {
			log.warn("World name verification failed for " + loginInfo + " (" + socket.getRemoteSocketAddress() + ")");
			socket.close();
		}
	}
	
	public void clientLogOut(final ByteBuffer msg) {
		loginInfo.logout();
		if (!msg.hasRemaining()) {
			/* NOTE: logout might be called again, on socketDisconnect */
			socket.close();
			return;
		}
		downgrade();
	}
}
