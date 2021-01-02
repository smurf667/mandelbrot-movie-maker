package de.engehausen.mb.math;

public class Point3D {

	public final double x;
	public final double y;
	public final double z;

	public Point3D(final double x, final double y, final double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Point3D(final Number topLeft, final double scale) {
		x = topLeft.getReal();
		y = topLeft.getImaginary();
		z = scale;
	}

	public Point3D add(final Point3D src) {
		return new Point3D(x + src.x, y + src.y, z + src.z);
	}

	public Point3D mul(final double scale) {
		return new Point3D(scale * x, scale * y, scale * z);
	}

	public String toString() {
		return String.format("(%f,%f,%f)", x, y, z);
	}

	/**
	 * Interpolate between p0 and p3 with control points p1 and p2.
	 * @param t position to interpolate (0..1)
	 * @param p0 start point
	 * @param p1 control point for start
	 * @param p2 control point for end
	 * @param p3 end point
	 * @return interpolated point
	 */
	public static Point3D bezier(
		final double t,
		final Point3D p0,
		final Point3D p1,
		final Point3D p2,
		final Point3D p3
	) {
		// see https://math.stackexchange.com/questions/577641/how-to-calculate-interpolating-splines-in-3d-space
		// C(t) = (1−t)^3 * P0 + 3 * t * (1−t)^2 * P1 + 3 * t^2 * (1−t) * P2 + t^3 * P3
		final double r = 1 - t;
		return p0
			.mul(r * r * r)
			.add(p1.mul(3 * t * r * r))
			.add(p2.mul(3 * t * t * r))
			.add(p3.mul(t * t * t));
	}

	/**
	 * Linear interpolation between two points
	 * @param t position to interpolate (0..1)
	 * @param p0 start point
	 * @param p1 end point
	 * @return interpolated point
	 */
	public static Point3D linear(
		final double t,
		final Point3D p0,
		final Point3D p1
	) {
		return p1.mul(t).add(p0.mul(1 - t));
	}

}
