package coolosity.cars.core.world;

import java.lang.reflect.Field;

import coolosity.cars.core.world.block.Block;
import coolosity.cars.core.world.block.BlockRoad;

public class Blocks {

	public static final Block grass = new Block(0);
	public static final Block road = new BlockRoad(1);
	
	public static Block fromID(int id)
	{
		try
		{
			for(Field field : Blocks.class.getFields())
			{
				if(field.getType() == Block.class)
				{
					Block b = (Block) field.get(null);
					if(b.getID() == id)
						return b;
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
}
