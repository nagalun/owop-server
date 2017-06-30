package me.andreww7985.owopserver.game;

import java.io.File;
import java.nio.file.Files;

import me.andreww7985.owopserver.helper.CompressionHelper;
import me.andreww7985.owopserver.server.OWOPServer;

public class WorldReader {
	private final static String worldDir = "worldData";
	private final String worldName;

	public WorldReader(final String worldname) {
		this.worldName = worldname;
		(new File(worldDir + File.separator + worldname)).mkdirs();
	}

	public void saveChunk(final Chunk chunk) {
		final File chunkFile = new File(worldDir + File.separator + worldName + File.separator + "r." + chunk.getX()
				+ "." + chunk.getY() + ".pxr");
		try {
			Files.write(chunkFile.toPath(), CompressionHelper.compress(chunk.getByteArray()));
		} catch (final Exception e) {
			OWOPServer.getInstance().getLogManager().exception(e);
		}
	}

	public Chunk readChunk(final int x, final int y) {
		final File chunkFile = new File(
				worldDir + File.separator + worldName + File.separator + "r." + x + "." + y + ".pxr");
		try {
			return new Chunk(CompressionHelper.decompress(Files.readAllBytes(chunkFile.toPath())), x, y);
		} catch (final Exception e) {
			return new Chunk(getEmptyChunk(), x, y);
		}
	}

	private byte[] getEmptyChunk() {
		final byte[] array = new byte[256 * 256 * 2];
		array[0] = (byte) 255;
		for (int i = 1; i < 256 * 256 * 2; i += i) {
			System.arraycopy(array, 0, array, i, ((256 * 256 * 2 - i) < i) ? (256 * 256 * 2 - i) : i);
		}
		return array;
	}
}