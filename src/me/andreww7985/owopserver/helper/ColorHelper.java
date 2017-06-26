package me.andreww7985.owopserver.helper;

public class ColorHelper {
	public static short toRGB565(final byte r8, final byte g8, final byte b8) {
		final byte r5 = (byte) (31.0f * (r8 & 0xFF) / 255.0f + 0.5f);
		final byte g6 = (byte) (63.0f * (g8 & 0xFF) / 255.0f + 0.5f);
		final byte b5 = (byte) (31.0f * (b8 & 0xFF) / 255.0f + 0.5f);
		return (short) (((r5) << 11) | ((g6) << 5) | (b5));
	}

	public static byte[] toRGB888(final short rgb565) {
		final byte r8 = (byte) (((rgb565 >> 11) & 0x1F) * 255.0f / 31.0f + 0.5f);
		final byte g8 = (byte) (((rgb565 >> 5) & 0x3F) * 255.0f / 63.0f + 0.5f);
		final byte b8 = (byte) ((rgb565 & 0x1F) * 255.0f / 31.0f + 0.5f);
		return new byte[] { r8, g8, b8 };
	}
}
