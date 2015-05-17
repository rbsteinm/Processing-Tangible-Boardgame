package cs211.imageprocessing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;


import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import processing.video.Capture;

/**
 * @author rbsteinm
 * image processing part of the cs-211 game project.
 * Allows us to perform edge detection on the game board.
 */
@SuppressWarnings("serial")
public class ImageProcessing extends PApplet {
	private final int WHITE = color(255);
	private final int BLACK = color(0);
	
	//only lines with more than MIN_VOTES are drawn by hough alg
	//set accordingly to the the size of the picture
	private static final int MIN_VOTES = 100;
	private static final float DISCRETIZATION_STEPS_PHI = 0.06f;
	private static final float DISCRETIZATION_STEPS_R = 2.5f;
	
	private PImage img;
	private PImage houghAccImg;
	private Capture cam;
	
	private List<PVector> lines;
	private List<int[]> quadCycles;
	
	public void setup() {
		size(1200, 300);
		noLoop();
		//setupCamera();
		
	}
	
	public void setupCamera(){
		String[] cameras = Capture.list();
		if (cameras.length == 0) {
			println("There are no cameras available for capture.");
			exit();
		} else {
			println("Available cameras:");
			for (int i = 0; i < cameras.length; i++) {
				println(cameras[i]);
			}
			cam = new Capture(this, cameras[cameras.length-1]);
			cam.start();
		}
	}
	

	public void draw() {
		/*if (cam.available() == true) {
			cam.read();
		}
		img = cam.get();*/
		img = loadImage("board1.jpg");
		img.resize(img.width/2, img.height/2);
		PImage edgeImg = getEdgeImage(img);
		
		background(BLACK);
		
		lines = detectLines(edgeImg, 6);
		QuadGraph quadGraph = new QuadGraph();
		quadGraph.build(lines, img.width, img.height);
		quadCycles = filterQuads(quadGraph.findCycles());
		List<PVector> finalLines = getLines(quadCycles);
		
		image(img, 0, 0);
		plotLines(finalLines, edgeImg);
		//displayQuads(quadCycles);
		image(houghAccImg, img.width, 0);
		image(edgeImg, img.width*2, 0);
		
	}
	
	/**
	 * processes the given image with some filters and transformations to detect the board edges
	 * @param img
	 * @return a new image with highlighted board edges 
	 */
	public PImage getEdgeImage(PImage img){
		PImage resultImg;
		resultImg = hueFilter(img, 90, 140);
		resultImg = brightnessFilter(resultImg, 0, 200);
		resultImg = saturationFilter(resultImg, 100, 255);
		resultImg = (gaussianBlur(resultImg, 95));
		resultImg = binaryThreshold(resultImg, 30);
		resultImg = sobelAlgorithm(resultImg);
		return resultImg;
	}
	
	/**
	 * All pixels whose brigthness is over the threshold become white, others become black
	 * @param image
	 * @return filtered image
	 */
	public PImage binaryThreshold(PImage image,float threshold){
		PImage result = createImage(image.width, image.height, RGB);
		image.loadPixels();
		result.loadPixels();
		for(int i = 0; i < image.width * image.height; i++){
				if(brightness(image.pixels[i]) > threshold){
					result.pixels[i] = WHITE;
				}
				else{
					result.pixels[i] = BLACK;
				}
		}
		result.updatePixels();
		return result;
	}
	
	/**
	 * All pixels whose brigthness is over the threshold become black, others become white
	 * @param image
	 * @return filtered image
	 */
	public PImage invertedBinaryThreshold(PImage image){
		PImage result = createImage(image.width, image.height, RGB);
		image.loadPixels();
		result.loadPixels();
		for(int i = 0; i < image.width * image.height; i++){
			if(brightness(image.pixels[i]) < 128){
				result.pixels[i] = WHITE;
			}
			else{
					result.pixels[i] = BLACK;
			}
			result.loadPixels();
		}
		
		
		result.updatePixels();
		
		return result;
	}
	
	/**
	 * @param img
	 * @param min
	 * @param max
	 * @return the given image, replacing all pixels whose hue is not between maxHue and minHue by black pixels
	 */
	public PImage hueFilter(PImage img, float min, float max){
		PImage result = createImage(img.width, img.height, RGB);
		img.loadPixels();
		for(int i = 0; i < img.width*img.height; i++){
			float hue = hue(img.pixels[i]);
			if(hue >= min && hue <= max){
				result.pixels[i] = img.pixels[i];
			}
			else{
				result.pixels[i] = BLACK;
			}
		}
		result.updatePixels();
		return result;
	}
	
