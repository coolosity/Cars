package coolosity.cars.core.display;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;

import javax.imageio.ImageIO;

import coolosity.cars.core.CarsException;
import coolosity.cars.core.CarsLogger;

public class Resources
{
	private static final int[] BLOCKIDS = {0};
	private static final String[] DATAIDS = {
		"road", "16"
	};
	private static final String[] IMGS = {
		"car"
	};
	
	private static HashMap<String,BufferedImage> images;
	
	public static void loadResources()
	{
		try
		{
			images = new HashMap<String,BufferedImage>();
			for(int id : BLOCKIDS)
			{
				images.put("block"+id, loadImage("res/block"+id+".png"));
			}
			for(int i=0;i<DATAIDS.length;i+=2)
			{
				String block = DATAIDS[i];
				int max = Integer.parseInt(DATAIDS[i+1]);
				for(int j=0;j<max;j++)
				{
					images.put(block+j, loadImage("res/"+block+j+".png"));
				}
			}
			for(String s : IMGS)
			{
				images.put(s, loadImage("res/"+s+".png"));
			}
		}
		catch(Exception e)
		{
			CarsLogger.err("An error occured while loading resources");
			e.printStackTrace();
		}
	}
	
	private static BufferedImage loadImage(String path)
	{
		File file = new File(path);
		if(!file.exists())
		{
			throw new CarsException("Image does not exist at path "+path);
		}
		try
		{
			BufferedImage img = ImageIO.read(file);
			return img;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new CarsException("Could not load image from path "+path);
		}
	}
	
	public static BufferedImage getImage(String key)
	{
		if(!images.containsKey(key))
			return null;
		return images.get(key);
	}
}
