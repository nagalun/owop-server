package me.andreww7985.owopserver.tool;

public abstract class Tool {
	private static final Class<?>[] TOOL_CLASSES = {};

	public static void use() {
		// You must override this method because Java doesn't allow static
		// abstract methods. :C
	}

	@SuppressWarnings("unchecked")
	public static Class<? extends Tool> getToolClass(final int id) {
		return (Class<? extends Tool>) TOOL_CLASSES[id];
	}
}