	/**
	 * @param img
	 * @param min brightness lowerbound
	 * @param max brightness upperbound
	 * @return the given image, replacing all pixels whose brightness is not between min and max by black pixels
	 */
	public PImage brightnessFilter(PImage img, float min, float max){
		PImage result = createImage(img.width, img.height, RGB);
		img.loadPixels();
		for(int i = 0; i < img.width*img.height; i++){
			float brightness = brightness(img.pixels[i]);
			if(brightness >= min && brightness <= max){
				result.pixels[i] = img.pixels[i];
			}
			else{
				result.pixels[i] = BLACK;
			}
		}
		result.updatePixels();
		return result;
	}
	
	/**
	 * @param img
	 * @param min saturation lowerbound
	 * @param max saturation upperbound
	 * @return the given image, replacing all pixels whose saturation is not between min and max by black pixels
	 */
	public PImage saturationFilter(PImage img, float min, float max){
		PImage result = createImage(img.width, img.height, RGB);
		img.loadPixels();
		for(int i = 0; i < img.width*img.height; i++){
			float saturation = saturation(img.pixels[i]);
			if(saturation >= min && saturation <= max){
				result.pixels[i] = img.pixels[i];
			}
			else{
				result.pixels[i] = BLACK;
			}
		}
		result.updatePixels();
		return result;
	}
	
	/**
	 * @param img
	 * @return the same image, convoluted by a given kernel
	 */
	public PImage convolute(PImage img, float[][] kernel, float weight){
		// create a greyscale image (type: ALPHA) for output
		PImage result = createImage(img.width, img.height, ALPHA);
		//loop that convolutes each pixel of the picture
		img.loadPixels();
		for(int x = 0; x < img.width; x++){
			for(int y = 0; y < img.height; y++){
				result.set(x, y, convolutePixel(x, y, img, kernel, weight));
			}
		}
		result.updatePixels();
		return result;
	}
	
	/**
	 * @param x x-position of the pixel
	 * @param y y-position of the pixel
	 * @param img image we took the pixel from
	 * @param kernel convolution matrix
	 * @return the new color of the pixel located in (x, y)
	 */
	public int convolutePixel(int x, int y, PImage img, float[][] kernel, float weight){
		assert(kernel.length == 3);
		assert(kernel[0].length == 3);
		float totalRed = 0;
		float totalGreen = 0;
		float totalBlue = 0;
		
		//ignoring border pixels
		int[][] result = {{img.get(x-1, y-1),img.get(x, y-1),img.get(x+1, y-1)},
						  {img.get(x-1, y), img.get(x, y),img.get(x-1, y+1)},
						  {img.get(x-1, y+1), img.get(x, y+1), img.get(x+1,y+1)}};
		if(x > 0 && x < img.width && y > 0 && y < img.height){
			for(int i = 0; i <= 2; i++){
				for(int j = 0; j <= 2; j++){
					totalRed += (red(result[i][j]) * kernel[i][j]);
					totalGreen += (green(result[i][j]) * kernel[i][j]);
					totalBlue += (blue(result[i][j]) * kernel[i][j]);
				}
			}
			//divide by the weight and make sure RGB values are not out of bound
			totalRed = constrain(totalRed/weight, 0, 255);
			totalGreen = constrain(totalGreen/weight, 0, 255);
			totalBlue = constrain(totalBlue/weight, 0, 255);
		}
		return color(totalRed, totalGreen, totalBlue);
	}
	
	/**
	 * @param img
	 * @param weight
	 * @return the given image, with a gaussian blur of the given weight applied on it
	 */
	public PImage gaussianBlur(PImage img, float weight){
		float[][] gaussianKernel = {{9, 12, 9},
									{12, 15, 12},
									{9, 12, 9}};
		
		return convolute(img, gaussianKernel, weight);
	}
	
