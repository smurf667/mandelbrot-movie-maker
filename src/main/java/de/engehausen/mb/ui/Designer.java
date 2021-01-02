package de.engehausen.mb.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.engehausen.mb.FrameData;
import de.engehausen.mb.Messages;
import de.engehausen.mb.PngSupport;
import de.engehausen.mb.math.MandelbrotSet;
import de.engehausen.mb.math.Number;

/**
 * User interface to display frames and to zoom in.
 */
public class Designer extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private final JMenuItem saveFrame;
	private final JMenuItem renderMovie;
	private final JMenuItem help;
	private final JMenuItem exit;

	protected final FramePreview preview;
	protected final DefaultListModel<Preview> thumbnails;
	protected final MandelbrotSet mandelbrot;

	private final double aspectRatio;

	/**
	 * Creates the designer UI. This has a horrible constructor. Thanks, Swing!
	 * @param frameData frame data for the initial Mandelbrot set view
	 * @param colors the RGB colors used for rendering
	 */
	public Designer(final FrameData frameData, final int... colors) {
		super(Messages.getString("title")); //$NON-NLS-1$
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setResizable(false);

		mandelbrot = new MandelbrotSet();
		aspectRatio = frameData.dimension.getHeight() / frameData.dimension.getWidth();

		saveFrame = new JMenuItem(Messages.getString("save.frame")); //$NON-NLS-1$
		saveFrame.setMnemonic(Messages.getMnemonic("save.frame")); //$NON-NLS-1$
		saveFrame.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		saveFrame.addActionListener(this);
		renderMovie = new JMenuItem(Messages.getString("render.movie")); //$NON-NLS-1$
		renderMovie.setMnemonic(Messages.getMnemonic("render.movie")); //$NON-NLS-1$
		renderMovie.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
		renderMovie.addActionListener(this);
		help = new JMenuItem(Messages.getString("help.title")); //$NON-NLS-1$
		help.setMnemonic(Messages.getMnemonic("help.title")); //$NON-NLS-1$
		help.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, ActionEvent.CTRL_MASK));
		help.addActionListener(this);
		exit = new JMenuItem(Messages.getString("exit")); //$NON-NLS-1$
		exit.setMnemonic(Messages.getMnemonic("exit")); //$NON-NLS-1$
		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
		exit.addActionListener(this);
		final JMenuBar menuBar = new JMenuBar();
		final JMenu fileMenu = new JMenu(Messages.getString("file")); //$NON-NLS-1$
		fileMenu.setMnemonic(Messages.getMnemonic("file")); //$NON-NLS-1$
		fileMenu.add(saveFrame);
		fileMenu.add(renderMovie);
		fileMenu.add(exit);
		menuBar.add(fileMenu);
		final JMenu helpMenu = new JMenu(Messages.getString("help")); //$NON-NLS-1$
		helpMenu.setMnemonic(Messages.getMnemonic("help.title")); //$NON-NLS-1$
		helpMenu.add(help);
		menuBar.add(helpMenu);
		setJMenuBar(menuBar);

		preview = new FramePreview(frameData, mandelbrot, colors);
		preview.setFocusable(true);
		final Designer parent = this;
		final MouseAdapter mouseListener = new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent event) {
				preview.requestFocus();
				if (!SwingUtilities.isRightMouseButton(event)) {
					preview.setPoint(event.getPoint());
				}
			}
			@Override
			public void mouseReleased(final MouseEvent event) {
				if (SwingUtilities.isRightMouseButton(event)) {
					preview.reframe(parent);
				} else {
					final Point origin = event.getPoint();
					if (preview.selection[0] == origin.x &&
						preview.selection[1] == origin.y) {
						Arrays.fill(preview.selection, 0);
						preview.repaint();
					}
				}
			}
			@Override
			public void mouseDragged(final MouseEvent event) {
				parent.getFramePreview().changeArea(event.getPoint(), parent.getAspectRatio());
			}
		};
		preview.addMouseListener(mouseListener);
		preview.addMouseMotionListener(mouseListener);
		final KeyAdapter keyListener = new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent event) {
				if (preview.selection[0] + preview.selection[1] > 0) {
					int component = -1;
					int step = 0;
					
					switch (event.getKeyCode()) {
						case KeyEvent.VK_UP:
							component = 1;
							step = -2;
							break;
						case KeyEvent.VK_DOWN:
							component = 1;
							step = 2;
							break;
						case KeyEvent.VK_LEFT:
							component = 0;
							step = -2;
							break;
						case KeyEvent.VK_RIGHT:
							component = 0;
							step = 2;
							break;
						case KeyEvent.VK_ENTER:
							preview.reframe(parent);
							break;
						default:
							// do nothing
							break;
					}
					if (component >= 0) {
						preview.selection[component] += step;
						preview.selection[component+2] += step;
						preview.repaint();
					}
				}
			}
		};
		preview.addKeyListener(keyListener);
		final JPanel root = new JPanel();
		root.setLayout(new BorderLayout());
		root.add(preview, BorderLayout.CENTER);

		thumbnails = new DefaultListModel<>();
		final JList<Preview> previews = new JList<>(thumbnails);
		previews.setVisibleRowCount(1);
		previews.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		previews.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;
			@Override
			public Component getListCellRendererComponent(final JList<?> list, final Object value, final int index, final boolean isSelected, final boolean cellHasFocus) {
				final Preview item = (Preview) thumbnails.get(index);
				item.selected = isSelected | cellHasFocus;
				return item;
			}
		});
		previews.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.VK_DELETE) {
					final int idx = previews.getSelectedIndex();
					if (idx > 0) {
						for (int i = thumbnails.size(); --i >= idx; ) {
							thumbnails.remove(i);
						}
						getRenderMovie().setEnabled(thumbnails.size() > 1);
						final Preview thumb = thumbnails.getElementAt(idx-1);
						previews.setSelectedIndex(idx-1);
						SwingUtilities.invokeLater(() -> {
							preview.frameData.topLeft = new Number(thumb.frameData.topLeft);
							preview.frameData.scale = thumb.frameData.scale;
							preview.frameData.frameOffset = thumb.frameData.frameOffset;
							preview.image = mandelbrot.render(preview.frameData, colors);
							preview.repaint();
						});
					}
				}
			}
		});
		createThumbnail(preview);
		root.add(new JScrollPane(previews, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS), BorderLayout.SOUTH);

		// add center
		getRootPane().getContentPane().add(root);
		pack();
	}

	/**
	 * Returns the frames for each zoom step.
	 * @return the frames for each zoom step.
	 */
	public List<FrameData> getFrameDataList() {
		final int max = thumbnails.getSize();
		final List<FrameData> result = new ArrayList<FrameData>(max);
		for (int i = 0; i < max; i++) {
			result.add(thumbnails.getElementAt(i).frameData);
		}
		
		return result;
	}

	/**
	 * Returns the RGB colors used for rendering.
	 * @return the RGB colors used for rendering.
	 */
	public int[] getColors() {
		return preview.colors;
	}

	/**
	 * Returns the Mandelbrot set used to render frames.
	 * @return the Mandelbrot set used to render frames.
	 */
	public MandelbrotSet getMandelbrotSet() {
		return mandelbrot;
	}

	protected final void createThumbnail(final FramePreview frame) {
		final BufferedImage original = frame.image;
		final double scale = 128d / original.getWidth();
		final double relative = original.getHeight() / (double) original.getWidth();
		final int thumbHeight = (int) (128*relative);
		final Dimension thumbDimension = new Dimension(128, thumbHeight);
		final Preview result = new Preview(new FrameData(thumbDimension, new Number(frame.frameData.topLeft), frame.frameData.scale, frame.frameData.frameOffset));
		final BufferedImage image = new BufferedImage(128, thumbHeight, BufferedImage.TYPE_INT_RGB);
		result.image = image;
		final Graphics2D g2d = image.createGraphics();
		try {
			final AffineTransform transform = AffineTransform.getTranslateInstance(0, 0);
			transform.scale(scale, scale);
			g2d.drawRenderedImage(original, transform);
		} finally {
			g2d.dispose();
		}
		thumbnails.addElement(result);
		renderMovie.setEnabled(thumbnails.size() > 1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void actionPerformed(final ActionEvent event) {
		final Object source = event.getSource();
		if (exit.equals(source)) {
			setVisible(false);
			System.exit(0);
//			dispose();
		} else if (saveFrame.equals(source)) {
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileFilter(new FileNameExtensionFilter(Messages.getString("image.format"), "png")); //$NON-NLS-1$ //$NON-NLS-2$
			if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				final File file = fileChooser.getSelectedFile();
				final PngSupport.MandelbrotMetaData metaData = new PngSupport.MandelbrotMetaData();
				metaData.frameData = getFramePreview().frameData;
				metaData.colors = getColors();
				SwingUtilities.invokeLater(() -> {
					try {
						new PngSupport().writeImageWithMetaData(preview.image, file, metaData);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}
		} else if (renderMovie.equals(source)) {
			final JDialog dialog = new JDialog((Frame) null, Messages.getString("save.movie"), true); //$NON-NLS-1$
			dialog.setModal(true);
			dialog.setResizable(true);
			dialog.getContentPane().add(new MovieDialog(dialog, this));
			dialog.pack();
			final Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
			dialog.setLocation(Double.valueOf(size.getWidth()/2 - dialog.getWidth()/2).intValue(), Double.valueOf(size.getHeight()/2 - dialog.getHeight()/2).intValue());
			SwingUtilities.invokeLater(() -> { dialog.setVisible(true); });
		} else if (help.equals(source)) {
			final JTextPane text = new JTextPane();
			final Dimension helpSize = new Dimension(512, 384);
			text.setPreferredSize(helpSize);
			text.setMaximumSize(helpSize);
			text.setContentType("text/html"); //$NON-NLS-1$
			final String language = Messages.getString("language"); //$NON-NLS-1$
			try (
				InputStream stream = getClass().getResourceAsStream("/help/" + language + ".html"); //$NON-NLS-1$ //$NON-NLS-2$
				final Scanner scanner = new Scanner(stream).useDelimiter("\\A"); //$NON-NLS-1$
			) {
				text.setText(scanner.hasNext() ? scanner.next() : ""); //$NON-NLS-1$
			} catch (IOException e) {
				e.printStackTrace();
			}
			text.setCaretPosition(0);
			text.setEditable(false);
			JOptionPane.showMessageDialog(this, new JScrollPane(text), Messages.getString("help"), JOptionPane.PLAIN_MESSAGE); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the frame preview component.
	 * @return the frame preview component.
	 */
	public FramePreview getFramePreview() {
		return preview;
	}

	protected JMenuItem getRenderMovie() {
		return renderMovie;
	}

	protected double getAspectRatio() {
		return aspectRatio;
	}

}
