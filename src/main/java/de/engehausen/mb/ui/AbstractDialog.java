package de.engehausen.mb.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Base for some dialogs.
 * Provides common functionality for layout, validation and input value conversion.
 */
public abstract class AbstractDialog extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]+");

	protected abstract Color getTextBackground();

	/**
	 * Creates labeled rows of content.
	 * @param labels the labels for the content
	 * @param components the actual content
	 * @param gridbag the layout
	 * @param container the container to which to add the components
	 */
	protected void addRows(final String[] labels, final JComponent[] components, final GridBagLayout gridbag, final Container container) {
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;

		for (int i = 0; i < labels.length; i++) {
			constraints.gridwidth = GridBagConstraints.RELATIVE;
			constraints.fill = GridBagConstraints.NONE;
			final JPanel label = new JPanel();
			label.add(new JLabel(labels[i]));
			container.add(label, constraints);

			constraints.gridwidth = GridBagConstraints.REMAINDER;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			constraints.weightx = 1.0d;
			container.add(components[i], constraints);
		}
	}

	/**
	 * Converts the value of the input to an integer.
	 * @param text the text field providing a numeric value.
	 * @return the parse value of the input
	 */
	protected int toInt(final JTextField text) {
		return Integer.parseInt(text.getText());
	}

	/**
	 * Verifies the input is a valid number and optionally enables
	 * or disables a component based on this result
	 * @param source the provider of the potentially numeric value
	 * @param enabler the component to enable or disable, may be {@code null}.
	 */
	protected void validateNumber(final JTextField source, final Component enabler) {
		final boolean isNumber = NUMBER_PATTERN.matcher(source.getText()).matches();
		source.setBackground(isNumber ? getTextBackground() : Color.RED);
		source.repaint();
		if (enabler != null) {
			enabler.setEnabled(isNumber);
		}
	}

}
