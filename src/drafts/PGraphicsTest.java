package drafts;
import processing.core.*;

import processing.core.PApplet;

@SuppressWarnings("serial")
public class PGraphicsTest extends PApplet{
	PGraphics mySurface;
	public void setup() {
	  size(400, 400, P2D);
	  mySurface = createGraphics(200, 200, P2D);
	}
	public void draw() {
	  background(200, 0, 0);
	  drawMySurface();
	  image(mySurface, 10, 190);
	}
	void drawMySurface() {
	  mySurface.beginDraw();
	  mySurface.background(0);
	  mySurface.ellipse(50, 50, 25, 25);
	  mySurface.endDraw();
	}
}
