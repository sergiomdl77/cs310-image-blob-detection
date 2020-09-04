import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.AbstractCollection;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;

import javax.swing.JPanel;

/**
 * Declaration of class that detects the blobs of a given color from an image file.
 * This class extends JPanel.
 * @author Sergio Delgado
 */
public class Detector extends JPanel
{
	/**
	 * Gets the percentual difference between two colors (in terms of their RGB components).
	 * @param c1 Color object, first color to compare.
	 * @param c2 Color object, second color to compare.
	 * @return int value percentual difference between the colors.
	 */
	public static int getDifference(Color c1, Color c2) 
	{
		int colorDif;
		int redDif;
		int greenDif;
		int blueDif;
		
		redDif = c1.getRed() - c2.getRed();
		greenDif = c1.getGreen() - c2.getGreen();
		blueDif = c1.getBlue() - c2.getBlue();
		
		colorDif = (redDif*redDif) + (greenDif*greenDif) + (blueDif*blueDif);
		
		colorDif = (colorDif * 100)/ (255*255*3);
		
		return colorDif; //replace this
	}
	

	/**
	 * Color the pixels white (if the pixel is not color we want) or black (if it's the color we want).
	 * @param image BufferedImage which contains the collection of pixels of the image file.
	 * @param c Color object that represents the color to be searched for in each pixel of the image.
	 * @param okDist int value that indicates the acceptable "distance" between the pixel and the color c (inclusive).
	 */
	public static void thresh(BufferedImage image, Color c, int okDist)
	{
		int width = image.getWidth();
		int height = image.getHeight();
		Color pColor = null;
		Pixel p = null;
		int pID = 0;
		Color mc = null;
		
		for (int y=0; y<height; y++)	// traversing every row of pixels of the image.
		{
			for (int x=0; x<width; x++)		// traversing every column of pixels of the image
			{
				pID = getId(image, x,  y);
				p = getPixel(image, pID);
				pColor = getColor(image, p);	// getting the color of the pixel <x,y>

				if (getDifference(c, pColor) <= okDist)	// if color of pixel within acceptable range
				{	
					image.setRGB(x,y,Color.BLACK.getRGB());	//	set pixel's color to BLACK
					mc = pColor;
				}
				else									// if color out of acceptable range
					
					image.setRGB(x,y,Color.WHITE.getRGB());	// 	set pixe's color to WHITE
			}
		}
	}
	
	
	/**
	 * Given an image, a disjoint set, and a pixel (defined by its id), return a pair which contains:
	 * (a) the blob above and (b) the blob to the left (each represented by their _root_ ids).
	 * If there is no above/left neighbor, then the appropriate part of the pair will be null.
	 * @param image BufferedImage object, with collection of pixels from image.
	 * @param ds Disjoint Sets containing the blobs of the image.
	 * @param pixelId int representing id of the pixel whose neighbors are being analized
	 * @return Pair<Integer,Integer> which holds the ids of the two pixels (a=left, b=above).
	 */
	public static Pair<Integer,Integer> getNeighborSets(BufferedImage image, DisjointSets<Pixel> ds, int pixelId)
	{
		Integer a = null;
		Integer b = null;
		Pixel p = getPixel(image, pixelId);
		int x = p.a;	// x component of the parameter pixelId
		int y = p.b;	// y component of the parameter pixelId

		// GETTING THE NEIGHBOR TO THE LEFT	
		if (x != 0)		// if the pixel above is not in the first column of the image
			a = ds.find(getId(image,x-1,y));
		// GETTING THE NEIGHBOR TO THE TOP
		if (y != 0)		// if the pixel is on the first column of the image
			b = ds.find(getId(image,x,y-1));
		
	
		return new Pair<>(a,b); 
	}
	
