package me.andreww7985.owopserver.server;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

/* Reads worlds of the c++ server */
public class WorldReader {
	final static String worlddir = "chunkdata";
	final String worldname;

	public WorldReader(final String worldname) {
		this.worldname = worldname;
		(new File(worlddir + File.separator + worldname)).mkdirs();
	}

	private FileChannel getFile(final int regionX, final int regionY, final Boolean createFileIfNonExistant) {
		final String filename = "r." + regionX + "." + regionY + ".pxr";
		final Path path = Paths.get(worlddir, worldname, filename);
		FileChannel fc = null;
		try {
			if (createFileIfNonExistant) {
				fc = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE,
						StandardOpenOption.CREATE);
				if (fc.size() < 3072) {
					/* Dangerous? */
					fc.write(ByteBuffer.allocate(1), 3071);
				}
			} else {
				fc = FileChannel.open(path, StandardOpenOption.READ, StandardOpenOption.WRITE);
			}
		} catch (final IOException e) {
			/*
			 * Do not log exceptions here.
			 *
			 * TODO: maybe cache files that were not found, to prevent opening
			 * them again until this gets called with createFileIfNonExistant
			 **/
			return null;
		}
		return fc;
	}

	public void writeChunk(final Chunk chunk, final int x, final int y) {
		final FileChannel fc = this.getFile(x >> 5, y >> 5, true);
		final byte[] pixels = chunk.getByteArray();
		if (fc == null) {
			OWOPServer.getInstance().getLogger().err("Could not save chunk at: " + x + ", " + y + "!");
			return;
		}
		final long lookup = 3 * ((x & 31) + (y & 31) * 32);
		final ByteBuffer bb = ByteBuffer.allocate(3);
		final ByteBuffer pixelsbuf = ByteBuffer.wrap(pixels);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		pixelsbuf.order(ByteOrder.LITTLE_ENDIAN);
		try {
			fc.read(bb, lookup);
			long chunkPos = bb.getShort(0) << 8 | bb.get(2);
			if (chunkPos != 0) {
				fc.write(pixelsbuf, chunkPos);
			} else {
				chunkPos = fc.size();
				/* Writing to bb doesn't seem to work (?) */
				final ByteBuffer bf = ByteBuffer.allocate(3);
				bf.order(ByteOrder.LITTLE_ENDIAN);
				bf.putShort(0, (short) (chunkPos >> 8));
				bf.put(2, (byte) (chunkPos & 0xFF));
				fc.write(bf, lookup);
				fc.write(pixelsbuf, chunkPos);
			}
			fc.close();
		} catch (final IOException e) {
			OWOPServer.getInstance().getLogger().err("Could not save chunk at: " + x + ", " + y + "!");
			OWOPServer.getInstance().getLogger().exception(e);
		}
	}

	public Chunk readChunk(final int x, final int y) {
		final FileChannel fc = this.getFile(x >> 5, y >> 5, false);
		final byte[] pixels = new byte[16 * 16 * 3];
		if (fc == null) {
			Arrays.fill(pixels, (byte) 255);
			return new Chunk(pixels, x, y);
		}
		final long lookup = 3 * ((x & 31) + (y & 31) * 32);
		final ByteBuffer bb = ByteBuffer.wrap(pixels, 0, 3);
		final ByteBuffer pixelsbuf = ByteBuffer.wrap(pixels);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		pixelsbuf.order(ByteOrder.LITTLE_ENDIAN);
		try {
			fc.read(bb, lookup);
			final int chunkPos = bb.getShort(0) << 8 | bb.get(2);
			if (chunkPos != 0) {
				fc.read(pixelsbuf, chunkPos);
			} else {
				Arrays.fill(pixels, (byte) 255);
			}
			fc.close();
		} catch (final IOException e) {
			OWOPServer.getInstance().getLogger().exception(e);
			Arrays.fill(pixels, (byte) 255);
		}
		return new Chunk(pixels, x, y);
	}
}
