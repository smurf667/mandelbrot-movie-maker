package de.engehausen.mb;

import java.awt.Dimension;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;

import com.xuggle.ferry.AtomicInteger;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.Configuration;
import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IRational;
import com.xuggle.xuggler.IStreamCoder;

import de.engehausen.mb.math.MandelbrotSet;
import de.engehausen.mb.ui.Designer;

/**
 * Renders the frames defined in the {@link Designer} into a
 * MP4 movie with the given frames per second and number of frames
 * per zoom step.
 */
public class MovieRenderer extends SwingWorker<Void, Void> {

	private final Designer designer;
	private final int framesPerSecond;
	private final int frameCount;
	private final int qScale;
	private final int bitRate;
	private final ProgressMonitor progress;
	private final String fileName;

	/**
	 * Creates the render.
	 * @param designer the designer supplying the zoom step information
	 * @param fps the frames per second
	 * @param seconds duration of video in seconds
	 * @param quality the quality (1..51, from best to worst)
	 * @param bitrate bitrate for the video
	 * @param fileName the file name of the result video
	 * @param monitor a progress monitor
	 */
	public MovieRenderer(
		final Designer designer,
		final int fps,
		final int seconds,
		final int quality,
		final int bitrate,
		final String fileName,
		final ProgressMonitor monitor
	) {
		this.designer = designer;
		this.fileName = fileName;
		framesPerSecond = fps;
		frameCount = fps * seconds;
		qScale = quality;
		bitRate = bitrate;
		progress = monitor;
	}

	/**
	 * Renders the movie.
	 */
	@Override
	protected Void doInBackground() throws Exception {
		final Dimension dimension = designer.getFramePreview().getPreferredSize();
		final MandelbrotSet mandelbrot = designer.getMandelbrotSet();
		final int[] colors = designer.getColors();

		final Properties configProps = new Properties();
		configProps.load(MovieRenderer.class.getResourceAsStream("/h264.properties"));
		configProps.setProperty("qmin", Integer.toString(qScale));
		configProps.setProperty("qmax", Integer.toString(qScale));
		configProps.setProperty("b", Integer.toString(bitRate));
		configProps.setProperty("ab", Integer.toString(bitRate));

		final IMediaWriter writer = ToolFactory.makeWriter(fileName);
		final int streamIndex = writer
			.addVideoStream(
				0,
				0,
				ICodec.ID.CODEC_ID_H264,
				IRational.make(1000, framesPerSecond),
				dimension.width,
				dimension.height
			);
		final IStreamCoder coder = writer
			.getContainer()
			.getStream(streamIndex)
			.getStreamCoder();
		Configuration.configure(configProps, coder);
		final long msGoal = (long) 1000d / framesPerSecond;

		final AtomicInteger count = new AtomicInteger();
		final AtomicLong timestamp = new AtomicLong(-msGoal);
		try {
			FrameStreams
				.buildLogarithmic(
					designer.getFrameDataList(),
					frameCount
				).forEach(frameData -> {
					if (progress.isCanceled()) {
						return;
					}
					writer
						.encodeVideo(
							0,
							mandelbrot.render(
								frameData.topLeft,
								frameData.scale,
								dimension.width,
								dimension.height,
								frameData.frameOffset,
								colors
							),
							timestamp.addAndGet(msGoal),
							TimeUnit.MILLISECONDS);
					progress.setProgress(count.incrementAndGet());
				});
		} finally {
			writer.close();
			progress.close();
		}
		return null;
	}

}
