package coolosity.cars.core.display;

import coolosity.cars.core.entities.Location;

public class Camera
{
	
	private double x;
	private double y;
	private double zoom;
	private double rot;
	private boolean rotate;
	
	public Camera(double x, double y, double zoom, double rot, boolean rotate)
	{
		this.x = x;
		this.y = y;
		this.zoom = zoom;
		this.rot = rot;
		this.rotate = rotate;
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
	
	public double getRot()
	{
		return rot;
	}
	
	public void setRot(double rot)
	{
		this.rot = rot;
	}
	
	public boolean getRotate()
	{
		return rotate;
	}
	
	public void setRotate(boolean rotate)
	{
		this.rotate = rotate;
	}
}
