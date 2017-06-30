package me.andreww7985.owopserver.helper;

import java.util.ArrayList;

import me.andreww7985.owopserver.server.OWOPServer;
import me.andreww7985.owopserver.timings.TimingsRecord;

public class CompressionHelper {
	public static byte[] compress(final byte[] original) {
		final TimingsRecord tr = TimingsRecord.start("chunkCompress");
		int compressedSize = 0;
		final ArrayList<Integer> repeatlocations = new ArrayList<Integer>();
		final ArrayList<Integer> repeattimes = new ArrayList<Integer>();
		int repeats = 1;

		int lastClr = -1;
		for (int i = 0; i < original.length; i += 2) {
			final int thisClr = original[i + 1] << 8 & 0xFF00 | original[i] & 0xFF;
			if (lastClr == thisClr) {
				repeats++;
			} else {
				if (repeats >= 3) {
					compressedSize -= (repeats - 1) * 2 - 2 - 2;
					repeatlocations.add((compressedSize - 2 - 2 - 2) / 2);
					repeattimes.add(repeats - 1); /* Will prevent overflows */
					repeats = 1;
					lastClr = thisClr;
					continue;
				}
				repeats = 1;
				lastClr = thisClr;
			}
			compressedSize += 2;
		}
		if (repeats >= 3) { /* bug possibility here */
			compressedSize -= (repeats /* - 1 */) * 2 - 2 - 2;
			repeatlocations.add((compressedSize - 2 - 2) / 2);
			repeattimes.add(repeats - 1);
		}
		/*
		 * System.out.print("S: " + compressedSize + ", NR: " +
		 * repeatlocations.size()); int s = 0; for (final int numr :
		 * repeattimes) { s += (1 + numr) * 2; s -= 2; } s += compressedSize -
		 * repeatlocations.size() * 2; System.out.println(", RS: " + s +
		 * ", OS: " + original.length);
		 */
		final byte[] u8compressed = new byte[2 + 2 + repeatlocations.size() * 2 + compressedSize];
		final int rllen = repeatlocations.size();
		final int length = original.length / 2 - 1;
		final int offset = 2 + 2 + rllen * 2;
		int cptr = 0;
		int dptr = offset;
		int optr = 0;
		u8compressed[cptr++] = (byte) (length & 0xFF);
		u8compressed[cptr++] = (byte) (length >> 8 & 0xFF);
		u8compressed[cptr++] = (byte) (rllen & 0xFF);
		u8compressed[cptr++] = (byte) (rllen >> 8 & 0xFF);
		for (int i = 0; i < rllen; i++) {
			final int loc = repeatlocations.get(i);
			final int times = repeattimes.get(i);
			u8compressed[cptr++] = (byte) (loc & 0xFF);
			u8compressed[cptr++] = (byte) (loc >> 8 & 0xFF);
			while (dptr < loc * 2 + offset) {
				u8compressed[dptr++] = original[optr++];
			}
			u8compressed[dptr++] = (byte) (times & 0xFF);
			u8compressed[dptr++] = (byte) (times >> 8 & 0xFF);
			u8compressed[dptr++] = original[optr++]; /* RG */
			u8compressed[dptr++] = original[optr++]; /* GB (565) */
			optr += (1 + times - 1) * 2;
		}
		while (optr < original.length) {
			u8compressed[dptr++] = original[optr++];
		}
		OWOPServer.getInstance().getTimingsManager().add(tr);
		return u8compressed;
	}

	public static byte[] decompress(final byte[] input) {
		final TimingsRecord tr = TimingsRecord.start("chunkDecompress");
		final int originalLength = (((input[1] & 0xFF) << 8 | (input[0] & 0xFF)) + 1) * 2;
		final byte[] output = new byte[originalLength];
		final int numOfRepeats = (input[3] & 0xFF) << 8 | (input[2] & 0xFF);
		final int offset = numOfRepeats * 2 + 4;
		int uptr = 0;
		int cptr = offset;
		for (int i = 0; i < numOfRepeats; i++) {
			final int currentRepeatLoc = 2 * ((((input[4 + i * 2 + 1] & 0xFF) << 8) | (input[4 + i * 2] & 0xFF)))
					+ offset;
			while (cptr < currentRepeatLoc) {
				output[uptr++] = input[cptr++];
			}
			int repeatedNum = ((input[cptr + 1] & 0xFF) << 8 | (input[cptr] & 0xFF)) + 1;
			final int repeatedColorRGB = (input[cptr + 3] & 0xFF) << 8 | (input[cptr + 2] & 0xFF);
			cptr += 4;
			while (repeatedNum-- != 0) {
				output[uptr] = (byte) (repeatedColorRGB & 0xFF);
				output[uptr + 1] = (byte) ((repeatedColorRGB & 0xFF00) >> 8);
				uptr += 2;
			}
		}
		while (cptr < input.length) {
			output[uptr++] = input[cptr++];
		}
		OWOPServer.getInstance().getTimingsManager().add(tr);
		return output;
	}
}
