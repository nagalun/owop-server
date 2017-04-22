package me.andreww7985.owopserver;

public class PlayerUpdate {
	public final int x, y, rgb, id;
	public final short tool;

	public PlayerUpdate(final int x, final int y, final int rgb, final short tool, final int id) {
		this.x = x;
		this.y = y;
		this.rgb = rgb;
		this.tool = tool;
		this.id = id;
	}
}
