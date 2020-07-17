package de.engehausen.mb;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.engehausen.mb.math.MandelbrotSet;
import de.engehausen.mb.math.Number;
import de.engehausen.mb.ui.AbstractDialog;
import de.engehausen.mb.ui.Designer;

/**
 * The startup wizard for the tool.
 * This allows to define frame size, number of zoom frames and the color
 * gradient. It also supports loading a previously saved PNG which contains
 * meta data describing the image.
 */
public class Wizard extends AbstractDialog implements ActionListener, KeyListener, ChangeListener {

	private static final long serialVersionUID = 1L;

	private final JDialog dialog;
	private final JTextField textWidth;
	private final JTextField textHeight;
	private final JTextField framesPerZoom;
	private final JSlider colorCount;
	private final JSlider hueOffset;
	private final JSlider hueRange;
	private final JSlider saturationFreq;
	private final JSlider brightnessFreq;
	private final JButton load;
	private final JButton confirm;
	private final JButton cancel;
	private final Color background;
	private final GradientPreview preview;
	private final MandelbrotPreview setPreview;
	private final JCheckBox mirrorColors;
	private final JCheckBox rotateColors;

	/**
	 * Creates the wizard
	 * @param parent the parent dialog starting the wizard.
	 */
	public Wizard(final JDialog parent) {
		dialog = parent;
		setLayout(new BorderLayout());
		textWidth = new JTextField(10);
		textWidth.setText("1024"); //$NON-NLS-1$
		textWidth.addKeyListener(this);
		background = textWidth.getBackground();
		textHeight = new JTextField(10);
		textHeight.setText("576"); //$NON-NLS-1$
		textHeight.addKeyListener(this);
		framesPerZoom = new JTextField(10);
		framesPerZoom.setText("100"); //$NON-NLS-1$
		framesPerZoom.addKeyListener(this);

		colorCount = createSlider(8, 255, 128);
		hueOffset = createSlider(0, 360, 0);
		hueRange = createSlider(0, 100, 100);
		saturationFreq = createSlider(0, 100, 0);
		brightnessFreq = createSlider(0, 100, 0);
		preview = new GradientPreview();

		mirrorColors = new JCheckBox();
		mirrorColors.setSelected(true);

		rotateColors = new JCheckBox();
		rotateColors.setSelected(false);

		setPreview = new MandelbrotPreview(new Dimension(224, 128));

		stateChanged(null);

		load = new JButton(Messages.getString("load")); //$NON-NLS-1$
		load.addActionListener(this);
		confirm = new JButton(Messages.getString("go")); //$NON-NLS-1$
		confirm.addActionListener(this);
		cancel = new JButton(Messages.getString("cancel")); //$NON-NLS-1$
		cancel.addActionListener(this);

		final JPanel contentPane = new JPanel();
		final GridBagLayout gridbag = new GridBagLayout();
		contentPane.setLayout(gridbag);

		final String[] labels = {
			Messages.getString("width"), //$NON-NLS-1$
			Messages.getString("height"), //$NON-NLS-1$
			Messages.getString("frame.zoom"), //$NON-NLS-1$
			Messages.getString("hue.count"), //$NON-NLS-1$
			Messages.getString("hue.offset"), //$NON-NLS-1$
			Messages.getString("hue.range"), //$NON-NLS-1$
			Messages.getString("saturation.freq"), //$NON-NLS-1$
			Messages.getString("brightness.freq"), //$NON-NLS-1$
			Messages.getString("gradient"), //$NON-NLS-1$
			Messages.getString("seamless"), //$NON-NLS-1$
			Messages.getString("shift.colors"), //$NON-NLS-1$
			Messages.getString("preview") //$NON-NLS-1$
		};
		final JComponent[] components = {
			textWidth,
			textHeight,
			framesPerZoom,
			colorCount,
			hueOffset,
			hueRange,
			saturationFreq,
			brightnessFreq,
			preview,
			mirrorColors,
			rotateColors,
			setPreview
		};
		addRows(labels, components, gridbag, contentPane);

		final JPanel main = new JPanel();
		main.add(contentPane);
		add(main, BorderLayout.CENTER);
		final JPanel buttons = new JPanel();
		buttons.setLayout(new BorderLayout());
		final JPanel loadPanel = new JPanel();
		loadPanel.add(load);
		buttons.add(loadPanel, BorderLayout.WEST);
		final JPanel yesno = new JPanel();
		yesno.add(confirm);
		yesno.add(cancel);
		buttons.add(yesno, BorderLayout.EAST);
		add(buttons, BorderLayout.SOUTH);
	}

	/**
	 * Returns the desired width of the frames.
	 * @return the width of the frames.
	 */
	public int getImageWidth() {
		return Integer.parseInt(textWidth.getText());
	}

	/**
	 * Returns the desired height of the frames.
	 * @return the height of the frames.
	 */
	public int getImageHeight() {
		return Integer.parseInt(textHeight.getText());
	}

