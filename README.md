# Mandelbrot Movie Maker

This is just a toy. It was born after watching this [excellent video](https://youtu.be/NGMRB4O922I) on the [Mandelbrot set](https://en.wikipedia.org/wiki/Mandelbrot_set).

I am amazed how such beautiful complexity can spring from a simple iteration formula.

This tool lets you explore the Mandelbrot set and also create a video out of the various zoom steps using [Xuggler](http://www.xuggle.com/xuggler/) (which uses [GPLv3](http://www.gnu.org/copyleft/gpl.html) as its license).

I've not put any special effort into controlling the numbers, just using double precision, which means there are limits to zooming in.

## Building and running

To run the application from Maven simply issue

	mvn compile exec:java

This will start a wizard in which you can define the color gradient and the number of frames per zoom. Once you're happy with the settings you can proceed to the zooming feature. Use the mouse to select a rectangular zooming area and press enter to zoom in. You can select a thumbnail in the bottom scroll pane and press delete to remove it and its peers to the right. A single frame can be saved using the file menu as a PNG file. The file menu also offers to save the frame sequence as a zooming MP4 movie.

To package the application as a self-contained, distributable and executable `.jar` file issue

	mvn package

Now you can run the output as a normal Java application, e.g. `java [-Duser.language=en] mandelbrot-movie-maker-...-jar-with-dependencies.jar`.

## Demonstration

Here are two videos generated with the tool:

[![v0.2.0](https://img.youtube.com/vi/BOyfSGexU08/0.jpg)](https://youtu.be/BOyfSGexU08)

[![v0.1.0](https://img.youtube.com/vi/BMJ7DHeYodc/0.jpg)](https://www.youtube.com/watch?v=BMJ7DHeYodc)

And here is a random collection of frames generated by the tool. You can load those frames for further investigation too:

|example|example|
|-|-|
|[![demo01](/src/test/resources/demo01.gif?raw=true "demo01")](/src/test/resources/demo01.png?raw=true)|[![demo02](/src/test/resources/demo02.gif?raw=true "demo02")](/src/test/resources/demo02.png?raw=true)|
|[![demo03](/src/test/resources/demo03.gif?raw=true "demo03")](/src/test/resources/demo03.png?raw=true)|[![demo04](/src/test/resources/demo04.gif?raw=true "demo04")](/src/test/resources/demo04.png?raw=true)|
|[![demo05](/src/test/resources/demo05.gif?raw=true "demo05")](/src/test/resources/demo05.png?raw=true)|[![demo06](/src/test/resources/demo06.gif?raw=true "demo06")](/src/test/resources/demo06.png?raw=true)|

## Version history

### v0.2.0

Better use of MP4 encoding, dropped color rotation and improve zoom flow.

### v0.1.0

Initial release.
