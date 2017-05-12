package de.engehausen.mb.math;

import java.awt.image.BufferedImage;

import de.engehausen.mb.FrameData;

/**
 * Mandelbrot set. This is the core of the tool
 */
public class MandelbrotSet {

	/**
	 * Renders an Mandelbrot set image for the given input.
	 * @param frameData the frame data to use
	 * @param colors the colors to use
	 * @return the generated image
	 */
	public BufferedImage render(final FrameData frameData, final int... colors) {
		return render(frameData.topLeft, frameData.scale, frameData.dimension.width, frameData.dimension.height, frameData.frameOffset, colors);
	}

	/**
	 * Renders an Mandelbrot set image for the given input.
	 * @param topLeft the top left corner for the image (on the complex number plane)
	 * @param scale the scale (aka zoom level)
	 * @param width the width of the result image
	 * @param height the height of the result image
	 * @param offset the color offset for the input colors
	 * @param colors the RGB colors to use for rendering
	 * @return the generated image
	 */
	public BufferedImage render(final Number topLeft, final double scale, final int width, final int height, final int offset, final int... colors) {
		final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		final double steps = scale / Math.min(width, height);
		int px;
		int py = 0;
		for (double y = topLeft.getImaginary(); py < height; py++, y += steps) {
			px = 0;
			for (double x = topLeft.getReal(); px < width; px++, x += steps) {
				final Number number = new Number(0, 0);
				final Number c = new Number(x, y);
				int i = 0;
				do {
					number.multiply(number).add(c);
				} while (i++ < colors.length && number.inside());
				img.setRGB(px, py, colors[(offset+i) % colors.length]);
			}
		}
		return img;
	}

}