	/**
	 * Turns pixels of acceptable color to Black and rest of pixels to White. Then traverses the black and white
	 * image (pixel by pixel) looking for same color neighbors (left and above), and unioning them with the currently
	 * visited pixel.
	 */
	public void detect() 
	{
		thresh(this.img, this.blobColor , this.okDist);	// Turning the color acceptable pixels of image to black,
														// and the rest of the image to white color.
		ArrayList<Pixel> arr = new ArrayList<>();

		int currentPixel = 0;
		for (int x=0; x<img.getWidth(); x++)	// initializing the array of pixels to give as parameter for ds constructor.
		{
			for (int y=0; y<img.getHeight(); y++)
			{
				arr.add(getPixel(img,currentPixel));
				currentPixel++;
			}
		}

		int totalPixels = currentPixel;
		
		ds = new DisjointSets<Pixel>(arr);  // Creating the disjoint set for this detector

		Pair<Integer,Integer> currentNeighbors; 
		for (currentPixel=0; currentPixel<totalPixels; currentPixel++)	// Traversing every pixel of image
		{
			currentNeighbors = getNeighborSets(img, ds, currentPixel); 
			
			if (currentNeighbors.a != null)   // if there is a left neighbor
			{
				if ( getDifference(getColor(img, getPixel(img, currentNeighbors.a)), getColor(img, getPixel(img,currentPixel)) ) == 0) 
				{			
					ds.union(currentPixel, currentNeighbors.a); // union it 
				}
			}
			if (currentNeighbors.b != null)   // if there is an above neighbor
			{
				if ( getDifference(getColor(img, getPixel(img, currentNeighbors.b)), getColor(img, getPixel(img, currentPixel)) ) == 0)
				{
					ds.union(ds.find(currentPixel), currentNeighbors.b); // union it
				}
			}
		}
	}

	/**
	 * Recolor all pixels in the k largest blobs and save output
	 * @param outputFileName name of the resulting image file.
	 * @param outputECFileName name of the resulting image file.
	 * @param k int value with the number of blobs to be detected.
	 */
	public void outputResults(String outputFileName, String outputECFileName, int k)
	{
		if(k<1)
			throw new IllegalArgumentException(new String("! Error: k should be greater than 0, current k="+k));
		
		/**
		 * This class is created to override Comparator of TreeSet
		 */
		class SetComp implements Comparator<Set<Pixel>>   
		{
		     @Override
		    public int compare(Set<Pixel> s1, Set<Pixel> s2) {
		        if(s1.size == s2.size){
		            return 0;
		        } if(s1.size < s2.size){
		            return -1;
		        } else {
		            return 1;
		        }
		    }
		}		
		
		// This is the declaration of the tree that will store and access (in ascending order) the blob sets.
		// Each element of the tree will have a value of type Set<Pixel> in which 
		TreeSet<Set<Pixel>> myTree = new TreeSet<>(new SetComp());  // 
		
		int imgSize = this.img.getHeight() * this.img.getWidth();
		
		for (int currPixId=0; currPixId<imgSize; currPixId++)	// traversing the whole image 
		{
			// if currently visited pixel's color is BLACK, we add that contains it to myTree.
			// myTree only accepts unique values, so it the set has been added, it won't be added as a duplicate.
			if ( getDifference( getColor(this.img, getPixel(this.img, currPixId)), Color.BLACK) == 0)	
				myTree.add(ds.get(currPixId));
		}
		
		Set<Pixel> biggestBlob = myTree.last();
		int i = 0;
		while (i<k && biggestBlob != null)	// Traversing myTree in descending order to find the k biggest sets (blobs)
		{
			System.out.println("blob: "+ (i+1) + ",  " + "size: " + biggestBlob.size());
			for (Pixel p: biggestBlob)			// Traversing the set to recolor each pixel in it
			{
				this.img.setRGB(p.a, p.b, getSeqColor(i,k).getRGB() );
			}
				
			biggestBlob = myTree.lower(biggestBlob);
			i++;
		}
		
		//save output image -- provided
		try {
			File ouptut = new File(outputFileName);
			ImageIO.write(this.img, "png", ouptut);
			System.err.println("- Saved result to "+outputFileName);
		}
		catch (Exception e) {
			System.err.println("! Error: Failed to save image to "+outputFileName);
		}
		
	}
	
