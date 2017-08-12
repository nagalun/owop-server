package me.andreww7985.owopserver.tool;

import me.andreww7985.owopserver.game.Player;
import me.andreww7985.owopserver.game.World;

public class PaintTool extends Tool {
	public static void use(final Player player, final World world, final int x, final int y, final short color) {
		world.putPixel(x, y, color);
	}

	public static boolean isAdminOnly() {
		return false;
	}
}
