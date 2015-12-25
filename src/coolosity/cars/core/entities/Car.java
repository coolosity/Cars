package coolosity.cars.core.entities;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import coolosity.cars.core.display.Resources;

public class Car extends Entity
{
	
	private double maxSpeed, maxRotSpeed;
	private double speed, rotSpeed;
	private double accel, rotAccel;
	private double deccel, rotDeccel;
	
	public Car(Location loc, double maxSpeed, double maxRotSpeed, double accel, double rotAccel, double deccel, double rotDeccel) {
		super(loc, 2.0, 1.3, 0);
		this.maxSpeed = maxSpeed;
		this.maxRotSpeed = maxRotSpeed;
		this.accel = accel;
		this.rotAccel = rotAccel;
		this.deccel = deccel;
		this.rotDeccel = rotDeccel;
	}

	@Override
	public void draw(BufferedImage img, int xloc, int yloc, int w, int h, double rotation) {
		BufferedImage car = Resources.getImage("car");
		Graphics2D g = (Graphics2D) img.getGraphics();
		g.translate(xloc+w/2, yloc+h/2);
		g.rotate(Math.toRadians(rotation));
		g.drawImage(car, -w/2, -h/2, w, h, null);
	}

	public double getMaxSpeed()
	{
		return maxSpeed;
	}
	
	public double getMaxRotSpeed()
	{
		return maxRotSpeed;
	}
	
	public double getSpeed()
	{
		return speed;
	}
	
	public void setSpeed(double speed)
	{
		this.speed = speed;
	}
	
	public double getRotSpeed()
	{
		return rotSpeed;
	}
	
	public void setRotSpeed(double rotSpeed)
	{
		this.rotSpeed = rotSpeed;
	}
	
	public double getAccel()
	{
		return accel;
	}
	
	public double getDeccel()
	{
		return deccel;
	}
	
	public double getRotAccel()
	{
		return rotAccel;
	}
	
	public double getRotDeccel()
	{
		return rotDeccel;
	}
}