	public static void main(String[] args) 
	{
		
		String path = "/home/sergio/eclipse-workspace/CS310-project4/src/";
		String fileName = "06_Toyblocks";
		String inputFileName = path + fileName + ".jpg";
		int r =50;
		int g =50;
		int b =220;
		int k =1;
		int myOkDist = 5;
		Detector myDetector = new Detector(inputFileName, new Color(r,g,b), myOkDist);
	
		String outputFileName = path + fileName + "_Result.png";
		
		String outputECFileName = null;

		myDetector.detect();
			
		myDetector.outputResults(outputFileName, outputECFileName, k);
		
	}

	//-----------------------------------------------------------------------
	//
	// Todo: Read and provide comments, but do not change the following code
	//
	//-----------------------------------------------------------------------

	//Data
	public BufferedImage img;        //this is the 2D array of RGB pixels
	private Color blobColor;         //the color of the blob we are detecting
	private String imgFileName;      //input image file name
	private DisjointSets<Pixel> ds;  //the disjoint set
	private int okDist;              //the distance between blobColor and the pixel which "still counts" as the color

	// constructor, read image from file
	public Detector(String imgfile, Color blobColor, int okDist) {
		this.imgFileName=imgfile;
		this.blobColor = blobColor;
		this.okDist = okDist;
		
		reloadImage();
	}

	// constructor, read image from file
	public void reloadImage() {
		File imageFile = new File(this.imgFileName);
		
		try {
			this.img = ImageIO.read(imageFile);
		}
		catch(IOException e) {
			System.err.println("! Error: Failed to read "+this.imgFileName+", error msg: "+e);
			return;
		}
	}

	// JPanel function
	public void paint(Graphics g) {
		g.drawImage(this.img, 0, 0,this);
	}

	//private classes below

	//Convenient helper class representing a pair of things
	private static class Pair<A,B> {
		A a;
		B b;
		public Pair(A a, B b) {
			this.a=a;
			this.b=b;
		}
	}

	//A pixel is a set of locations a (aka. x, distance from the left) and b (aka. y, distance from the top)
	private static class Pixel extends Pair<Integer, Integer> {
		public Pixel(int x, int y) {
			super(x,y);
		}
	}

	//Convert a pixel in an image to its ID
	private static int getId(BufferedImage image, Pixel p) {
		return getId(image, p.a, p.b);
	}

	//Convex ID for an image back to a pixel
	private static Pixel getPixel(BufferedImage image, int id) {
		int y = id/image.getWidth();
		int x = id-(image.getWidth()*y);

		if(y<0 || y>=image.getHeight() || x<0 || x>=image.getWidth())
			throw new ArrayIndexOutOfBoundsException();

		return new Pixel(x,y);
	}

	//Converts a location in an image into an id
	private static int getId(BufferedImage image, int x, int y) {
		return (image.getWidth()*y)+x;
	}

	//Returns the color of a given pixel in a given image
	private static Color getColor(BufferedImage image, Pixel p) {
		return new Color(image.getRGB(p.a, p.b));
	}
	
	//Pass 0 -> k-1 as i to get the color for the blobs 0 -> k-1
	private Color getSeqColor(int i, int max) {
		if(i < 0) i = 0;
		if(i >= max) i = max-1;
		
		int r = (int)(((max-i+1)/(double)(max+1)) * blobColor.getRed());
		int g = (int)(((max-i+1)/(double)(max+1)) * blobColor.getGreen());
		int b = (int)(((max-i+1)/(double)(max+1)) * blobColor.getBlue());
		
		if(r == 0 && g == 0 && b == 0) {
			r = g = b = 10;
		}
		else if(r == 255 && g == 255 && b == 255) {
			r = g = b = 245;
		}
		
		return new Color(r, g, b);
	}
}
