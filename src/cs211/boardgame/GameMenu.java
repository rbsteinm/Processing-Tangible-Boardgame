package cs211.boardgame;

import processing.core.PApplet;

public class GameMenu extends PApplet {
	private final static int BUTTON_WIDTH = 300;
	private final static int BUTTON_HEIGHT = 50;
	private final static int BUTTON_MARGIN = 50;
	private final static int TOP_MARGIN = 0;
	private final static int TEXT_MARGIN = 15;
	private int LEFT_MARGIN = 50;
	
	private String[] buttonNames = new String[]{"DIGITAL GAME", "TANGIBLE GAME", "MILESTONE IV TEST"};
	private int nButtons = buttonNames.length;
	

	public void setup() {
		size(1400, 800);
		background(255);
		stroke(0);
		noFill();
	}

	public void draw() {
		background(204, 229, 255);
		showButtons();
		noFill();
		if (mousePressed) {
			if (mouseX > BUTTON_MARGIN && mouseX < BUTTON_WIDTH + BUTTON_MARGIN && mouseY > BUTTON_MARGIN && mouseY < BUTTON_HEIGHT + BUTTON_MARGIN) {
				println("The mouse is pressed and over the button");
				PApplet game = new DigitalGame();
				game.init();
				add(game);
				this.stop();
				game.start();
			}
		}
	}
	
	public void mousePressed(){
		if(mouseOverButton(0)){
			PApplet game = new DigitalGame();
			game.init();
			add(game);
			this.stop();
			game.start();
		}
		else if(mouseOverButton(1)){
			PApplet game = new TangibleGame(false);
			game.init();
			add(game);
			this.stop();
			game.start();
		} else if(mouseOverButton(2)){
			PApplet game = new TangibleGame(true);
			game.init();
			add(game);
			this.stop();
			game.start();
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
			strokeWeight(3);
			rect(LEFT_MARGIN, y, BUTTON_WIDTH, BUTTON_HEIGHT, 10);
			textSize(25);
			fill(50);
			textAlign(CENTER);
			text(buttonNames[i], LEFT_MARGIN + BUTTON_WIDTH/2, y + BUTTON_HEIGHT - TEXT_MARGIN);
			noFill();
			y += BUTTON_HEIGHT;
		}
	}
}
