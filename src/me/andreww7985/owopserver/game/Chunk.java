package me.andreww7985.owopserver.game;

public class Chunk {
	private final byte[] pixels;
	private final int x, y;
	private Boolean changed = false;

	public Chunk(final byte[] pixeldata, final int x, final int y) {
		this.pixels = pixeldata;
		this.x = x;
		this.y = y;
	}

	public void putPixel(final byte x, final byte y, final short rgb565) {
		pixels[(((y & 0xFF) << 8) + (x & 0xFF)) << 1] = (byte) rgb565;
		pixels[((((y & 0xFF) << 8) + (x & 0xFF)) << 1) + 1] = (byte) (rgb565 >> 8);
		changed = true;
	}

	public short getPixel(final byte x, final byte y) {
		short rgb565 = (short) (pixels[(((y & 0xFF) << 8) + (x & 0xFF)) << 1] & 0xFF);
		rgb565 |= (pixels[((((y & 0xFF) << 8) + (x & 0xFF)) << 1) + 1] & 0xFF) << 8;
		return rgb565;
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

	public Boolean shouldSave() {
		final Boolean should = changed;
		/* We'll just assume whoever is calling this will save */
		changed = false;
		return should;
	}
}