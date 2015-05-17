package drafts;
import processing.core.*;

@SuppressWarnings("serial")
public class MyMover extends PApplet {

	Mover mover;

	public void setup() {
		size(800, 200);
		mover = new Mover();
	}

	public void draw() {
		background(255);
		mover.update();
		mover.checkEdges();
		mover.display();
	}

	public class Mover {
		private final static float GRAVITY_FORCE = 0.4f;
		PVector location;
		PVector velocity;
		PVector gravity;

		Mover() {
			location = new PVector(width / 2, height / 2);
			velocity = new PVector(1, 1);
			gravity = new PVector(0, GRAVITY_FORCE);
		}

		public void update() {
			velocity.add(gravity);
			location.add(velocity);
		}

		public void display() {
			stroke(0);
			strokeWeight(2);
			fill(127);
			ellipse(location.x, location.y, 48, 48);
		}

		public void checkEdges() {
			if ((location.x + 26 > width) || (location.x - 26 < 0)) {
				velocity.x = velocity.x * -1;
			}
			if ((location.y + 26 > height) || (location.y - 26 < 0)) {
				velocity.y = velocity.y * -1;
			}
			location.x = constrain(location.x, 26, width-26);
			location.y = constrain(location.y, 26, height-26);
		}

	}
}
