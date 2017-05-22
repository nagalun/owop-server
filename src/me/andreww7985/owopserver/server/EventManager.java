package me.andreww7985.owopserver.server;

import java.lang.reflect.Method;
import java.util.ArrayList;

import me.andreww7985.owopserver.event.Event;
import me.andreww7985.owopserver.plugin.OWOPPlugin;

public class EventManager {
	private final ArrayList<OWOPPlugin> listeners = new ArrayList<OWOPPlugin>();

	public void registerEventListener(final OWOPPlugin listener) {
		listeners.add(listener);
	}

	public void callEvent(final Event event) {
		listeners.forEach(listener -> {
			for (final Method method : listener.getClass().getDeclaredMethods()) {
				final Class<?>[] parameters = method.getParameterTypes();
				if (parameters.length == 1 && parameters[0].equals(event.getClass())) {
					try {
						method.invoke(listener, event);
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
}
