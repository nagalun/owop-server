package me.andreww7985.owopserver.event;

import me.andreww7985.owopserver.server.Player;

public class PlayerMoveEvent extends Event {
	private final int fromX, fromY;
	private int toX, toY;
	private final Player player;

	public PlayerMoveEvent(final Player player, final int toX, final int toY) {
		this.player = player;
		this.fromX = player.getX();
		this.fromY = player.getY();
		this.toX = toX;
		this.toY = toY;
	}

	public int getFromX() {
		return fromX;
	}

	public int getFromY() {
		return fromY;
	}

	public int getToX() {
		return toX;
	}

	public int getToY() {
		return toY;
	}

	public void setToX(final int value) {
		toX = value;
	}

	public void setToY(final int value) {
		toY = value;
	}

	public Player getPlayer() {
		return player;
	}
}
