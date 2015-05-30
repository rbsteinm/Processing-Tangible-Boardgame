package cs211.boardgame;
import java.util.ArrayList;
import java.util.List;
import processing.core.*;
import processing.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;

import cs211.imageprocessing.ImageProcessing;

/**
 * @author rbsteinm
 *
 */
@SuppressWarnings("serial")
public class DigitalGame extends PApplet{
	
	//TODO LA VITESSE N'EST PAS NULLE LORSQUE LA BALLE EST IMMOBILE, CE QUI
	//POSE PROBLEME LORS DU CALCUL DU SCORE. A REGLER RAPIDEMENT
	//Solution: poser un treshhold t: si v < t => v = 0
	
	private final static int MIN_WHEEL_VALUE = 0;
	private final static int MAX_WHEEL_VALUE = 100;
	private final static float MIN_ANGLE = -(PI/3);
	private final static float MAX_ANGLE = (PI/3);
	private final static float SPHERE_RADIUS = 10.0f;
	private final static float PLATE_WIDTH = 350.0f;
	private final static float PLATE_DEPTH = 350.0f;
	private final static float PLATE_HEIGHT = 20.0f;
	private final static float CYLINDER_HEIGHT = 40.0f;
	private final static float CYLINDER_RADIUS = 15.0f;
	private final static int CYLINDER_RESOLUTION = 40;
	private final static int DATA_SURFACE_HEIGHT = 140;
	private final static int TOP_VIEW_PLATE_WIDTH = 100;
	private final static int TOP_VIEW_PLATE_HEIGHT = 100;
	private final static int DATA_SURFACE_MARGIN = 10;
	private final static int SCOREBOARD_WIDTH = 100;
	private final static int SCOREBOARD_HEIGHT = 100;
	private final static int BARCHART_WIDTH = 1000;
	private final static int BARCHART_HEIGHT = 100;
	private final static int SCROLLBAR_HEIGHT = 10;
	private final static int SCROLLBAR_WIDTH = 400;
	private final static int MAX_SCORE = 1000;
	private final static int MIN_SCORE = 0;
	private final static float DEFAULT_BARCHART_RECT_WIDTH = 3.0f;
	
	private float barchartRectWidth = DEFAULT_BARCHART_RECT_WIDTH;
	private float barchartRectHeight = DEFAULT_BARCHART_RECT_WIDTH;
	private float barchartRectMargin = DEFAULT_BARCHART_RECT_WIDTH / 3.0f;
	
	private HScrollbar scrollbar;
	
	private TimerTask task;
	private Timer timer;
	
	private PGraphics dataSurface;
	private PGraphics topView;
	private PGraphics scoreboard;
	private PGraphics barChart;
	
	private List<Float> scores = new ArrayList<Float>();
	
	private float rotateX = 0.0f;
	private float rotateY = 0.0f;
	private float rotateZ = 0.0f;
	private float rotateSpeedXZ = 350.0f;
	private float rotateSpeedY = PI/6;
	private float boardRotateSpeed = 2.0f;
	
	private float wheelValue = 50.0f;
	
	private boolean shiftView = false;
	
	private Mover ball = new Mover();
	private Cylinder cylinder = new Cylinder();
	private List<PVector> cylinders = new ArrayList<PVector>();
	private float score = 0;
	private float lastScore = 0;
	
	
	public void setup(){
		size(1500, 800, P3D);
		cylinder.setup();
		dataSurface = createGraphics(width, DATA_SURFACE_HEIGHT, P2D);
		topView = createGraphics(TOP_VIEW_PLATE_WIDTH, TOP_VIEW_PLATE_HEIGHT, P2D);
		scoreboard = createGraphics(SCOREBOARD_WIDTH + 2*DATA_SURFACE_MARGIN, DATA_SURFACE_HEIGHT);
		barChart = createGraphics(width - TOP_VIEW_PLATE_WIDTH - SCOREBOARD_WIDTH - 4*DATA_SURFACE_MARGIN, DATA_SURFACE_HEIGHT);
		setupTimer();
		scrollbar = new HScrollbar(3*DATA_SURFACE_MARGIN + TOP_VIEW_PLATE_WIDTH + SCOREBOARD_WIDTH, height - DATA_SURFACE_MARGIN - SCROLLBAR_HEIGHT, SCROLLBAR_WIDTH, SCROLLBAR_HEIGHT);

	}
	
