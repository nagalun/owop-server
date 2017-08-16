package me.andreww7985.owopserver.packet;

public class PacketJoinOK extends Packet {
	public PacketJoinOK(final int id) {
		super((byte) 0x01, 4);
		putInt(id);
	}
}
