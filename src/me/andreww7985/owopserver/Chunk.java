package me.andreww7985.owopserver;

public class Chunk {
	private final byte[] pixels;
	private final int x, y;
	private Boolean changed = false;

	public Chunk(final byte[] pixeldata, final int x, final int y) {
		this.pixels = pixeldata;
		this.x = x;
		this.y = y;
	}
	
	public void setPixel(final int x, final int y, final int rgb) {
		pixels[3 * (y * 16 + x)] = (byte) (rgb & 0xFF);
		pixels[3 * (y * 16 + x) + 1] = (byte) (rgb >> 8 & 0xFF);
		pixels[3 * (y * 16 + x) + 2] = (byte) (rgb >> 16 & 0xFF);
		changed = true;
	}

	public int getPixel(final int x, final int y) {
		int rgb = ((pixels[3 * (y * 16 + x)]) >>> 0 & 0xFF);
		rgb |= ((pixels[3 * (y * 16 + x) + 1]) >>> 0 & 0xFF) << 8;
		rgb |= ((pixels[3 * (y * 16 + x) + 2]) >>> 0 & 0xFF) << 16;
		rgb &= 0xFFFFFF;
		return rgb;
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
		Boolean should = changed;
		/* We'll just assume whoever is calling this will save */
		changed = false;
		return should;
	}
}