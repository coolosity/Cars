package coolosity.cars.core.world;

import java.awt.image.BufferedImage;

import coolosity.cars.core.display.Resources;

public class Building
{

	private int width, height;
	private String img;
	
	public Building(int width, int height, String img)
	{
		this.width = width;
		this.height = height;
		this.img = img;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public void draw(BufferedImage img, int x, int y, int width, int height, int rot)
	{
		img.getGraphics().drawImage(Resources.getImage(this.img,rot), x, y, width, height, null);
	}
}
