package de.engehausen.mb.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import de.engehausen.mb.FrameData;

/**
 * A component for showing a preview of a zoom frame of the Mandelbrot set.
 * This is used as the common base for both thumbnails and a full frame view.
 */
public class Preview extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final Color HIGHLIGHT = new Color(224, 224, 224, 64);
	private static final Color HIGHLIGHT_BORDER = new Color(16, 16, 16, 64);

	protected final FrameData frameData;
	protected boolean selected;
	protected BufferedImage image;

	/**
	 * Creates the component.
	 * @param frameData the data describing the area of the Mandelbrot set to render.
	 */
	public Preview(final FrameData frameData) {
		this.frameData = frameData;
	}


	@Override
	protected void paintComponent(final Graphics graphics) {
		super.paintComponent(graphics);
		if (image != null) {
			final int width = getWidth();
			final int height = getHeight();
			graphics.drawImage(image, 0, 0, width, height, null);
			if (selected) {
				graphics.setColor(HIGHLIGHT);
				graphics.fillRect(0, 0, width, height);
				graphics.setColor(HIGHLIGHT_BORDER);
				graphics.drawRect(0, 0, width - 1, height - 1);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension getPreferredSize() {
		return frameData.dimension;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension getMaximumSize() {
		return frameData.dimension;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Dimension getMinimumSize() {
		return frameData.dimension;
	}

}
