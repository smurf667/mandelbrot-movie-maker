package de.engehausen.mb;

import java.awt.Dimension;
import java.util.StringTokenizer;

import de.engehausen.mb.math.Number;

/**
 * Describes a Mandelbrot zoom frame.
 */
public class FrameData {

	/** dimension of the frame */
	public Dimension dimension;
	/** top left corner (complex number) */
	public Number topLeft;
	/** zoom scale */
	public double scale;
	/** color offset */
	public int frameOffset;

	/**
	 * Parses a string representation into frame data.
	 * @param string a string previously generated via {@link #toString(FrameData)}.
	 * @return the frame data
	 */
	public static FrameData parseFrameData(final String string) {
		final StringTokenizer tokenizer = new StringTokenizer(string, ",");
		final Dimension dimension = new Dimension(
			Integer.parseInt(tokenizer.nextToken()),
			Integer.parseInt(tokenizer.nextToken())
		);
		final Number topLeft = new Number(
			Double.parseDouble(tokenizer.nextToken()),
			Double.parseDouble(tokenizer.nextToken())
		);
		final double scale = Double.parseDouble(tokenizer.nextToken());
		return new FrameData(dimension, topLeft, scale, Integer.parseInt(tokenizer.nextToken()));
	}

	/**
	 * Serializes the given frame data into a string representation.
	 * @param frameData the data to describe as a string.
	 * @return the data described as a string
	 */
	public static String toString(final FrameData frameData) {
		final StringBuilder builder = new StringBuilder(64);
		builder
			.append(frameData.dimension.width).append(',')
			.append(frameData.dimension.height).append(',')
			.append(frameData.topLeft.getReal()).append(',')
			.append(frameData.topLeft.getImaginary()).append(',')
			.append(frameData.scale).append(',')
			.append(frameData.frameOffset);
		return builder.toString();
	}

	/**
	 * Creates a new frame data instance.
	 * @param dimension the frame width and height
	 * @param topLeft the top left corner
	 * @param scale the zoom scale
	 * @param frameOffset the color offset
	 */
	public FrameData(final Dimension dimension, final Number topLeft, final double scale, final int frameOffset) {
		this.dimension = dimension;
		this.topLeft = topLeft;
		this.scale = scale;
		this.frameOffset = frameOffset;
	}

	/**
	 * Creates frame data with the values of the given source.
	 * @param data the source data
	 */
	public FrameData(final FrameData data) {
		this.dimension = data.dimension;
		this.topLeft = new Number(data.topLeft);
		this.scale = data.scale;
		this.frameOffset = data.frameOffset;
	}

}
