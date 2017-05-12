package de.engehausen.mb;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.w3c.dom.NodeList;

/**
 * Writes and reads Mandelbrot frames in PNG format.
 */
public class PngSupport {
	
	private static final String META_TEXT = "Text"; //$NON-NLS-1$
	private static final String META_VALUE = "value"; //$NON-NLS-1$
	private static final String META_KEYWORD = "keyword"; //$NON-NLS-1$
	private static final String META_TEXT_ENTRY = "TextEntry"; //$NON-NLS-1$

	private static final String KEY_COLORS = "colors"; //$NON-NLS-1$
	private static final String KEY_FRAME_DATA = "frameData"; //$NON-NLS-1$
	private static final String KEY_SHIFT_COLORS = "shift"; //$NON-NLS-1$
	private static final String KEY_ZOOM_FRAMES = "fpz"; //$NON-NLS-1$

	/**
	 * Writes the image in PNG format and attached the given meta data.
	 * @param image the image to write
	 * @param file the file to write to
	 * @param metaData the meta data
	 * @throws IOException in case of error
	 */
	public void writeImageWithMetaData(final BufferedImage image, final File file, final MandelbrotMetaData metaData) throws IOException {
		ImageIO.write(image, "png", file); //$NON-NLS-1$
		writeMetaData(file, metaData);
	}

	/**
	 * Reads the meta data for the given file.
	 * 
	 * @param file the file to read from
	 * @return the meta data
	 * @throws IOException in case of error
	 */
	public MandelbrotMetaData readMetaData(final File file) throws IOException {
		final MandelbrotMetaData result = new MandelbrotMetaData();
		try (final ImageInputStream input = ImageIO.createImageInputStream(file);) {
			final Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
			if (readers.hasNext()) {
				final ImageReader reader = readers.next();
				reader.setInput(input);
				final IIOImage image = reader.readAll(0, null);

				final IIOMetadata data = image.getMetadata();
				final IIOMetadataNode root = (IIOMetadataNode) data.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
				final NodeList entries = root.getElementsByTagName(META_TEXT_ENTRY);

				for (int i = entries.getLength(); --i >= 0; ) {
					final IIOMetadataNode node = (IIOMetadataNode) entries.item(i);
					final String key = node.getAttribute(META_KEYWORD);
					if (KEY_FRAME_DATA.equals(key)) {
						result.frameData = FrameData.parseFrameData(node.getAttribute(META_VALUE));
					} else if (KEY_COLORS.equals(key)) {
						result.colors = parseColors(node.getAttribute(META_VALUE));
					} else if (KEY_SHIFT_COLORS.equals(key)) {
						result.shiftColors = Boolean.parseBoolean(node.getAttribute(META_VALUE));
					} else if (KEY_ZOOM_FRAMES.equals(key)) {
						result.framesPerZoom = Integer.parseInt(node.getAttribute(META_VALUE), 16);
					}
				}
			} else {
				throw new IllegalStateException("no reader available for "+file); //$NON-NLS-1$
			}
		}
		if (result.incomplete()) {
			throw new IllegalStateException("no Mandelbrot meta data found in image"); //$NON-NLS-1$
		}
		return result;
	}

	protected void writeMetaData(final File file, final MandelbrotMetaData metaData) throws IOException {
		try (
			final ImageInputStream input = ImageIO.createImageInputStream(file);
			final ImageOutputStream output = ImageIO.createImageOutputStream(file)
		) {

			final Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
			if (readers.hasNext()) {
				final ImageReader reader = readers.next();
				reader.setInput(input);
				final IIOImage image = reader.readAll(0, null);

				final IIOMetadata data = image.getMetadata();
				addMetaData(data, KEY_FRAME_DATA, FrameData.toString(metaData.frameData));
				addMetaData(data, KEY_ZOOM_FRAMES, Integer.toString(metaData.framesPerZoom, 16));
				addMetaData(data, KEY_SHIFT_COLORS, Boolean.toString(metaData.shiftColors));
				addMetaData(data, KEY_COLORS, toString(metaData.colors));

				final ImageWriter writer = ImageIO.getImageWriter(reader);
				writer.setOutput(output);
				writer.write(image);
			} else {
				throw new IllegalStateException("no reader available for "+file);
			}
		}
	}

	private void addMetaData(final IIOMetadata metadata, final String key, final String value) throws IIOInvalidTreeException {
		final IIOMetadataNode textEntry = new IIOMetadataNode(META_TEXT_ENTRY);
		textEntry.setAttribute(META_KEYWORD, key);
		textEntry.setAttribute(META_VALUE, value);

		final IIOMetadataNode text = new IIOMetadataNode(META_TEXT);
		text.appendChild(textEntry);

		final IIOMetadataNode root = new IIOMetadataNode(IIOMetadataFormatImpl.standardMetadataFormatName);
		root.appendChild(text);

		metadata.mergeTree(IIOMetadataFormatImpl.standardMetadataFormatName, root);
	}

	/**
	 * Parses a string list of RGB colors into an integer array representing
	 * those colors. See {@link #toString(int...)} to generate a parseable string.
	 * @param string the string to parse into a list
	 * @return an array of RGB colors
	 */
	public static int[] parseColors(final String string) {
		final List<Integer> list = Pattern.compile(",")
			.splitAsStream(string)
			.map( str -> Integer.valueOf(str, 16) )
			.collect(Collectors.toList());
		final int[] result = new int[list.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = list.get(i).intValue();
		}
		return result;
	}

	/**
	 * Serializes an array of RGB colors into a string.
	 * @param colors the colors to serialize
	 * @return a string representing the array
	 */
	public static String toString(final int... colors) {
		final StringBuilder builder = new StringBuilder(6*colors.length);
		for (int i = 1; i <= colors.length; i++) {
			builder.append(Integer.toString(colors[i-1], 16));
			if (i < colors.length) {
				builder.append(',');
			}
		}
		return builder.toString();
	}

	/**
	 * Meta data to be stored with or read from a mandelbrot frame save to a file.
	 */
	public static class MandelbrotMetaData {

		public FrameData frameData;
		public int[] colors;
		public int framesPerZoom;
		public boolean shiftColors;

		/**
		 * Returns the color array. This method
		 * exists to avoid auto-generated method from inner/outer class access.
		 * @return an array of RGB colors.
		 */
		public int[] getColors() {
			return colors;
		}

		/**
		 * Indicates of the meta data is incomplete or not.
		 * @return {@code true} if the data is incomplete, {@code false} otherwise.
		 */
		public boolean incomplete() {
			return frameData == null || colors == null;
		}

	}

}
