package coolosity.cars.core.entities;

import java.awt.image.BufferedImage;

public abstract class Entity
{

	protected Location location;
	protected double width;
	protected double height;
	protected double rotation;
	
	public Entity(Location location, double width, double height, double rotation)
	{
		this.location = location;
		this.width = width;
		this.height = height;
		this.rotation = rotation;
	}
	
	public abstract void draw(BufferedImage img, int xloc, int yloc, int w, int h, double rotation);
	
	public Location getLocation()
	{
		return location;
	}
	
	public double getWidth()
	{
		return width;
	}
	
	public double getHeight()
	{
		return height;
	}
	
	public double getRotation()
	{
		return rotation;
	}
	
	public void setRotation(double rotation)
	{
		this.rotation = rotation;
	}
}