	/**
	 * Returns the configured color gradient.
	 * @return the color gradient.
	 */
	public int[] getColors() {
		final Color[] colors  = preview.getColors();
		final boolean seamless = mirrorColors.isSelected();
		final int max = 2 * colors.length - 1;
		final int[] rgbs = new int[seamless ? max : colors.length];
		for (int i = 0; i < colors.length; i++) {
			rgbs[i] = colors[i].getRGB();
			if (seamless && i > 0) {
				rgbs[max-i] = rgbs[i];
			}
		}
		return rgbs;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		final Object source = event.getSource();
		final boolean isConfirm = confirm.equals(source);
		if (isConfirm || cancel.equals(source)) {
			dialog.setVisible(false);
			dialog.dispose();
			if (confirm.equals(source)) {
				final int[] colors = getColors();
				final int frames = toInt(framesPerZoom);
				final FrameData frameData = new FrameData(
					new Dimension(toInt(textWidth), toInt(textHeight)),
					new Number(-2.25, -1),
					2d,
					0
				);
				SwingUtilities.invokeLater(() -> {
					new Designer(frameData, frames, rotateColors.isSelected(), colors).setVisible(true);
				});
			}
		} else if (load.equals(source)) {
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileFilter(new FileNameExtensionFilter(Messages.getString("image.format"), "png")); //$NON-NLS-1$ //$NON-NLS-2$
			if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				try {
					final PngSupport.MandelbrotMetaData metaData = new PngSupport().readMetaData(fileChooser.getSelectedFile());
					dialog.setVisible(false);
					dialog.dispose();
					SwingUtilities.invokeLater(() -> {
						new Designer(
							metaData.frameData,
							metaData.framesPerZoom,
							metaData.shiftColors,
							metaData.getColors()
						).setVisible(true);
					});
				} catch (IOException e) {
					JOptionPane.showMessageDialog(this, Messages.getString("cannot.load")); //$NON-NLS-1$
				}
			}
		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stateChanged(final ChangeEvent event) {
		preview.generate(
			colorCount.getValue(),
			hueOffset.getValue() / 360f,
			hueRange.getValue() / 100f,
			saturationFreq.getValue() / 400f,
			brightnessFreq.getValue() / 400f);
		final int[] colors = getColors();
		SwingUtilities.invokeLater(() -> setPreview.preview(colors));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void keyTyped(final KeyEvent event) {
		// ignored
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void keyPressed(final KeyEvent event) {
		// ignored
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void keyReleased(final KeyEvent event) {
		final Object source = event.getSource();
		if (source instanceof JTextField) {
			validateNumber((JTextField) source, confirm);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Color getTextBackground() {
		return background;
	}

	private JSlider createSlider(final int min, final int max, final int init) {
		final JSlider result = new JSlider(SwingConstants.HORIZONTAL, min, max, init);
		result.addChangeListener(this);
		return result;
	}

	private static class GradientPreview extends JPanel {

		private static final long serialVersionUID = 1L;
		
		private BufferedImage image;
		private Color[] colors;
		
		protected Color[] getColors() {
			return colors;
		}

		/**
		 * Creates a new image showing the current color gradient.
		 * @param count number of result colors
		 * @param offset hue offset
		 * @param maxColor maximum number of colors
		 * @param saturationFreq saturation frequency
		 * @param brightnessFreq brightness frequency
		 */
		public void generate(final int count,
			final float offset,
			final float maxColor,
			final float saturationFreq,
			final float brightnessFreq) {
			colors = new Color[count];
			final double hueStep = maxColor / count;
			float hue = offset;
			float saturation = saturationFreq;
			float brightness = brightnessFreq;
			for (int i = 0; i < count; i++) {
				colors[i] = Color.getHSBColor(hue, (float) (1 + Math.cos(saturation))/2, (float) (1 + Math.cos(brightness))/2);
				hue += hueStep;
				saturation += saturationFreq;
				brightness += brightnessFreq;
			}
			image = new BufferedImage(count, 1, BufferedImage.TYPE_INT_RGB);
			for (int i = 0; i < count; i++) {
				image.setRGB(i, 0, colors[i].getRGB());
			}
			repaint();
		}

		@Override
		protected void paintComponent(final Graphics graphics) {
			super.paintComponent(graphics);
			graphics.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
		}
	}

	/**
	 * Previews a Mandelbrot set with the current colors.
	 */
	private static class MandelbrotPreview extends JPanel {

		private static final long serialVersionUID = 1L;

		private final Dimension dimension;
		private final MandelbrotSet mandelbrot;
		private BufferedImage image;
		private final Number topLeft = new Number(-2.25, -1);
		
		public MandelbrotPreview(final Dimension size) {
			dimension = size;
			mandelbrot = new MandelbrotSet();
		}

		/**
		 * Updates the preview image.
		 * @param colors the colors to use to generate the image
		 */
		public void preview(final int... colors) {
			image = mandelbrot.render(topLeft, 2d, dimension.width, dimension.height, 0, colors);
			repaint();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Dimension getPreferredSize() {
			return dimension;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Dimension getMaximumSize() {
			return getPreferredSize();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Dimension getMinimumSize() {
			return getPreferredSize();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected void paintComponent(final Graphics graphics) {
			super.paintComponent(graphics);
			if (image != null) {
				graphics.drawImage(image, 0, 0, this.getWidth(), this.getHeight(), null);
			}
		}

	}

	/**
	 * Runs the wizard.
	 * @param args the arguments are ignored.
	 */
	public static void main(final String[] args) {
		final JDialog dialog = new JDialog((Frame) null, Messages.getString("get.started"), true); //$NON-NLS-1$
		dialog.setResizable(false);
		dialog.getContentPane().add(new Wizard(dialog));
		dialog.pack();
		final Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
		dialog.setLocation(Double.valueOf(size.getWidth()/2 - dialog.getWidth()/2).intValue(), Double.valueOf(size.getHeight()/2 - dialog.getHeight()/2).intValue());
		SwingUtilities.invokeLater(() -> dialog.setVisible(true));
	}

}
