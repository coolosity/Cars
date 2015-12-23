package coolosity.cars.core.world.block;

import java.awt.image.BufferedImage;

import coolosity.cars.core.display.Resources;

public class BlockRoad extends Block
{

	public BlockRoad(int id)
	{
		super(id);
	}

	@Override
	public BufferedImage getImage(int data)
	{
		return Resources.getImage("road"+data);
	}
}
