package de.engehausen.mb.math;

import org.junit.Assert;
import org.junit.Test;

public class NumberTest {
	
	@Test
	public void testAddition() {
		final Number number1 = new Number(0, 0);
		final Number number2 = new Number(2, 1);
		number1.add(number2);
		Assert.assertEquals(2, number1.getReal(), 0);
		Assert.assertEquals(1, number1.getImaginary(), 0);
	}

	@Test
	public void testSubtraction() {
		final Number number1 = new Number(1, 0);
		final Number number2 = new Number(1, 1);
		number1.subtract(number2);
		Assert.assertEquals(0, number1.getReal(), 0);
		Assert.assertEquals(-1, number1.getImaginary(), 0);
	}

	@Test
	public void testMultiplication() {
		final Number number1 = new Number(1, 2);
		final Number number2 = new Number(number1);
		number1.multiply(number2);
		Assert.assertEquals(-3, number1.getReal(), 0);
		Assert.assertEquals(4, number1.getImaginary(), 0);
	}

	@Test
	public void testDivision() {
		final Number number = new Number(4, 2);
		number.divide(2);
		Assert.assertEquals(2, number.getReal(), 0);
		Assert.assertEquals(1, number.getImaginary(), 0);
	}
	
	@Test
	public void testDistance() {
		final Number number1 = new Number(5, 5);
		final Number number2 = new Number(1, 1);
		Assert.assertFalse(number1.inside());
		Assert.assertTrue(number2.inside());
	}

}