	public void setupTimer(){
		task = new TimerTask()
		{
			@Override
			public void run() 
			{
				scores.add(score);
			}	
		};
		
		timer = new Timer();
		timer.scheduleAtFixedRate(task, 0, 1000);
	}
	
	public void draw(){
		directionalLight(50, 100, 125, 0, -1, 0);
		ambientLight(102, 102, 102);
		background(255);
		drawGame();
		noLights();
		drawBottomSurface();
		scrollbar.update();
		scrollbar.display();
	}
	
	public void drawGame(){
		pushMatrix();
		translate(width/2, height/2);
		if(!shiftView){
			rotateX(rotateX);
			rotateY(rotateY);
			rotateZ(rotateZ);
			box(PLATE_WIDTH, PLATE_HEIGHT, PLATE_DEPTH);
			noFill();
			stroke(0);
			for(PVector cylinderPosition: cylinders){
				cylinder.show(cylinderPosition.x, cylinderPosition.y);
			}
			noStroke();
			ball.show();
		}
		else{
			rect(-PLATE_WIDTH/2, -PLATE_DEPTH/2, PLATE_WIDTH, PLATE_DEPTH);
			ball.shiftModeShow();
			for(PVector cylinderPosition: cylinders){
				cylinder.shiftModeShow(cylinderPosition.x, cylinderPosition.y);
			}
		}
		popMatrix();
	}
	
	public void drawBottomSurface(){
		drawDataSurface();
		drawTopViewSurface();
		drawScoreboard();
		drawBarchart();
	}
	
	public void drawDataSurface() {
		dataSurface.beginDraw();
		dataSurface.background(255, 228, 140);
		dataSurface.endDraw();
		image(dataSurface, 0, height - DATA_SURFACE_HEIGHT);
	}
	
	public void drawTopViewSurface(){
		topView.beginDraw();
		topView.pushMatrix();
		topView.translate(topView.width/2, topView.height/2);
		topView.background(0, 0, 255);
		//displaying ball on topView surface
		topView.noStroke();
		topView.fill(255, 0, 0);
		//TODO fade away trail?
		//topViewSurface.fill(0, 0, 255);
		//topViewSurface.fill(255, 0, 0, 10);
		float ballPosX = map(ball.location.x, -PLATE_WIDTH/2, PLATE_WIDTH/2, -topView.width/2, topView.width/2);
		float ballPosZ = map(ball.location.z, -PLATE_DEPTH/2, PLATE_DEPTH/2, -topView.height/2, topView.height/2);
		topView.ellipse(ballPosX, ballPosZ, 5, 5);
		//displaying cylinders on topView surface
		topView.fill(255);
		for(PVector cyl: cylinders){
			float cylX = map(cyl.x, -PLATE_WIDTH/2, PLATE_WIDTH/2, -topView.width/2, topView.width/2);
			float cylZ = map(cyl.y, -PLATE_DEPTH/2, PLATE_DEPTH/2, -topView.height/2, topView.height/2);
			topView.ellipse(cylX, cylZ, 10, 10);
		}
		topView.popMatrix();
		topView.endDraw();
		image(topView, DATA_SURFACE_MARGIN,  height - DATA_SURFACE_MARGIN - topView.width);
	}
	
