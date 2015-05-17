package drafts;

import processing.core.*;

@SuppressWarnings("serial")
public class Triangle2 extends PApplet {
	PShape triangle = new PShape();

	public void setup() {
		size(400, 400, P3D);
		triangle = createShape();
		triangle.beginShape(TRIANGLES);
		triangle.vertex(0, 0);
		triangle.vertex(50, 0);
		triangle.vertex(50, 50);
		triangle.endShape();
	}

	public void draw() {
		background(0);
		translate(mouseX, mouseY);
		shape(triangle);
	}

}
