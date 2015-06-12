package cs211.imageprocessing;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;

public class BlobDetection extends PApplet{
	PApplet applet;
	Polygon quad = new Polygon();
	PImage img = loadImage("board1.jpg");
	ImageProcessing imageProcessing;
	
	public void setup(){
		size(1500, 800, P3D);
		imageProcessing = new ImageProcessing(this, false);
		noLoop();
		
	}
	
	public void draw(){
		imageProcessing.HBSFilter(img, 90, 140, 60, 190, 100, 255);
		//image(img,0,0);
		image(findConnectedComponents(img), 0, 0);
	}

	/**
	 * Create a blob detection instance with the four corners of the Lego board.
	 */
	public BlobDetection(PApplet applet, PVector c1, PVector c2, PVector c3, PVector c4) {
		quad.addPoint((int) c1.x, (int) c1.y);
		quad.addPoint((int) c2.x, (int) c2.y);
		quad.addPoint((int) c3.x, (int) c3.y);
		quad.addPoint((int) c4.x, (int) c4.y);
	}
	
	public BlobDetection(){}

	/** Returns true if a (x,y) point lies inside the quad */
	public boolean isInQuad(int x, int y) {
		return quad.contains(x, y);
	}

	public PImage findConnectedComponents(PImage input) {
		// First pass: label the pixels and store labels' equivalences
		int[] labels = new int[input.width * input.height];
		List<TreeSet<Integer>> labelsEquivalences = new ArrayList<TreeSet<Integer>>();
		labelsEquivalences.add(new TreeSet<Integer>());
		int currentLabel = 1;
		int pixelLabel;
		int neighbourLabel;
		for(int y = 1; y < input.height-1; y++){
			for(int x = 1; x < input.width-1; x++){
				//si le pixel n'est pas noir
				if(brightness(input.get(x, y)) > 0){
					labels[y*input.width+x] = currentLabel;
					//on parcourt tous ses voisins
					for(int j = y-1; j <= y+1; j++){
						for(int i = x-1; i <= x+1; i++){
							if(i != x || y != j){
								//si un des voisins a un label plus petit et non nul, on l'utilise comme nouveau label
								if(labels[input.width*j+i] > 0 && labels[input.width*j+i] < labels[input.width*y+x]){
									labels[y*input.width+x] = labels[input.width*j+i];
								}
							}
						}
					}
					//si après le parcours des voisins notre label est toujours currentLabel, on incremente currentLabel
					//et on cree une equivalence pour le nouveau label
					//System.out.println(labels[y*input.width+x]);
					if(labels[y*input.width+x] == currentLabel){
						TreeSet<Integer> newEqu = new TreeSet<>();
						newEqu.add(currentLabel);
						labelsEquivalences.add(newEqu);	
						currentLabel++;
					}
					pixelLabel = labels[y*input.width+x];
					//On re-parcourt les voisins pour assigner les labels equivalences
					for(int j = y-1; j <= y+1; j++){
						for(int i = x-1; i <= x+1; i++){
							if(brightness(input.get(x, y)) > 0){
								neighbourLabel = labels[j*input.width+i];
								if((x != i || y != j) && neighbourLabel != pixelLabel){
									//System.out.println("(" + neighbourLabel + ", " + pixelLabel + ")");
									TreeSet<Integer> pixelTree = labelsEquivalences.get(pixelLabel);
									TreeSet<Integer> neighbourTree = labelsEquivalences.get(neighbourLabel);
									labelsEquivalences.get(neighbourLabel).add(pixelLabel);
									labelsEquivalences.get(pixelLabel).add(neighbourLabel);
								}
							}
						}
					}
				}
			}
		}
		// Second pass: re-label the pixels by their equivalent class
		for(int y = 0; y < input.height; y++){
			for(int x = 0; x < input.width;x++){
				int pixel = input.get(x, y);
				//si le pixel n'est pas noir
				if(pixel != 0){
					pixelLabel = labels[y*input.width+x];
					int minEquivalentIndex = labelsEquivalences.get(pixelLabel).first();
					labels[y*input.width+x] = minEquivalentIndex;
				}
			}
		}
		// Finally, output an image with each blob colored in one uniform color.
		PImage output = createImage(input.width, input.height, RGB);
		int c = color(random(255f), random(255f), random(255f));
		output.set(0, 0, c);
			for(int y = 1; y < input.height; y++){
				for(int x = 1; x < input.width;x++){
					if(labels[y*input.width+x] > 0){
						System.out.println(labels[input.width*y+x]);
						if(labels[y*input.width+x] != labels[y*input.width+x-1]){
							c = color(random(255f), random(255f), random(255f));
						}
						output.set(x, y, c);
					}
			}
		}
		return output;
	}
}