	public void drawScoreboard(){
		scoreboard.beginDraw();
		scoreboard.stroke(255);
		scoreboard.strokeWeight(2);
		scoreboard.fill(255, 228, 140);
		scoreboard.rect(DATA_SURFACE_MARGIN, DATA_SURFACE_MARGIN, SCOREBOARD_WIDTH, SCOREBOARD_HEIGHT);
		noStroke();
		String scoreText = "";
		scoreText += "score:\n" + roundNumber(score);
		scoreText += "\nVelocity: \n" + roundNumber(ball.velocity.mag());
		scoreText += "\nLast score: \n" + roundNumber(lastScore);
		scoreboard.textSize(10);
		scoreboard.fill(0);
		scoreboard.text(scoreText, DATA_SURFACE_MARGIN*2, DATA_SURFACE_MARGIN*2);
		scoreboard.endDraw();
		image(scoreboard, TOP_VIEW_PLATE_WIDTH + DATA_SURFACE_MARGIN, height - scoreboard.height);
	}
	
	public void drawBarchart(){
		barChart.beginDraw();
		//draw background rectangle
		barChart.fill(255, 240, 160);
		barChart.stroke(255);
		barChart.strokeWeight(2);
		barChart.rect(DATA_SURFACE_MARGIN, DATA_SURFACE_MARGIN, BARCHART_WIDTH, BARCHART_HEIGHT);
		barChart.noFill();
		barChart.noStroke();
		//draw small score rectangles
		barChart.fill(0, 0, 255);
		barchartRectWidth = 2*scrollbar.getPos()*DEFAULT_BARCHART_RECT_WIDTH;
		for(int i = 0; i < scores.size(); i++){
			if(DATA_SURFACE_MARGIN + barchartRectMargin + i *(barchartRectWidth + barchartRectMargin) < BARCHART_WIDTH){
				for(int j = 0; 50*j < scores.get(i); j++){
					barChart.rect(DATA_SURFACE_MARGIN + barchartRectMargin + i *(barchartRectWidth + barchartRectMargin),
							BARCHART_HEIGHT - j*(barchartRectHeight + barchartRectMargin),
							barchartRectWidth,
							barchartRectHeight);
				}
			}
		}
		barChart.noFill();
		barChart.endDraw();
		image(barChart, 2*DATA_SURFACE_MARGIN + TOP_VIEW_PLATE_WIDTH + SCOREBOARD_WIDTH, height - DATA_SURFACE_HEIGHT);
	}
	
	/**
	 * @param newScore new score value
	 * updates score variable
	 * score has lower and upper bounds
	 */
	public void updateScore(float newScore){
		if(newScore > MAX_SCORE){
			score = MAX_SCORE;
		}
		else if(newScore < MIN_SCORE){
			score = MIN_SCORE;
		}
		else{
			lastScore = newScore - score;
			score = newScore;
		}
	}
	
	/**
	 * returns given number in String format with only two digits after the ","
	 */
	public String roundNumber(float n){
		return String.format("%.03f", n);
	}
	
	public void keyPressed(){
		if(key == CODED){
			if(keyCode == LEFT){
				rotateY -= rotateSpeedY;
			}
			else if(keyCode == RIGHT){
				rotateY += rotateSpeedY;
			}
			if(keyCode == SHIFT){
				shiftView = true;
				//TODO find a way to pause the timer when in shiftMode
				//timer.cancel();
			}
		}
	}
	
	public void keyReleased(){
		shiftView = false;
	}
	
	// updates the inclinaison of the plate when we drag it with the mouse
	// plate does not move if mouse is dragged on data surface
	public void mouseDragged(){
		if(!shiftView && mouseY < height - DATA_SURFACE_HEIGHT){
			rotateX += (pmouseY - mouseY)/rotateSpeedXZ;
			rotateZ += (mouseX - pmouseX)/rotateSpeedXZ;
			rotateX = constrain(rotateX, MIN_ANGLE, MAX_ANGLE);
			rotateZ = constrain(rotateZ, MIN_ANGLE, MAX_ANGLE);
			
		}
	}
	
