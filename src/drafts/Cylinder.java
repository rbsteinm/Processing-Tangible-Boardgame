package drafts;

import processing.core.*;

@SuppressWarnings("serial")
public class Cylinder extends PApplet {
	float cylinderBaseSize = 50;
	float cylinderHeight = 50;
	int cylinderResolution = 40;
	PShape openCylinder = new PShape();
	PShape cylinderTop = new PShape();
	PShape cylinderBottom = new PShape();

	public void setup() {
		size(400, 400, P3D);
		float angle;
		float[] x = new float[cylinderResolution + 1];
		float[] y = new float[cylinderResolution + 1];
		// get the x and y position on a circle for all the sides
		for (int i = 0; i < x.length; i++) {
			angle = (TWO_PI / cylinderResolution) * i;
			x[i] = sin(angle) * cylinderBaseSize;
			y[i] = cos(angle) * cylinderBaseSize;
		}
		openCylinder = createShape();
		cylinderTop = createShape();
		cylinderBottom = createShape();
		//Draw border, top and bottom of the cylinder
		openCylinder.beginShape(QUAD_STRIP);
		cylinderTop.beginShape(TRIANGLE_FAN);
		cylinderBottom.beginShape(TRIANGLE_FAN);
		cylinderTop.vertex(0, 0, cylinderHeight);
		cylinderBottom.vertex(0, 0, 0);
		for (int i = 0; i < x.length; i++) {
			openCylinder.vertex(x[i], y[i], 0);
			openCylinder.vertex(x[i], y[i], cylinderHeight);
			cylinderTop.vertex(x[i], y[i], 0);
			cylinderBottom.vertex(x[i], y[i], cylinderHeight);
		}
		
		openCylinder.endShape();
		cylinderTop.endShape();
		cylinderBottom.endShape();
	}

	public void draw() {
		background(255);
		translate(mouseX, mouseY, 0);
		shape(openCylinder);
		shape(cylinderTop);
		shape(cylinderBottom);
	}

}
