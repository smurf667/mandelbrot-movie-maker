package de.engehausen.mb;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import de.engehausen.mb.math.Point3D;

public class FrameStreams {

	public static Stream<FrameData> buildLinear(final List<FrameData> frames, final int frameCount) {
		return StreamSupport.stream(new LinearIterator(frames, frameCount), false);
	}

	public static Stream<FrameData> buildLogarithmic(final List<FrameData> frames, final int desiredFrameCount) {
		return StreamSupport.stream(new LogarithmicIterator(frames, desiredFrameCount), false);
	}

	private static abstract class AbstractIterator implements Spliterator<FrameData> {

		protected final Iterator<FrameData> iterator;
		protected final int max;
		protected final FrameData inter;
		protected final double scaleStart;
		protected FrameData current;
		protected FrameData next;
		protected int pos;

		public AbstractIterator(final List<FrameData> frames, final int max) {
			final int size = frames.size();
			if (size < 2) {
				throw new IllegalArgumentException("must have at least two frames");
			}
			iterator = frames.iterator();
			this.max = max;
			current = iterator.next();
			next = iterator.next();
			scaleStart = current.scale;
			inter = new FrameData(current);
		}

		@Override
		public boolean tryAdvance(final Consumer<? super FrameData> consumer) {
			if (pos > max) {
				return false;
			}
			final double actual = getScale();
			if (actual < next.scale) {
				current = next;
				next = iterator.next();
			}
			final Point3D interpolated = interpolate(actual);
			inter.topLeft.setReal(interpolated.x);
			inter.topLeft.setImaginary(interpolated.y);
			inter.scale = interpolated.z;
			inter.frameOffset = current.frameOffset;
			consumer.accept(inter);
			pos++;
			return true;
		}

		@Override
		public Spliterator<FrameData> trySplit() {
			return this;
		}

		@Override
		public long estimateSize() {
			return max;
		}

		@Override
		public int characteristics() {
			return Spliterator.ORDERED;
		}

		protected abstract double getScale();

		protected Point3D interpolate(final double scale) {
			final double t = (scale - current.scale) / (next.scale - current.scale);
			return Point3D.linear(
				t,
				new Point3D(current.topLeft, current.scale),
				new Point3D(next.topLeft, next.scale)
				);
		}
	}

	private static class LinearIterator extends AbstractIterator {

		private final double step;

		public LinearIterator(final List<FrameData> frames, final int max) {
			super(frames, max);
			step = (frames.get(frames.size() - 1).scale - current.scale) / (double) max;
		}

		@Override
		protected double getScale() {
			return scaleStart + step * pos;
		}

	}

	private static class LogarithmicIterator extends AbstractIterator {

		private static final double BASE = 0.95d;
		private double factor;
		private double initial;

		public LogarithmicIterator(final List<FrameData> frames, final int max) {
			super(frames, max);
			initial = current.scale;
			factor = (Math.log(frames.get(frames.size() - 1).scale / (BASE * initial)) / Math.log(BASE)) / max;
		}

		@Override
		protected double getScale() {
			return initial * Math.pow(BASE, factor * pos);
		}

		@Override
		protected Point3D interpolate(final double scale) {
			final double t = (scale - current.scale) / (next.scale - current.scale);
			final Point3D from = new Point3D(current.topLeft, current.scale);
			final Point3D to = new Point3D(next.topLeft, next.scale);
			return Point3D.bezier(
				t,
				from,
				from,
				to,
				to
			);
		}

	}

}