	//Scolling the mouse wheel changes the rotation speed
	public void mouseWheel(MouseEvent event){
		if(!shiftView){
			//wheelValue is contained between 0 and 100
			wheelValue += event.getCount();
			wheelValue = constrain(wheelValue, MIN_WHEEL_VALUE, MAX_WHEEL_VALUE);
			
			//wheelValue is then mapped to corresponding X, Y and Z rotation speeds
			rotateSpeedY = map(wheelValue, MIN_WHEEL_VALUE, MAX_WHEEL_VALUE, 0.01f, PI/3);
			rotateSpeedXZ = map(wheelValue, MIN_WHEEL_VALUE, MAX_WHEEL_VALUE, 600.0f, 100.0f);
		}
	}
	
	//clicking on the plate in shift mode draws a cylinder on the clicked position
	public void mousePressed(){
		if(shiftView){
			float x = mouseX - width/2.0f;
			float y = mouseY - height/2.0f;
			boolean onPlate = (x < PLATE_WIDTH/2 && x > -PLATE_WIDTH/2 && y < PLATE_DEPTH/2 && y > -PLATE_DEPTH/2);
			boolean notOnBall = (dist(ball.location.x, ball.location.z, x, y) > SPHERE_RADIUS + CYLINDER_RADIUS);
			if(onPlate && notOnBall){
				cylinders.add(new PVector(x, y));
			}
		}
	}
	
	
	/**
	 * class representing a ball (sphere) and defining the way it moves
	 *
	 */
	public class Mover {
		private final static float GRAVITY_CONSTANT = 0.5f;
		private final static float NORMAL_FORCE = 1.0f;
		private final static float MU = 0.2f;
		private final static float FRICTION_MAGNITUDE = NORMAL_FORCE * MU;
		private PVector location;
		private PVector velocity;
		private PVector gravity;
		private PVector friction;	

		public Mover() {
			location = new PVector(0,-(SPHERE_RADIUS + PLATE_HEIGHT/2), 0);
			velocity = new PVector(0, 0, 0);
			gravity = new PVector(0, 0, 0);
			friction = new PVector(0, 0, 0);
		}

		/**
		 * updates ball position according to friction, gravity and its velocity
		 */
		private void update() {
			friction = velocity.get();
			friction.mult(-1);
			friction.normalize();
			friction.mult(FRICTION_MAGNITUDE);
			gravity.x = sin(rotateZ) * GRAVITY_CONSTANT;
			gravity.z = sin(-rotateX) * GRAVITY_CONSTANT;
			velocity.add(gravity);
			velocity.add(friction);
			location.add(velocity);
		}
		
		/**
		 * Handles ball bouncing when it arrives at an edge of the plate
		 */
		private void checkEdges() {    
		    if(location.x + SPHERE_RADIUS >= PLATE_WIDTH/2){
		    	updateScore(score - velocity.mag());
		    	location.x = PLATE_WIDTH/2 - SPHERE_RADIUS;
		    	velocity.x = - velocity.x;
		    }
		    else if(location.x -SPHERE_RADIUS <= -PLATE_WIDTH/2 ){
		    	updateScore(score - velocity.mag());
		    	location.x = -PLATE_WIDTH/2 + SPHERE_RADIUS;
		    	velocity.x = - velocity.x;
		    }
		    
		    if(location.z + SPHERE_RADIUS >= PLATE_DEPTH/2) {
		    	updateScore(score - velocity.mag());
		    	location.z =  PLATE_DEPTH/2 - SPHERE_RADIUS;
		    	velocity.z = - velocity.z;
		    }    
		    
		    else if(location.z - SPHERE_RADIUS <= -PLATE_DEPTH/2 ){
		    	updateScore(score - velocity.mag());
		    	location.z = - PLATE_DEPTH/2 + SPHERE_RADIUS;
		    	velocity.z = - velocity.z;
		    }
		    System.out.println(velocity.mag());
		}
		
