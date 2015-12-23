package coolosity.cars.core.entities;

public class Location
{

	private double x;
	private double y;
	
	public Location(double x, double y)
	{
		this.x = x;
		this.y = y;
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
	
	public double distance(Location other)
	{
		return distance(other.x,other.y);
	}
	
	public double distance(double xx, double yy)
	{
		return Math.sqrt(Math.pow(xx-x, 2)+Math.pow(yy-y, 2));
	}
}
