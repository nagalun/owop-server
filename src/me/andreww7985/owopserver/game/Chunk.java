package me.andreww7985.owopserver.game;

public class Chunk {
	private final byte[] pixels;
	private final int x, y;
	private boolean changed = false;

	public Chunk(final byte[] pixeldata, final int x, final int y) {
		this.pixels = pixeldata;
		this.x = x;
		this.y = y;
	}

	public void putPixel(final byte x, final byte y, final short color) {
		pixels[(((y & 0xFF) << 8) + (x & 0xFF)) << 1] = (byte) color;
		pixels[((((y & 0xFF) << 8) + (x & 0xFF)) << 1) + 1] = (byte) (color >> 8);
		changed = true;
	}

	public short getPixel(final byte x, final byte y) {
		return (short) ((pixels[(((y & 0xFF) << 8) + (x & 0xFF)) << 1] & 0xFF)
				| ((pixels[((((y & 0xFF) << 8) + (x & 0xFF)) << 1) + 1] & 0xFF) << 8));
	}

	public byte[] getByteArray() {
		return pixels;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public boolean shouldSave() {
		final boolean should = changed;
		/* We'll just assume whoever is calling this will save */
		changed = false;
		return should;
	}
}