		/**
		 * handles ball mouvements when it bounces against a cylinder
		 */
		private void checkCylinderCollision(){
			for(PVector cyl: cylinders){
				PVector n = PVector.sub(new PVector(location.x, location.z), cyl);
				if(dist(location.x, location.z, cyl.x, cyl.y) < CYLINDER_RADIUS + SPHERE_RADIUS){
					updateScore(score + velocity.mag());
					PVector velocity2D = new PVector(velocity.x, velocity.z);
					n.normalize();
					location.x = n.x*(CYLINDER_RADIUS+SPHERE_RADIUS)+cyl.x;
					location.z = n.y*(CYLINDER_RADIUS+SPHERE_RADIUS)+cyl.y;
					PVector newVelocity2D = PVector.sub(velocity2D, PVector.mult(n, 2*PVector.dot(velocity2D, n)));
					velocity = new PVector(newVelocity2D.x, 0, newVelocity2D.y);
				}
			}
		}

		/**
		 * displays the sphere on the screen
		 */
		private void display() {
			translate(location.x, location.y, location.z);
			fill(180, 180, 180);
			sphere(SPHERE_RADIUS);
			noFill();
		}

		/**
		 * displays the ball and updates its position.
		 * Also handles bouncing against edges
		 */
		public void show(){
			this.update();
			this.checkEdges();
			this.checkCylinderCollision();
			this.display();
		}
		
		/**
		 * displays the ball on the rectangle when shift mode is activated
		 */
		public void shiftModeShow(){
			pushMatrix();
			translate(location.x, location.z);
			sphere(SPHERE_RADIUS);
			popMatrix();
		}
	}
	
	/**
	 * Cylinder represented by a 2-dimensional vector corresponding tos
	 * its position on the plate. (0, 0) is on the center of the plate
	 *
	 */
	public class Cylinder{
		
		private float baseRadius;
		private float height;
		private int resolution;
		
		public Cylinder(){
			this.baseRadius = CYLINDER_RADIUS;
			this.height = CYLINDER_HEIGHT;
			this.resolution= CYLINDER_RESOLUTION;
		}
		
		private PShape openCylinder = new PShape();
		private PShape cylinderTop = new PShape();
		private PShape cylinderBottom = new PShape();

		/**
		 * creates and sets up the 3D shape of the cylinder
		 */
		private void setup() {
			float angle;
			float[] x = new float[resolution + 1];
			float[] y = new float[resolution + 1];
			// get the x and y position on a circle for all the sides
			for (int i = 0; i < x.length; i++) {
				angle = (TWO_PI / resolution) * i;
				x[i] = sin(angle) * baseRadius;
				y[i] = cos(angle) * baseRadius;
			}
			openCylinder = createShape();
			cylinderTop = createShape();
			cylinderBottom = createShape();
			//Draw border, top and bottom of the cylinder
			openCylinder.beginShape(QUAD_STRIP);
			cylinderTop.beginShape(TRIANGLE_FAN);
			cylinderBottom.beginShape(TRIANGLE_FAN);
			cylinderTop.vertex(0, 0, height);
			cylinderBottom.vertex(0, 0, 0);
			for (int i = 0; i < x.length; i++) {
				openCylinder.vertex(x[i], y[i], 0);
				openCylinder.vertex(x[i], y[i], height);
				cylinderTop.vertex(x[i], y[i], height);
				cylinderBottom.vertex(x[i], y[i], 0);
			}
			
			openCylinder.endShape();
			cylinderTop.endShape();
			cylinderBottom.endShape();
		}
		
