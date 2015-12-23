package coolosity.cars.core.world;

import java.util.Random;

import coolosity.cars.core.world.block.Block;

public class WorldGenerator
{

	private int width, height;
	private long seed;
	
	public WorldGenerator(int width, int height)
	{
		this(width,height,System.currentTimeMillis());
	}
	
	public WorldGenerator(int width, int height, long seed)
	{
		this.width = width;
		this.height = height;
		this.seed = seed;
	}
	
	public void generate(Block[][] blocks, int[][] data, Random r)
	{
		for(int x=0;x<blocks.length;x++)
		{
			for(int y=0;y<blocks[0].length;y++)
			{
				blocks[x][y] = Blocks.grass;
			}
		}
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public long getSeed()
	{
		return seed;
	}
}
