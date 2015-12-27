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
		"road", "17"
	};
	private static final String[] IMGS = {
		"car"
	};
	private static final String[] BUILDINGS = {
		"bank"
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
			for(String s : BUILDINGS)
			{
				images.put("building_"+s, loadImage("res/building_"+s+".png"));
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
	
	public static BufferedImage getImage(String key, int rot)
	{
		BufferedImage img = getImage(key);
		if(img==null)return null;
		BufferedImage ret = null;
		switch(rot)
		{
		case 0:
			ret = img;
			break;
		case 1:
			ret = new BufferedImage(img.getHeight(),img.getWidth(),img.getType());
			for(int x=0;x<img.getWidth();x++)
			{
				for(int y=0;y<img.getHeight();y++)
				{
					ret.setRGB(img.getHeight()-1-y, x, img.getRGB(x, y));
				}
			}
			break;
		case 2:
			ret = new BufferedImage(img.getWidth(),img.getHeight(),img.getType());
			for(int x=0;x<img.getWidth();x++)
			{
				for(int y=0;y<img.getHeight();y++)
				{
					ret.setRGB(img.getWidth()-1-x, img.getHeight()-1-y, img.getRGB(x, y));
				}
			}
			break;
		case 3:
			ret = new BufferedImage(img.getHeight(),img.getWidth(),img.getType());
			for(int x=0;x<img.getWidth();x++)
			{
				for(int y=0;y<img.getHeight();y++)
				{
					ret.setRGB(y, img.getWidth()-1-x, img.getRGB(x, y));
				}
			}
			break;
		}
		return ret;
	}
}