		/**
		 * displays the 3 shapes of the cylinder:
		 * side, top and bottom
		 */
		private void display(){
			shape(openCylinder);
			shape(cylinderTop);
			shape(cylinderBottom);
		}
		/**
		 * Shows the cylinder on the plate when in classic mode
		 */
		public void show(float x, float y){
			pushMatrix();
			translate(x, -PLATE_HEIGHT/2, y);
			rotateX(PI/2);
			this.display();
			popMatrix();
		}
		/**
		 * shows a circle on the plate in shift mode exactly on
		 * the cylinder's position
		 */
		public void shiftModeShow(float x, float y){
			stroke(0);
			ellipse(x, y, CYLINDER_RADIUS*2, CYLINDER_RADIUS*2);
			noStroke();
		}
	}
	
	class HScrollbar {
		
		  float barWidth;  //Bar's width in pixels
		  float barHeight; //Bar's height in pixels
		  float xPosition;  //Bar's x position in pixels
		  float yPosition;  //Bar's y position in pixels
		  
		  float sliderPosition, newSliderPosition;    //Position of slider
		  float sliderPositionMin, sliderPositionMax; //Max and min values of slider
		  
		  boolean mouseOver;  //Is the mouse over the slider?
		  boolean locked;     //Is the mouse clicking and dragging the slider now?

		  /**
		   * @brief Creates a new horizontal scrollbar
		   * 
		   * @param x The x position of the top left corner of the bar in pixels
		   * @param y The y position of the top left corner of the bar in pixels
		   * @param w The width of the bar in pixels
		   * @param h The height of the bar in pixels
		   */
		  HScrollbar (float x, float y, float w, float h) {
		    barWidth = w;
		    barHeight = h;
		    xPosition = x;
		    yPosition = y;
		    
		    sliderPosition = xPosition + barWidth/2 - barHeight/2;
		    newSliderPosition = sliderPosition;
		    
		    sliderPositionMin = xPosition;
		    sliderPositionMax = xPosition + barWidth - barHeight;
		  }

		  /**
		   * @brief Updates the state of the scrollbar according to the mouse movement
		   */
		  void update() {
		    if (isMouseOver()) {
		      mouseOver = true;
		    }
		    else {
		      mouseOver = false;
		    }
		    if (mousePressed && mouseOver) {
		      locked = true;
		    }
		    if (!mousePressed) {
		      locked = false;
		    }
		    if (locked) {
		      newSliderPosition = constrain(mouseX - barHeight/2, sliderPositionMin, sliderPositionMax);
		    }
		    if (abs(newSliderPosition - sliderPosition) > 1) {
		      sliderPosition = sliderPosition + (newSliderPosition - sliderPosition);
		    }
		  }

		  /**
		   * @brief Clamps the value into the interval
		   * 
		   * @param val The value to be clamped
		   * @param minVal Smallest value possible
		   * @param maxVal Largest value possible
		   * 
		   * @return val clamped into the interval [minVal, maxVal]
		   */
		  float constrain(float val, float minVal, float maxVal) {
		    return min(max(val, minVal), maxVal);
		  }

		  /**
		   * @brief Gets whether the mouse is hovering the scrollbar
		   *
		   * @return Whether the mouse is hovering the scrollbar
		   */
		  boolean isMouseOver() {
		    if (mouseX > xPosition && mouseX < xPosition+barWidth &&
		      mouseY > yPosition && mouseY < yPosition+barHeight) {
		      return true;
		    }
		    else {
		      return false;
		    }
		  }

		  /**
		   * @brief Draws the scrollbar in its current state
		   */ 
		  void display() {
		    noStroke();
		    fill(204);
		    rect(xPosition, yPosition, barWidth, barHeight);
		    if (mouseOver || locked) {
		      fill(0, 0, 0);
		    }
		    else {
		      fill(102, 102, 102);
		    }
		    rect(sliderPosition, yPosition, barHeight, barHeight);
		    fill(topView.ambientColor);
		  }

		  /**
		   * @brief Gets the slider position
		   * 
		   * @return The slider position in the interval [0,1] corresponding to [leftmost position, rightmost position]
		   */
		  float getPos() {
		    return (sliderPosition - xPosition)/(barWidth - barHeight);
		  }
		}
	
}
