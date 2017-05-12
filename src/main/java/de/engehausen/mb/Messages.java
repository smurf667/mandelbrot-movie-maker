package de.engehausen.mb;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Accessor for translated text.
 */
public final class Messages {

	private static final String BUNDLE_NAME = "messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	
	private Messages() {
	}

	/**
	 * Returns the translated text for the given key.
	 * @param key the lookup key for the text
	 * @return the translated text or the key itself surrounded by {@code !}
	 */
	public static String getString(final String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}

	/**
	 * Returns the locale-sensitive character for a mnemonic.
	 * @param key the lookup key for the mnemonic
	 * @return the mnemonic or the character {@code !}
	 */
	public static char getMnemonic(final String key) {
		return getString(key + ".mne").charAt(0);
	}

}
