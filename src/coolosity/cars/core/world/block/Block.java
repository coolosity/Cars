package coolosity.cars.core.world.block;

import java.awt.image.BufferedImage;

import coolosity.cars.core.display.Resources;

public class Block
{
	private int id;
	
	public Block(int id)
	{
		this.id = id;
	}
	
	public int getID()
	{
		return id;
	}
	
	public BufferedImage getImage(int data)
	{
		return Resources.getImage("block"+id);
	}
}