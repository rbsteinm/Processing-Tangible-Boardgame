package drafts;

import processing.core.PApplet;
import processing.core.PImage;
import processing.video.Capture;

@SuppressWarnings("serial")
public class CameraTest extends PApplet {
	Capture cam;
	PImage img;

	public void setup() {
		size(640, 480);
		String[] cameras = Capture.list();
		if (cameras.length == 0) {
			println("There are no cameras available for capture.");
			exit();
		} else {
			println("Available cameras:");
			for (int i = 0; i < cameras.length; i++) {
				println(cameras[i]);
			}
			cam = new Capture(this, cameras[cameras.length-1]);
			cam.start();
		}
	}

	public void draw() {
		if (cam.available() == true) {
			cam.read();
		}
		img = cam.get();
		image(img, 0, 0);
	}
}