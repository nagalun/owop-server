package me.andreww7985.owopserver;

public class Chunk {
	private final int[][] pixels = new int[16][16];

	public void setPixel(final int x, final int y, final int rgb) {
		pixels[((x % 16) + 16) % 16][((y % 16) + 16) % 16] = rgb;
	}

	public int getPixel(final int x, final int y) {
		return pixels[((x % 16) + 16) % 16][((y % 16) + 16) % 16];
	}
}
