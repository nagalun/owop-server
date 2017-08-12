package me.andreww7985.owopserver.tool;

import me.andreww7985.owopserver.game.Player;
import me.andreww7985.owopserver.game.World;

public abstract class Tool {
	private static final Class<?>[] TOOL_CLASSES = {};

	public static void use(final Player player, final World world, final int x, final int y, final short color) {
		// You must override this method because Java doesn't allow static
		// abstract methods. :C
	}

	@SuppressWarnings("unchecked")
	public static Class<? extends Tool> getToolClass(final int id) {
		return (Class<? extends Tool>) TOOL_CLASSES[id];
	}

	public static boolean isAdminOnly() {
		return false;
	}
}
