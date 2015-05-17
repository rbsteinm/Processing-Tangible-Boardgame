package drafts;

import processing.core.*;

@SuppressWarnings("serial")
public class Triangle1 extends PApplet {
	public void setup() {
		size(400, 400, P3D);
	}

	public void draw() {
		background(0);
		translate(mouseX, mouseY);
		beginShape(TRIANGLES);
		vertex(0, 0);
		vertex(50, 0);
		vertex(50, 50);
		endShape();
	}
}
