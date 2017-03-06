package cs211.boardgame;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PVector;

@SuppressWarnings("serial")
public class GameMenu extends PApplet {
	private final static float PLATE_WIDTH = 450.0f;
	private final static float PLATE_DEPTH = 450.0f;
	private final static float CYLINDER_RADIUS = 13.0f;
	private final static float SPHERE_RADIUS = 10.0f;
	private final static int BUTTON_WIDTH = 300;
	private final static int BUTTON_HEIGHT = 50;
	private final static int BUTTON_MARGIN = 50;
	private final static int TOP_MARGIN = 200;
	private final static int TEXT_MARGIN = 15;
	
	private int LEFT_MARGIN = 50;
	private ArrayList<PVector> bowlingPins = new ArrayList<PVector>();
	private boolean noPinAlert = false;
	
	private String[] buttonNames = new String[]{"DIGITAL GAME", "TANGIBLE GAME", "TANGIBLE DEMO"};
	private String instructions = 
			"* tilt the board in order to hit all the pins with the ball !\n\n" +
			"* hold SHIFT to add more pins during the game\n\n" +
			"* in digital mode, scroll the mouse wheel to change \n  rotation speed\n\n" +
			"* press ESC to quit";
	

	public void setup() {
		size(1400, 800);
		background(255);
		noFill();
	}

	public void draw() {
		background(204, 229, 255);
		writeText();
		strokeWeight(3);
		showButtons();
		noStroke();
		fill(150);
		drawBoard();
		stroke(0);
		noFill();
	}
	
	public void drawBoard(){
		rect(width/2-PLATE_WIDTH/2, height/2-PLATE_DEPTH/2, PLATE_WIDTH, PLATE_DEPTH);
		for(PVector pin: bowlingPins){
			//showing pins positions with circles on the shiftMode plate
			stroke(0);
			strokeWeight(1);
			ellipse(pin.x+width/2, pin.y+height/2, CYLINDER_RADIUS*2, CYLINDER_RADIUS*2);
			noStroke();
		}
	}
	
	public void mousePressed(){
		createPin(mouseX-width/2, mouseY-height/2);
			if(mouseOverButton(0) && bowlingPins.size() > 0){
				PApplet game = new Game(false, false, bowlingPins);
				game.init();
				add(game);
				this.stop();
				this.destroy();
			}
			else if(mouseOverButton(1) && bowlingPins.size() > 0){
				PApplet game = new Game(true, false, bowlingPins);
				game.init();
				add(game);
				this.stop();
				this.destroy();
			} else if(mouseOverButton(2)){
				PApplet game = new Game(true, true, bowlingPins);
				game.init();
				add(game);
				this.stop();
				this.destroy();
			} else if(mouseOverButton(0) || mouseOverButton(1)){
			noPinAlert = true;
		}
	}
	
	/**
	 * @param y
	 * @return true if the mouse is over the button starting at height y
	 */
	public boolean mouseOverButton(int i){
		return (mouseX > LEFT_MARGIN && 
				mouseX < LEFT_MARGIN + BUTTON_WIDTH && 
				mouseY > (TOP_MARGIN+BUTTON_MARGIN)+i*(BUTTON_HEIGHT+BUTTON_MARGIN) &&
				mouseY < (TOP_MARGIN+BUTTON_MARGIN)+i*(BUTTON_HEIGHT+BUTTON_MARGIN) + BUTTON_HEIGHT);
	}
	
	public void showButtons(){
		float y = TOP_MARGIN;
		for(int i = 0; i < buttonNames.length; i++){
			y += BUTTON_MARGIN;
			if (mouseOverButton(i)){
				fill(170);
			}
			rect(LEFT_MARGIN, y, BUTTON_WIDTH, BUTTON_HEIGHT, 10);
			textSize(25);
			textAlign(CENTER);
			fill(50);
			text(buttonNames[i], LEFT_MARGIN + BUTTON_WIDTH/2, y + BUTTON_HEIGHT - TEXT_MARGIN);
			noFill();
			y += BUTTON_HEIGHT;
		}
	}
	
	public void writeText(){
		textSize(30);
		textAlign(CENTER);
		fill(50);
		text("Place your pins on the board!", width/2, 100);
		textAlign(LEFT);
		textSize(25);
		text("instructions: ", width-9*LEFT_MARGIN, TOP_MARGIN + BUTTON_MARGIN);
		textSize(14);
		text(instructions, width-8*LEFT_MARGIN, TOP_MARGIN + 2*BUTTON_MARGIN);
		textAlign(CENTER);
		if(noPinAlert){
			textSize(15);
			fill(255, 0, 0);
			text("place at least one pin before starting the game", width/2, 130);
		}
		noFill();
	}
	
	/**
	 * @param x
	 * @param y
	 * create a new pin at coord. (x, y) if the location 
	 * is on the plate and free (other pins/ball)
	 */
	public void createPin(float x, float y){
		boolean onPlate = (x < PLATE_WIDTH/2 && x > -PLATE_WIDTH/2 && y < PLATE_DEPTH/2 && y > -PLATE_DEPTH/2);
		boolean notOnBall = (dist(0, 0, x, y) > SPHERE_RADIUS + CYLINDER_RADIUS);
		boolean notOnOtherPin = true;
		if(!onPlate || !notOnBall){
			return;
		}
		for(PVector vect: bowlingPins){
			if(dist(vect.x, vect.y, x, y) < 2*CYLINDER_RADIUS){
				notOnOtherPin = false;
				return;
			}
		}
		if(notOnOtherPin){
			bowlingPins.add(new PVector(x, y));
		}
	}
}