	/**
	 * applies sobel algorithm (basic edge detection on the given image
	 * @param img
	 * @return the given image with sobel algorithm applied on it
	 */
	public PImage sobelAlgorithm(PImage img){
		float[][] hKernel = {{0, 1, 0},
							 {0, 0, 0},
							 {0, -1, 0}};
		float[][] vKernel = {{0, 0, 0},
							 {1, 0, -1},
							 {0, 0, 0}};
		
		PImage result = createImage(img.width, img.height, ALPHA);
		// clear the image
		for (int i = 0; i < img.width * img.height; i++) {
			result.pixels[i] = color(0);
		}
		
		
		float max = 0;
		float[] buffer = new float[img.width * img.height];
		float sumH;
		float sumV;
		//selecting each pixel, ignoring top/bottom/left/right edges
		img.loadPixels();
		for(int x = 2; x < img.width-2; x++){
			for(int y = 2; y < img.height-2; y++){
				//kernel sums for this pixel
				sumH = 0;
				sumV = 0;
				//Convoluting the pixel with its neighbours
				for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                            float val = brightness(img.pixels[(y + ky) * img.width + (x + kx)]);
                            sumH += hKernel[ky + 1][kx + 1] * val;
                            sumV += vKernel[ky + 1][kx + 1] * val;
                    }
				}
				float sum = sqrt(pow(sumH, 2) + pow(sumV, 2));
				if(sum > max){
					max = sum;
				}
				buffer[y*img.width + x] = sum;
			}
		}
		
		for (int y = 2; y < img.height - 2; y++) { // Skip top and bottom edges
			for (int x = 2; x < img.width - 2; x++) { // Skip left and right
				if (buffer[y * img.width + x] > (int) (max * 0.3f)) { // 30% of the max
					result.pixels[y * img.width + x] = color(255);
				} else {
					result.pixels[y * img.width + x] = color(0);
				}
			}
		}
		result.updatePixels();
		return result;
	}
	
	/**
	 * @param accumulator the array accumulator of the hough alg.
	 * @param rDim
	 * @param phiDim
	 * displays the content of the hough accumulator
	 */
	public PImage getHoughAccImg(int[] accumulator, int rDim, int phiDim) {
		PImage houghImg = createImage(rDim + 2, phiDim + 2, ALPHA);
		for (int i = 0; i < accumulator.length; i++) {
			houghImg.pixels[i] = color(min(255, accumulator[i]));
		}
		houghImg.updatePixels();
		houghImg.resize(img.width, img.height);
		return houghImg;
	}
	
	
	/**
	 * detects all the lines in the image that have at least
	 *  a certain number of votes with Hough algorithm 
	 * @param nLines
	 * @param image
	 * @return an array containing the nLines most voted lines found by Hough's alg
	 */
	public ArrayList<PVector> detectLines(PImage image, int nLines){
		// dimensions of the accumulator
		int phiDim = (int) (Math.PI / DISCRETIZATION_STEPS_PHI);
		int rDim = (int) (((image.width + image.height) * 2 + 1) / DISCRETIZATION_STEPS_R);
		
		// pre-compute the sin and cos values
		float[] tabSin = new float[phiDim+1];
		float[] tabCos = new float[phiDim+1];
		float ang = 0;
		float inverseR = 1.f / DISCRETIZATION_STEPS_R;
		for (int accPhi = 0; accPhi < phiDim; ang += DISCRETIZATION_STEPS_PHI, accPhi++) {
			tabSin[accPhi] = (float) (Math.sin(ang) * inverseR);
			tabCos[accPhi] = (float) (Math.cos(ang) * inverseR);
		}
		
		// our accumulator (with a 1 pix margin around)
		int[] accumulator = new int[(phiDim + 2) * (rDim + 2)];
		//array containing the best n candidate lines' indices in accumulator[]
		ArrayList<Integer> bestCandidates = new ArrayList<Integer>();
		
		// Fill the accumulator: on edge points (ie, white pixels of the edge
		// image), store all possible (r, phi) pairs describing lines going
		// through the point.
		for (int y = 0; y < image.height; y++) {
			for (int x = 0; x < image.width; x++) {
				// Are we on an edge?
				if (brightness(image.pixels[y * image.width + x]) != 0) {
					// ...determine here all the lines (r, phi) passing through
					// pixel (x,y), convert (r,phi) to coordinates in the
					// accumulator, and increment accordingly the accumulator.
					for(float phi = 0.0f; phi < Math.PI; phi += DISCRETIZATION_STEPS_PHI){
						int accPhi = Math.round(phi/DISCRETIZATION_STEPS_PHI);
						float r = (float)(x*tabCos[accPhi] + y*tabSin[accPhi]);
						int accR = Math.round((r) + ((rDim - 1)*(0.5f)));
						int idx = (accPhi + 1)*(rDim + 2) + accR + 1;
						accumulator[idx] += 1;
					}
				}
			}
		}
		
		houghAccImg = getHoughAccImg(accumulator, rDim, phiDim);
		
		// size of the region we search for a local maximum
		int neighbourhood = 10;
		// only search around lines with more that this amount of votes
		// (to be adapted to your image)
		for (int accR = 0; accR < rDim; accR++) {
			for (int accPhi = 0; accPhi < phiDim; accPhi++) {
				// compute current index in the accumulator
				int idx = (accPhi + 1) * (rDim + 2) + accR + 1;
				if (accumulator[idx] > MIN_VOTES) {
					boolean bestCandidate = true;
					// iterate over the neighbourhood
					for (int dPhi = -neighbourhood / 2; dPhi < neighbourhood / 2 + 1; dPhi++) {
						// check we are not outside the image
						if (accPhi + dPhi < 0 || accPhi + dPhi >= phiDim)
							continue;
						for (int dR = -neighbourhood / 2; dR < neighbourhood / 2 + 1; dR++) {
							// check we are not outside the image
							if (accR + dR < 0 || accR + dR >= rDim)
								continue;
							int neighbourIdx = (accPhi + dPhi + 1) * (rDim + 2)
									+ accR + dR + 1;
							if (accumulator[idx] < accumulator[neighbourIdx]) {
								// the current idx is not a local maximum!
								bestCandidate = false;
								break;
							}
						}
						if (!bestCandidate)
							break;
					}
					if (bestCandidate) {
						// the current idx *is* a local maximum
						bestCandidates.add(idx);
					}
				}
			}
		}
		
		//sort the lines, from the most to the least voted
		Collections.sort(bestCandidates, new HoughComparator(accumulator));
		
		//compute back R and PHI coordinates, then store the line they represent
		ArrayList<PVector> finalLines = new ArrayList<PVector>();
		for(int i = 0; i < min(nLines, bestCandidates.size()); i++){
			int idx = bestCandidates.get(i);
			// first, compute back the (r, phi) polar coordinates:
			int accPhi = (int) (idx / (rDim + 2)) - 1;
			int accR = idx - (accPhi + 1) * (rDim + 2) - 1;
			float r = (accR - (rDim - 1) * 0.5f) * DISCRETIZATION_STEPS_R;
			float phi = accPhi * DISCRETIZATION_STEPS_PHI;
			finalLines.add(new PVector(r, phi));
		}
		
		return finalLines;
	}
	
	/**
	 * plots the nLines first lines of the given lines array. Does nothing if the array is empty. 
	 * Also draws intersections between the plotted lines, if any.
	 * @param lines: lines detected by hough, ordered from the most voted to the least voted
	 * @param edgeImg 
	 * @param nLines number of lines to display
	 */
	public void plotLines(List<PVector> lines, PImage edgeImg){
		if(lines.size() > 0){
			//plot lines
			for (int i = 0; i < lines.size(); i++) {
				float r = lines.get(i).x;
				float phi = lines.get(i).y;
				
				// Cartesian equation of a line: y = ax + b
				// in polar, y = (-cos(phi)/sin(phi))x + (r/sin(phi))
				// => y = 0 : x = r / cos(phi)
				// => x = 0 : y = r / sin(phi)
				// compute the intersection of this line with the 4 borders of
				// the image
				int x0 = 0;
				int y0 = (int) (r / sin(phi));
				int x1 = (int) (r / cos(phi));
				int y1 = 0;
				int x2 = edgeImg.width;
				int y2 = (int) (-cos(phi) / sin(phi) * x2 + r / sin(phi));
				int y3 = edgeImg.width;
				int x3 = (int) (-(y3 - r / sin(phi)) * (sin(phi) / cos(phi)));
				// Finally, plot the lines
				stroke(204, 102, 0);
				if (y0 > 0) {
					if (x1 > 0)
						line(x0, y0, x1, y1);
					else if (y2 > 0)
						line(x0, y0, x2, y2);
					else
						line(x0, y0, x3, y3);
				} else {
					if (x1 > 0) {
						if (y2 > 0)
							line(x1, y1, x2, y2);
						else
							line(x1, y1, x3, y3);
					} else
						line(x2, y2, x3, y3);
				}
			}
			
			getIntersections(lines);
		}
	}
	
	/**
	 * draws the intersections between the given lines and returns them
	 * @param lines
	 * @return the coordinates of the potential intersections between
	 * all pairs of lines from the set given in parameter
	 */
	public ArrayList<PVector> getIntersections(List<PVector> lines) {
		ArrayList<PVector> intersections = new ArrayList<PVector>();
		for (int i = 0; i < lines.size() - 1; i++) {
			PVector line1 = lines.get(i);
			for (int j = i + 1; j < lines.size(); j++) {
				PVector line2 = lines.get(j);
				PVector intersection = getIntersection(line1, line2);
				intersections.add(intersection);
				//drawing the intersection
				fill(255, 128, 0);
				ellipse(intersection.x, intersection.y, 10, 10);
			}
		}
		return intersections;
	}
	
	/**
	 * @param line1
	 * @param line2
	 * @return returns the coordinates of the intersection of the 2 given lines
	 */
	public PVector getIntersection(PVector line1, PVector line2){
		float d = (float)(Math.cos(line2.y)*Math.sin(line1.y) - Math.cos(line1.y)*Math.sin(line2.y));
		float x = (float)((line2.x*Math.sin(line1.y) - line1.x*Math.sin(line2.y))/d);
		float y = (float)((-line2.x*Math.cos(line1.y) + line1.x*Math.cos(line2.y))/d);
		return new PVector(x, y);
	}
	
	/**
	 * @param quad
	 * @return the lines of the detected quads
	 */
	public List<PVector> getLines(List<int[]> quadCycles){
		List<PVector> quadLines = new ArrayList<PVector>();
		if(quadCycles.size() > 0){
			//arbitrary selecting the first quad of quadCycles
			//TODO find a way to select the most accurate quad
			int[] quad = quadCycles.get(0);
			PVector l1 = lines.get(quad[0]);
			PVector l2 = lines.get(quad[1]);
			PVector l3 = lines.get(quad[2]);
			PVector l4 = lines.get(quad[3]);
			quadLines.add(l1);
			quadLines.add(l2);
			quadLines.add(l3);
			quadLines.add(l4);
		}
		return quadLines;
	}
	
	/**
	 * filters the given quads list, removing:
	 * non-convex quads
	 * too small, too big quads
	 * (almost) flat quads
	 * @param quadGraph
	 * @return 
	 * @return
	 */
	public List<int[]> filterQuads(List<int[]> quads){
		List<int[]> remainingQuads = new ArrayList<int[]>();
		for(int[] quad: quads){
			PVector l1 = lines.get(quad[0]);
			PVector l2 = lines.get(quad[1]);
			PVector l3 = lines.get(quad[2]);
			PVector l4 = lines.get(quad[3]);
			PVector c12 = getIntersection(l1, l2);
			PVector c23 = getIntersection(l2, l3);
			PVector c34 = getIntersection(l3, l4);
			PVector c41 = getIntersection(l4, l1);
			//TODO skipped validArea checking, as asked for milestone 3
			if(QuadGraph.isConvex(c12, c23, c34, c41) && QuadGraph.nonFlatQuad(c12, c23, c34, c41)){
				remainingQuads.add(quad);
			}
		}
		System.out.println((quads.size() - remainingQuads.size()) + " quads filtered; " + remainingQuads.size() + " remaining.");
		return remainingQuads;
	}
	
	/**
	 * @param quadGraph
	 * displays the given quad graph
	 */
	public void displayQuads(List<int[]> quads){
		for (int[] quad : quads) {
			PVector l1 = lines.get(quad[0]);
			PVector l2 = lines.get(quad[1]);
			PVector l3 = lines.get(quad[2]);
			PVector l4 = lines.get(quad[3]);
			PVector c12 = getIntersection(l1, l2);
			PVector c23 = getIntersection(l2, l3);
			PVector c34 = getIntersection(l3, l4);
			PVector c41 = getIntersection(l4, l1);
			// Choose a random, semi-transparent colour
			Random random = new Random();
			fill(color(min(255, random.nextInt(300)),
					min(255, random.nextInt(300)),
					min(255, random.nextInt(300)), 50));
			quad(c12.x, c12.y, c23.x, c23.y, c34.x, c34.y, c41.x, c41.y);
		}
		System.out.println(quads.size() + " quad(s) displayed");
	}
	
	

	class HoughComparator implements Comparator<Integer> {
		int[] accumulator;
		public HoughComparator(int[] accumulator) {
		this.accumulator = accumulator;
		}
		@Override
		public int compare(Integer l1, Integer l2) {
		if (accumulator[l1] > accumulator[l2]
		|| (accumulator[l1] == accumulator[l2] && l1 < l2)) return -1;
		return 1;
		}
	}


}
