package me.andreww7985.owopserver.network;

public enum ServerOpcode {
	/* General opcodes */
	SWITCHING_NETSTATE((byte) 0x00),
	
	/* Verification specific opcodes */
	CAPTCHA_REQUIRED((byte) 0x01),
	CAPTCHA_STATUS((byte) 0x02),
	
	/* Login specific opcodes */
	LOGIN_INFO((byte) 0x01), /* Sets user name */
	LOGIN_STATUS((byte) 0x02),
	
	/* Lobby specific opcodes */
	SET_INFO((byte) 0x01), /* ID, world name, permissions */
	
	/* Play specific opcodes */
	LOAD_CHUNK((byte) 0x01),
	UNLOAD_CHUNK((byte) 0x02),
	CLIENT_SYNC((byte) 0x03), /* Includes client state: tool selected, position x/y, permissions, ... */
	ACTION_REJECTED((byte) 0x04), /* Over pixel limit */
	WORLD_STATE((byte) 0x05);
	
	
	public final byte code;

	private ServerOpcode(final byte code) {
		this.code = code;
	}
}
