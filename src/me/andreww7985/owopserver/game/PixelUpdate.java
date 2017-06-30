package me.andreww7985.owopserver.game;

public class PixelUpdate {
	public final int x, y;
	public final short rgb565;

	public PixelUpdate(final int x, final int y, final short rgb565) {
		this.x = x;
		this.y = y;
		this.rgb565 = rgb565;
	}
}
