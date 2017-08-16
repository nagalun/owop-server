package me.andreww7985.owopserver.packet;

public class PacketTeleport extends Packet {
	public PacketTeleport(final int pixelX, final int pixelY) {
		super((byte) 0x04, 8);
		putInt(pixelX);
		putInt(pixelY);
	}
}
