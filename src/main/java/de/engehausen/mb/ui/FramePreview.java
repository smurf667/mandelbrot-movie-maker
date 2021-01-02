package de.engehausen.mb.ui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.Arrays;

import javax.swing.SwingUtilities;

import de.engehausen.mb.FrameData;
import de.engehausen.mb.math.MandelbrotSet;
import de.engehausen.mb.math.Number;

/**
 * Visual component to show a preview of a Mandelbrot set frame.
 */
public class FramePreview extends Preview {

	private static final long serialVersionUID = 1L;

	protected final int[] selection = new int[4];
	
	protected final int[] colors;

	/**
	 * Creates the component.
	 * @param frameData the frame data describing the position and zoom on the Mandelbrot set.
	 * @param mandelbrot the Mandelbrot set rendering component
	 * @param colors the colors to use for rendering
	 */
	public FramePreview(final FrameData frameData, final MandelbrotSet mandelbrot, final int... colors) {
		super(frameData);
		this.colors = colors;
		image = mandelbrot.render(frameData, colors);
	}

	/**
	 * Rebuilds the image to show the selected frame.
	 * @param parentView the parent view
	 */
	public void reframe(final Designer parentView) {
		if (selection[0] + selection[1] > 0) {
			// recompute parameters
			final double min = Math.min(frameData.dimension.getWidth(), frameData.dimension.getHeight());
			
			final double minX = getMinX();
			final double minY = getMinY();
			final Number offset = new Number(frameData.scale * minX / min, frameData.scale * minY / min);
			frameData.topLeft.add(offset);
			final double percent = (getMaxX() - minX) / frameData.dimension.getWidth();
			frameData.scale *= percent;
			Arrays.fill(selection, 0);
			SwingUtilities.invokeLater(() -> {
				image = parentView.mandelbrot.render(frameData, colors);
				parentView.createThumbnail(this);
				repaint();
			});
		}
	}

	@Override
	protected void paintComponent(final Graphics graphics) {
		super.paintComponent(graphics);
		if (selection[0] != 0 || selection[1] != 0) {
			final int minx = getMinX();
			final int miny = getMinY();
			final int maxx = getMaxX();
			final int maxy = getMaxY();
			graphics.setColor(Color.WHITE);
			graphics.drawRect(minx, miny, maxx - minx, maxy - miny);
		}
	}

	protected void changeArea(final Point origin, final double aspectRatio) {
		if (selection[0] != origin.x || selection[1] != origin.y) {
			selection[2] = origin.x;
			selection[3] = selection[1]
					+ (int) (Math.signum(origin.y - selection[1]) * aspectRatio * (getMaxX() - getMinX()));
			repaint();
		}
	}

	protected void setPoint(final Point origin) {
		selection[2] =
		selection[0] = origin.x;
		selection[3] =
		selection[1] = origin.y;
	}

	protected int getMaxX() {
		return getMax(0);
	}

	protected int getMaxY() {
		return getMax(1);
	}

	protected int getMinX() {
		return getMin(0);
	}

	protected int getMinY() {
		return getMin(1);
	}

	private int getMax(final int first) {
		return Math.max(selection[first], selection[first + 2]);
	}

	private int getMin(final int first) {
		return Math.min(selection[first], selection[first + 2]);
	}

}
