package coolosity.cars.core.display;

import coolosity.cars.core.entities.Location;

public class Camera
{
	
	private double x;
	private double y;
	private double zoom;
	
	public Camera(double x, double y, double zoom)
	{
		this.x = x;
		this.y = y;
		this.zoom = zoom;
	}
	
	public void setLocation(Location l)
	{
		setX(l.getX());
		setY(l.getY());
	}
	
	public double getX()
	{
		return x;
	}
	
	public void setX(double x)
	{
		this.x = x;
	}
	
	public double getY()
	{
		return y;
	}
	
	public void setY(double y)
	{
		this.y = y;
	}
	
	public double getZoom()
	{
		return zoom;
	}
	
	public void setZoom(double zoom)
	{
		this.zoom = zoom;
	}
}
