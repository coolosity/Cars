package coolosity.cars.core.world;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Random;

import coolosity.cars.core.CarsException;
import coolosity.cars.core.CarsLogger;
import coolosity.cars.core.display.Camera;
import coolosity.cars.core.display.Resources;
import coolosity.cars.core.entities.Entity;
import coolosity.cars.core.entities.Location;
import coolosity.cars.core.world.block.Block;

public class CarsWorld
{
	
	public static CarsWorld loadFromFile(File file)
	{
		if(!file.exists())
			throw new CarsException("Trying to load world from file "+file.getPath()+" but it doesn't exist!");
		
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String[] data = reader.readLine().split(" ");
			int width = Integer.parseInt(data[0]);
			int height = Integer.parseInt(data[1]);
			CarsWorld world = new CarsWorld(width,height);
			for(int y=0;y<height;y++)
			{
				String[] line = reader.readLine().split(" ");
				if(line.length!=width)
				{
					reader.close();
					throw new CarsException("World is corrupted! From file "+file.getPath()+" on line "+y+2);
				}
				for(int x=0;x<line.length;x++)
				{
					String[] dt = line[x].split("-");
					world.blocks[x][y] = Blocks.fromID(Integer.parseInt(dt[0]));
					world.data[x][y] = Integer.parseInt(dt[1]);
				}
			}
			reader.close();
			return world;
		}
		catch(Exception e)
		{
			CarsLogger.err("An error occured while loading world from file "+file.getPath()+":");
			e.printStackTrace();
			return null;
		}
	}
	
	public static void saveToFile(CarsWorld world, File file)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
			writer.write(world.getWidth()+" "+world.getHeight());
			writer.newLine();
			for(int y=0;y<world.getHeight();y++)
			{
				for(int x=0;x<world.getWidth();x++)
				{
					Block b = world.blocks[x][y];
					writer.write(b.getID()+"-"+world.data[x][y]);
					if(x<world.getWidth()-1)writer.write(" ");
				}
				writer.newLine();
			}
			writer.close();
		}
		catch(Exception e)
		{
			CarsLogger.err("An error occured while saving world to file "+file.getPath()+":");
			e.printStackTrace();
		}
	}
	
	public static CarsWorld generate(WorldGenerator generator)
	{
		Random r = new Random();
		r.setSeed(generator.getSeed());
		CarsWorld world = new CarsWorld(generator.getWidth(),generator.getHeight());
		generator.generate(world.blocks, world.data, r);
		return world;
	}
	
	private Block[][] blocks;
	private int[][] data;
	private ArrayList<Entity> entities;
	
	private CarsWorld(int width, int height)
	{
		this.blocks = new Block[width][height];
		this.data = new int[width][height];
		this.entities = new ArrayList<Entity>();
	}
	
	public void addEntity(Entity entity)
	{
		entities.add(entity);
	}
	
	public void draw(BufferedImage img, Camera camera)
	{
		int camblockview = 16;
		
		if(camera.getRotate())
		{
			int size = img.getWidth()*2;
			boolean grid = false;
			boolean coords = false;
			double rot = Math.toRadians(camera.getRot());
			BufferedImage ori = new BufferedImage(size,size,img.getType());
			Graphics g = ori.getGraphics();
			{
				int cbv = camblockview*2;
				double tlx = camera.getX()-cbv/2;
				double tly = camera.getY()-cbv/2;
				double ppx = ori.getWidth()*1.0/cbv;
				double ppy = ori.getHeight()*1.0/cbv;
				double xoff = tlx-((int)tlx);
				double yoff = tly-((int)tly);
				g.setColor(Color.RED);
				
				//blocks
				for(int x=0;x<cbv;x++)
				{
					for(int y=0;y<cbv;y++)
					{
						int xx = (int) (x+tlx);
						int yy = (int) (y+tly);
						if(xx>=0 && yy>=0 && xx<blocks.length && yy<blocks[0].length)
						{
							Block b = blocks[xx][yy];
							g.drawImage(Resources.getImage("block"+b.getID()), (int)((x-xoff)*ppx), (int)((y-yoff)*ppy), (int)(ppx)+1, (int)(ppy)+1, null);
							if(coords)
								g.drawString(xx+" "+yy, (int)((x-xoff)*ppx+ppx/3), (int)((y-yoff)*ppy+ppy/3));
						}
					}
				}
				
				//grid
				g.setColor(Color.WHITE);
				if(grid)
				{
					int t = 4;
					for(int x=0;x<cbv;x++)
					{
						g.fillRect((int)((x-xoff)*ppx)-t/2, 0, t, ori.getHeight());
					}
					for(int y=0;y<cbv;y++)
					{
						g.fillRect(0, (int)((y-yoff)*ppy)-t/2, ori.getWidth(), t);
					}
				}
				
				//entities
				for(Entity entity : entities)
				{
					if(entity.getLocation().distance(camera.getX(),camera.getY())<Math.sqrt(2*Math.pow(camblockview, 2)))
					{
						Location l = entity.getLocation();
						entity.draw(ori, (int)((l.getX()-tlx)*ppx), (int)((l.getY()-tly)*ppy), (int)(entity.getWidth()*ppx), (int)(entity.getHeight()*ppy), entity.getRotation());
					}
				}
			}
			
			AffineTransform trans = new AffineTransform();
			trans.rotate(rot);
			Point pt = new Point(ori.getWidth()/2,ori.getHeight()/2);
			Point2D ptr = trans.transform(pt, null);
			
			AffineTransform identity = new AffineTransform();
			identity.translate(ori.getWidth()/2-ptr.getX(), ori.getHeight()/2-ptr.getY());
			identity.rotate(rot);
			
			BufferedImage rotated = new BufferedImage(ori.getWidth(),ori.getHeight(),ori.getType());
			Graphics2D gg = (Graphics2D) rotated.getGraphics();
			gg.drawImage(ori, identity, null);
			
			int xsize = (int) (rotated.getWidth()/camera.getZoom());
			int ysize = (int) (rotated.getHeight()/camera.getZoom());
			BufferedImage small = rotated.getSubimage((rotated.getWidth()-xsize)/2, (rotated.getHeight()-ysize)/2, xsize, ysize);
			BufferedImage scaled = new BufferedImage(rotated.getWidth(),rotated.getHeight(),rotated.getType());
			Graphics gs = scaled.getGraphics();
			gs.drawImage(small, 0, 0, scaled.getWidth(), scaled.getHeight(), null);
			
			
			BufferedImage todraw = scaled;
			
			Graphics gi = img.getGraphics();
			gi.drawImage(todraw.getSubimage((todraw.getWidth()-img.getWidth())/2, (todraw.getHeight()-img.getHeight())/2, img.getWidth(), img.getHeight()),0,0,img.getWidth(),img.getHeight(),null);
		}
		else
		{
			boolean grid = false;
			boolean coords = false;
			Graphics g = img.getGraphics();
			{
				double cbvx = camblockview;
				double cbvy = img.getHeight()*1.0/img.getWidth()*cbvx;
				double tlx = camera.getX()-cbvx/2;
				double tly = camera.getY()-cbvy/2;
				double ppx = img.getWidth()*1.0/cbvx;
				double ppy = img.getHeight()*1.0/cbvy;
				double xoff = tlx-((int)tlx);
				double yoff = tly-((int)tly);
				g.setColor(Color.RED);
				
				//blocks
				for(int x=0;x<cbvx+1;x++)
				{
					for(int y=0;y<cbvy+1;y++)
					{
						int xx = (int) (x+tlx);
						int yy = (int) (y+tly);
						if(xx>=0 && yy>=0 && xx<blocks.length && yy<blocks[0].length)
						{
							Block b = blocks[xx][yy];
							g.drawImage(Resources.getImage("block"+b.getID()), (int)((x-xoff)*ppx), (int)((y-yoff)*ppy), (int)(ppx)+1, (int)(ppy)+1, null);
							if(coords)
							{
								g.drawString(xx+"", (int)((x-xoff)*ppx+ppx/3), (int)((y-yoff)*ppy+ppy/3));
								g.drawString(yy+"", (int)((x-xoff)*ppx+ppx/3), (int)((y-yoff)*ppy+ppy/3)+10);
							}
						}
					}
				}
				
				//grid
				g.setColor(Color.WHITE);
				if(grid)
				{
					int t = 4;
					for(int x=0;x<cbvx+1;x++)
					{
						g.fillRect((int)((x-xoff)*ppx)-t/2, 0, t, img.getHeight());
					}
					for(int y=0;y<cbvy+1;y++)
					{
						g.fillRect(0, (int)((y-yoff)*ppy)-t/2, img.getWidth(), t);
					}
				}
				
				//entities
				for(Entity entity : entities)
				{
					if(entity.getLocation().distance(camera.getX(),camera.getY())<Math.sqrt(2*Math.pow(camblockview, 2)))
					{
						Location l = entity.getLocation();
						entity.draw(img, (int)((l.getX()-tlx)*ppx), (int)((l.getY()-tly)*ppy), (int)(entity.getWidth()*ppx), (int)(entity.getHeight()*ppy), entity.getRotation());
					}
				}
			}
		}
	}
	
	public int getWidth()
	{
		return blocks.length;
	}
	
	public int getHeight()
	{
		return blocks[0].length;
	}
}
