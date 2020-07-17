package de.engehausen.mb;

import java.awt.Dimension;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.Configuration;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStreamCoder;

import de.engehausen.mb.math.MandelbrotSet;
import de.engehausen.mb.math.Number;
import de.engehausen.mb.ui.Designer;

/**
 * Renders the frames defined in the {@link Designer} into a
 * MP4 movie with the given frames per second and number of frames
 * per zoom step.
 */
public class MovieRenderer extends SwingWorker<Void, Void> {

	private final Designer designer;
	private final int framesPerSecond;
	private final int qScale;
	private final int bitRate;
	private final ProgressMonitor progress;
	private final String fileName;

	/**
	 * Creates the render.
	 * @param designer the designer supplying the zoom step information
	 * @param fps the frames per second
	 * @param quality the quality (1..51, from best to worst)
	 * @param bitrate bitrate for the video
	 * @param fileName the file name of the result video
	 * @param monitor a progress monitor
	 */
	public MovieRenderer(
		final Designer designer,
		final int fps,
		final int quality,
		final int bitrate,
		final String fileName,
		final ProgressMonitor monitor
	) {
		this.designer = designer;
		this.fileName = fileName;
		framesPerSecond = fps;
		qScale = quality;
		bitRate = bitrate;
		progress = monitor;
	}

	/**
	 * Renders the movie.
	 */
	@Override
	protected Void doInBackground() throws Exception {
		final MandelbrotSet mandelbrot = designer.getMandelbrotSet();
		final int[] colors = designer.getColors();
		final List<FrameData> list = designer.getFrameDataList();
		final int max = list.size() - 1;
		FrameData current = list.get(0);
		FrameData last;
		final int fpz = designer.getFramesPerZoom();
		final Dimension dimension = designer.getFramePreview().getPreferredSize();
		final double fpzd = fpz;

		final Properties configProps = new Properties();
		configProps.load(MovieRenderer.class.getResourceAsStream("/h264.properties"));
		configProps.setProperty("qmin", Integer.toString(qScale));
		configProps.setProperty("qmax", Integer.toString(qScale));
		configProps.setProperty("b", Integer.toString(bitRate));
		configProps.setProperty("ab", Integer.toString(bitRate));

		final IMediaWriter writer = ToolFactory.makeWriter(fileName);
		final int streamIndex = writer.addVideoStream(0, 0, ICodec.ID.CODEC_ID_H264, IRational.make(1000, framesPerSecond), dimension.width, dimension.height);
		final IStreamCoder coder = writer.getContainer().getStream(streamIndex).getStreamCoder();
		Configuration.configure(configProps, coder);
		final long msGoal = (long) 1000d / framesPerSecond;
		long timestamp = 0;
		int zoomStep = 0;

		try {
			int count = 0;
			do {
				last = current;
				current = list.get(++zoomStep);
				final Number delta = new Number(current.topLeft)
					.subtract(last.topLeft)
					.divide(fpzd);
				final double scaleStep = (current.scale - last.scale) / fpzd;
				final Number frame = new Number(last.topLeft);
				double frameScale = last.scale;
				int off = last.frameOffset;
				for (int j = 0; j < fpz && !progress.isCanceled(); j++, count++) {
					writer.encodeVideo(0, mandelbrot.render(frame, frameScale, dimension.width, dimension.height, off, colors), timestamp, TimeUnit.MILLISECONDS);
					timestamp += msGoal;
					progress.setProgress(count);
					frame.add(delta);
					frameScale += scaleStep;
					if (designer.isRotateColors()) {
						off++;
					}
				}
			} while (zoomStep < max);
		} catch (Throwable t) {
			t.printStackTrace(System.err);
		} finally {
			writer.close();
			progress.close();
		}
		return null;
	}

}
