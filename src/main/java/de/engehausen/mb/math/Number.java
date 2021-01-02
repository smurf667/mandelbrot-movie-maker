package de.engehausen.mb.math;

/**
 * Complex number, based on {@code double} data type.
 */
public class Number {

	private double a;
	private double b;

	/**
	 * Creates the number with the given components.
	 * @param r the real component.
	 * @param i the imaginary component.
	 */
	public Number(final double r, final double i) {
		a = r;
		b = i;
	}

	/**
	 * Creates the number based on the given number.
	 * @param number the number whose components to copy.
	 */
	public Number(final Number number) {
		this(number.a, number.b);
	}

	/**
	 * Returns the real component of the number.
	 * @return the real component of the number.
	 */
	public double getReal() {
		return a;
	}

	/**
	 * Returns the imaginary component of the number.
	 * @return the imaginary component of the number.
	 */
	public double getImaginary() {
		return b;
	}

	/**
	 * Sets the components
	 * @param r the real component
	 * @param i the imaginary component
	 */
	public void set(final double r, final double i) {
		a = r;
		b = i;
	}

	/**
	 * Sets the real component
	 * @param r the real component
	 */
	public void setReal(final double r) {
		a = r;
	}

	/**
	 * Sets the imaginary component
	 * @param i the imaginary component
	 */
	public void setImaginary(final double i) {
		b = i;
	}

	/**
	 * Multiplies this number by the given number.
	 * @param number the number to multiply by.
	 * @return this, modified instance
	 */
	public Number multiply(final Number number) {
		final double nx = a * number.a - b * number.b;
		final double ny = a * number.b + b * number.a;
		a = nx;
		b = ny;
		return this;
	}

	/**
	 * Adds to this number the given number.
	 * @param number the number to add.
	 * @return this, modified instance
	 */
	public Number add(final Number number) {
		a += number.a;
		b += number.b;
		return this;
	}

	/**
	 * Subtracts from this number the given number.
	 * @param number the number to add.
	 * @return this, modified instance
	 */
	public Number subtract(final Number number) {
		a -= number.a;
		b -= number.b;
		return this;
	}

	/**
	 * Divides this number by the given scalar factor.
	 * @param factor the factor to divide by.
	 * @return this, modified instance
	 */
	public Number divide(final double factor) {
		a /= factor;
		b /= factor;
		return this;
	}

	/**
	 * Tests if this number is inside the radius of two.
	 * @return {@code true} if inside, {@code false} otherwise.
	 */
	public boolean inside() {
		return a*a + b*b < 4;
	}

	/**
	 * Returns a human-readable representation of the number.
	 * @return a human-readable representation of the number.
	 */
	public String toString() {
		return a+","+b;
	}

}
