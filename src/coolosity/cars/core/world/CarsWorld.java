package coolosity.cars.core.world;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
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
import coolosity.cars.core.entities.Entity;
import coolosity.cars.core.entities.Location;
import coolosity.cars.core.world.block.Block;

public class CarsWorld
{
	private static class WorldBuilding
	{
		private Building building;
		private int x, y, rot;
		
		public WorldBuilding(Building building, int x, int y, int rot)
		{
			this.building = building;
			this.x = x;
			this.y = y;
			this.rot = rot;
		}
		
		public int getWidth()
		{
			return building.getWidth();
		}
		
		public int getHeight()
		{
			return building.getHeight();
		}
		
		public int getX()
		{
			return x;
		}
		
		public int getY()
		{
			return y;
		}
		
		public int getRot()
		{
			return rot;
		}
		
		public void draw(BufferedImage img, int x, int y, int width, int height)
		{
			building.draw(img, x, y, width, height, rot);
		}
		
		public boolean isVisible(double xx, double yy, double width, double height)
		{
			Point a1 = new Point(x,y);
			Point b1 = new Point(x+building.getWidth(),y+building.getHeight());
			
			Point a, b;
			switch(rot)
			{
			case 0:
			case 2:
				a = a1;
				b = b1;
				break;
			default:
				a = a1;
				b = new Point(a.x+building.getHeight(),a.y+building.getWidth());
				break;
			}
			
			if(a.x>=xx && a.y>=yy && a.x<=xx+width && a.y<=yy+height)
				return true;
			if(b.x>=xx && b.y>=yy && b.x<=xx+width && b.y<=yy+height)
				return true;
			return false;
		}
	}
	
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
		generator.generate(world.blocks, world.data, r, world);
		return world;
	}
	
	private Block[][] blocks;
	private int[][] data;
	private ArrayList<Entity> entities;
	private ArrayList<WorldBuilding> buildings;
	
	private CarsWorld(int width, int height)
	{
		this.blocks = new Block[width][height];
		this.data = new int[width][height];
		this.entities = new ArrayList<Entity>();
		this.buildings = new ArrayList<WorldBuilding>();
	}
	
	public void addEntity(Entity entity)
	{
		entities.add(entity);
	}
	
	public void addBuilding(Building building, int x, int y, int rot)
	{
		CarsLogger.dev("Adding building at ["+x+", "+y+"] ("+rot+")");
		buildings.add(new WorldBuilding(building,x,y,rot));
	}
	
	public void draw(BufferedImage img, Camera camera)
	{
		int camblockview = 30;
		boolean grid = false;
		boolean coords = false;
		boolean showdata = false;
		Graphics g = img.getGraphics();
		double cbvx = camblockview/camera.getZoom();
		double cbvy = img.getHeight()*1.0/img.getWidth()*cbvx;
		double tlx = camera.getX()-cbvx/2;
		double tly = camera.getY()-cbvy/2;
		if(tlx<0)tlx = 0;
		if(tly<0)tly = 0;
		if(tlx+cbvx>blocks.length)tlx = blocks.length-cbvx;
		if(tly+cbvy>blocks[0].length)tly = blocks[0].length-cbvy;
		
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
					g.drawImage(b.getImage(data[xx][yy]), (int)((x-xoff)*ppx), (int)((y-yoff)*ppy), (int)(ppx)+1, (int)(ppy)+1, null);
					if(coords)
					{
						g.drawString(xx+"", (int)((x-xoff)*ppx+ppx/3), (int)((y-yoff)*ppy+ppy/3));
						g.drawString(yy+"", (int)((x-xoff)*ppx+ppx/3), (int)((y-yoff)*ppy+ppy/3)+10);
					}
					if(showdata)
					{
						g.drawString(data[xx][yy]+"", (int)((x-xoff)*ppx+ppx/3), (int)((y-yoff)*ppy+ppy/3)+15);
					}
				}
			}
		}
		
		//buildings
		for(WorldBuilding building : buildings)
		{
			if(building.isVisible(tlx, tly, cbvx, cbvy))
			{
				double xx = building.getX()-tlx;
				double yy = building.getY()-tly;
				int width = (int)(ppx*building.getWidth());
				int height = (int)(ppy*building.getHeight());
				switch(building.getRot())
				{
				case 1:
				case 3:
					int temp = width;
					width = height;
					height = temp;
				}
				building.draw(img, (int)(xx*ppx), (int)(yy*ppy), width+1, height+1);
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
	
	public int getWidth()
	{
		return blocks.length;
	}
	
	public int getHeight()
	{
		return blocks[0].length;
	}
}
