package me.andreww7985.owopserver.packet;

public class PacketChunk extends Packet {
	public PacketChunk(final int chunkX, final int chunkY, final byte[] chunkData) {
		super((byte) 0x02, 8 + chunkData.length);
		putInt(chunkX);
		putInt(chunkY);
		putBytes(chunkData);
	}
}
