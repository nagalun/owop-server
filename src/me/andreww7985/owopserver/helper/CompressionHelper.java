package me.andreww7985.owopserver.helper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class CompressionHelper {
	private static ArrayList<Integer> repeatlocations;
	private static ArrayList<Byte> compressed;
	private static int cptr, repeats, thisClr, lastClr, i;
	private static byte[] original;

	private static void check() {
		if (++repeats >= 3) {
			repeatlocations.add(cptr & 0xFF);
			repeatlocations.add(cptr >> 8 & 0xFF);
			compressed.add(cptr++, (byte) (repeats & 0xFF));
			compressed.add(cptr++, (byte) (repeats >> 8 & 0xFF));
			compressed.add(cptr++, (byte) (lastClr & 0xFF));
			compressed.add(cptr++, (byte) (lastClr >> 8 & 0xFF));
			compressed.add(cptr++, (byte) (lastClr >> 16 & 0xFF));
		} else {
			for (int j = i - repeats * 3; j < i; j++) {
				compressed.add(cptr++, original[j]);
			}
		}
		repeats = 0;
	}

	public static byte[] compress(final byte[] original) {
		CompressionHelper.original = original;
		ByteBuffer bbuf = ByteBuffer.wrap(original);
		bbuf.order(ByteOrder.LITTLE_ENDIAN);
		repeatlocations = new ArrayList<Integer>();
		compressed = new ArrayList<Byte>();
		cptr = 0;
		lastClr = (bbuf.get(2) << 16 | bbuf.getShort(0)) & 0xFFFFFF;
		repeats = 0;
		for (i = 3; i < original.length; i += 3) {
			thisClr = (bbuf.get(i + 2) << 16 | bbuf.getShort(i)) & 0xFFFFFF;
			if (lastClr == thisClr) {
				repeats++;
			} else {
				check();
			}
			lastClr = thisClr;
		}
		check();
		final byte[] u8compressed = new byte[2 + 2 + repeatlocations.size() + compressed.size()];
		final int rllen = repeatlocations.size() / 2;
		cptr = 0;
		final int length = original.length;
		u8compressed[cptr++] = (byte) (length & 0xFF);
		u8compressed[cptr++] = (byte) (length >> 8 & 0xFF);
		u8compressed[cptr++] = (byte) (rllen & 0xFF);
		u8compressed[cptr++] = (byte) (rllen >> 8 & 0xFF);
		for (int i = 0; i < repeatlocations.size(); i++) {
			u8compressed[cptr++] = (byte) (int) repeatlocations.get(i);
		}
		for (int i = 0; i < compressed.size(); i++) {
			u8compressed[cptr++] = compressed.get(i);
		}
		return u8compressed;
	}
}
