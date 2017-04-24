package me.andreww7985.owopserver;

public class Chunk {
	private final byte[] pixels = new byte[16 * 16 * 3];

	public void setPixel(final int x, final int y, final int rgb) {
		/* The RGB integer looks like this: 
		 * 0x00BBGGRR
		 **/
		pixels[3 * (y * 16 + x)]     = (byte) (rgb       & 0xFF); /* R */
		pixels[3 * (y * 16 + x) + 1] = (byte) (rgb >> 8  & 0xFF); /* G */
		pixels[3 * (y * 16 + x) + 2] = (byte) (rgb >> 16 & 0xFF); /* B */
	}

	public int getPixel(final int x, final int y) {
		int rgb  = ((int)(pixels[3 * (y * 16 + x)])     >>> 0 & 0xFF);       /* R */
		    rgb |= ((int)(pixels[3 * (y * 16 + x) + 1]) >>> 0 & 0xFF) << 8;  /* G */
		    rgb |= ((int)(pixels[3 * (y * 16 + x) + 2]) >>> 0 & 0xFF) << 16; /* B */
		    rgb &= 0xFFFFFF;
		return rgb;
	}
	
	public byte[] getByteArray() {
		return pixels;
	}
}