package me.andreww7985.owopserver.server;

public class Chunk {
	private final byte[] pixels;
	private final int x, y;
	private Boolean changed = false;

	public Chunk(final byte[] pixeldata, final int x, final int y) {
		this.pixels = pixeldata;
		this.x = x;
		this.y = y;
	}

	public void setPixel(final int x, final int y, final int rgb565) {
		pixels[2 * (y * 256 + x)] = (byte) (rgb565 & 0xFF);
		pixels[2 * (y * 256 + x) + 1] = (byte) (rgb565 >> 8 & 0xFF);
		changed = true;
	}

	public int getPixel(final int x, final int y) {
		int rgb565 = ((pixels[2 * (y * 256 + x)]) >>> 0 & 0xFF);
		rgb565 |= ((pixels[2 * (y * 256 + x) + 1]) >>> 0 & 0xFF) << 8;
		rgb565 &= 0xFFFF;
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