package de.engehausen.mb.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.engehausen.mb.Messages;
import de.engehausen.mb.MovieRenderer;

/**
 * Dialog for saving an MP4 movie of the zoom into a Mandelbrot set.
 */
public class MovieDialog extends AbstractDialog implements ActionListener, KeyListener {

	private static final long serialVersionUID = 1L;

	private final JDialog dialog;
	private final JTextField framesPerSecond;
	private final JTextField fileName;
	private final JButton confirm;
	private final JButton cancel;
	private final JButton choose;
	private final Color background;
	private final Designer designer;
	
	/**
	 * Creates the dialog.
	 * @param parent the parent dialog
	 * @param designer the designer UI supplying relevant information for rendering.
	 */
	public MovieDialog(final JDialog parent, final Designer designer) {
		dialog = parent;
		this.designer = designer;
		setLayout(new BorderLayout());
		framesPerSecond = new JTextField(4);
		framesPerSecond.setText("15"); //$NON-NLS-1$
		framesPerSecond.addKeyListener(this);
		background = framesPerSecond.getBackground();

		fileName = new JTextField(30);
		fileName.setText(""); //$NON-NLS-1$
		fileName.addKeyListener(this);
		
		final JPanel fileNamePanel = new JPanel();
		fileNamePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		fileNamePanel.add(fileName);
		choose = new JButton(Messages.getString("choose")); //$NON-NLS-1$
		choose.addActionListener(this);
		fileNamePanel.add(choose);

		confirm = new JButton(Messages.getString("save")); //$NON-NLS-1$
		confirm.addActionListener(this);
		confirm.setEnabled(false);
		cancel = new JButton(Messages.getString("cancel")); //$NON-NLS-1$
		cancel.addActionListener(this);

		final JPanel contentPane = new JPanel();
		final GridBagLayout gridbag = new GridBagLayout();
		contentPane.setLayout(gridbag);

		final String[] labels = {
			Messages.getString("frame.rate"), //$NON-NLS-1$
			Messages.getString("file.name") //$NON-NLS-1$
		};
		final JPanel border = new JPanel();
		border.setLayout(new FlowLayout(FlowLayout.LEFT));
		border.add(framesPerSecond);
		final JComponent[] components = {
			border,
			fileNamePanel
		};
		addRows(labels, components, gridbag, contentPane);

		final JPanel main = new JPanel();
		main.add(contentPane);
		add(main, BorderLayout.CENTER);
		final JPanel buttons = new JPanel();
		buttons.setLayout(new FlowLayout(FlowLayout.TRAILING));
		buttons.add(confirm);
		buttons.add(cancel);
		add(buttons, BorderLayout.SOUTH);
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
			if (isConfirm) {
				final int fps = Integer.parseInt(framesPerSecond.getText());
				SwingUtilities.invokeLater(() -> {
					final int max = designer.thumbnails.size() - 1;
					final ProgressMonitor progress = new ProgressMonitor(this, Messages.getString("rendering"), null, 0, max * designer.framesPerZoom); //$NON-NLS-1$
					progress.setMillisToDecideToPopup(50);
					progress.setMillisToPopup(250);
					new MovieRenderer(designer, fps, fileName.getText(), progress).execute();
				});
			}
		} else if (choose.equals(source)) {
			final JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileFilter(new FileNameExtensionFilter(Messages.getString("video.format"), "mp4")); //$NON-NLS-1$ //$NON-NLS-2$
			if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				fileName.setText(fileChooser.getSelectedFile().getAbsolutePath());
				confirm.setEnabled(true);
			}
		}
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
		if (framesPerSecond.equals(source)) {
			validateNumber((JTextField) source, confirm);
		}
		confirm.setEnabled(fileName.getText().length() > 0);
	}

	@Override
	protected Color getTextBackground() {
		return background;
	}

}
