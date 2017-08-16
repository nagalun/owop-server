package me.andreww7985.owopserver.packet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import me.andreww7985.owopserver.game.PixelUpdate;
import me.andreww7985.owopserver.game.Player;

public class PacketUpdates extends Packet {
	@SuppressWarnings("rawtypes")
	public PacketUpdates(final HashSet<Player> playerUpdates, final HashSet<PixelUpdate> pixelUpdates,
			final ArrayList<Integer> disconnects) {
		super((byte) 0x03,
				4 + (playerUpdates.size() > 255 ? 255 : playerUpdates.size()) * 15
						+ (pixelUpdates.size() > 65535 ? 65535 : pixelUpdates.size()) * 10
						+ (disconnects.size() > 255 ? 255 : disconnects.size()) * 4);
		Iterator iterator;

		putByte((byte) (playerUpdates.size() > 255 ? 255 : playerUpdates.size()));
		iterator = playerUpdates.iterator();
		Player player;
		for (byte i = 0; i < 256 && iterator.hasNext(); i++) {
			player = (Player) iterator.next();
			putInt(player.getID());
			putInt(player.getX());
			putInt(player.getY());
			putShort(player.getColor());
			putByte(player.getTool());
		}

		putByte((byte) (pixelUpdates.size() > 65535 ? 65535 : pixelUpdates.size()));
		iterator = pixelUpdates.iterator();
		PixelUpdate pixelUpdate;
		for (short i = 0; i < 65536 && iterator.hasNext(); i++) {
			pixelUpdate = (PixelUpdate) iterator.next();
			putInt(pixelUpdate.x);
			putInt(pixelUpdate.y);
			putShort(pixelUpdate.color);
		}

		putByte((byte) (pixelUpdates.size() > 255 ? 255 : pixelUpdates.size()));
		iterator = pixelUpdates.iterator();
		int id;
		for (byte i = 0; i < 256 && iterator.hasNext(); i++) {
			id = (int) iterator.next();
			putInt(id);
		}
	}
}
