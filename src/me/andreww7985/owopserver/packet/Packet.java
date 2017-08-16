package me.andreww7985.owopserver.packet;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public abstract class Packet {
	private final byte id;
	private final int length;
	private final ByteBuffer data;

	public Packet(final byte id, final ByteBuffer bb) {
		this.id = id;
		this.length = bb.getInt();
		this.data = ByteBuffer.allocate(length + 5);
		final byte[] temp = new byte[length + 5];
		bb.get(temp, 0, length + 5);
		data.put(temp);
		data.position(5);
	}

	public Packet(final byte id, final int length) {
		this.id = id;
		this.length = length;
		this.data = ByteBuffer.allocate(length);
		data.put(id);
		data.putInt(length);
	}

	public int getID() {
		return id;
	}

	public byte[] getData() {
		return data.array();
	}

	protected byte getByte() {
		return data.get();
	}

	protected void putByte(final byte value) {
		data.put(value);
	}

	protected void putShort(final short value) {
		data.putShort(value);
	}

	protected short getShort() {
		return data.getShort();
	}

	protected int getInt() {
		return data.getInt();
	}

	protected void putInt(final int value) {
		data.putInt(value);
	}

	protected String getString() throws Exception {
		return new String(getBytes(), StandardCharsets.UTF_8);
	}

	protected void putString(final String value) {
		putBytes(value.getBytes(StandardCharsets.UTF_8));
	}

	protected void putBytes(final byte[] value) {
		putInt(value.length);
		data.put(value);
	}

	protected byte[] getBytes() {
		final int length = getInt();
		final byte[] bytes = new byte[length];
		data.get(bytes, 0, length);

		return bytes;
	}
}
