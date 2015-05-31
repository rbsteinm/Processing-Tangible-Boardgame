package drafts;

import processing.core.PApplet;
import processing.core.PShape;

public class BowlingPinTest extends PApplet{
	PShape bowlingPin;
	public void setup(){
		size(1500, 800, P3D);
		bowlingPin = loadShape("bowlingPin6.obj");
	}
	public void draw(){
		translate(150, 150);
		shape(bowlingPin);
	}

}
