package me.andreww7985.owopserver.network;

public enum StateId {
	VERIFICATION((byte) 0x00),
	LOGIN((byte) 0x01),
	LOBBY((byte) 0x02),
	PLAY((byte) 0x03);
	
	public final byte code;

	private StateId(final byte code) {
		this.code = code;
	}
}
