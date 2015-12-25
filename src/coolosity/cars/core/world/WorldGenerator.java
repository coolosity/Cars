package coolosity.cars.core.world;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;

import coolosity.cars.core.CarsLogger;
import coolosity.cars.core.world.block.Block;

class Intersection
{
	
	private Point p;
	private HashMap<Integer,Point> dirs;
	
	public Intersection(Point p, HashMap<Integer,Point> dirs)
	{
		this.p = p;
		this.dirs = dirs;
	}
	
	public Intersection(Point p)
	{
		this(p,new HashMap<Integer,Point>());
	}
	
	public Point getPoint()
	{
		return p;
	}
	
	public HashMap<Integer,Point> getDirs()
	{
		return dirs;
	}
}

class Path
{
	private Point a, b;
	private Intersection ia, ib;
	
	public Path(Point a, Point b, Intersection ia, Intersection ib)
	{
		this.a = a;
		this.b = b;
		this.ia = ia;
		this.ib = ib;
	}
	
	public Point getA()
	{
		return a;
	}
	
	public Point getB()
	{
		return b;
	}
	
	public Intersection getIA()
	{
		return ia;
	}
	
	public Intersection getIB()
	{
		return ib;
	}
	
	public boolean contains(Point p)
	{
		if(a.x==b.x && a.x==p.x)
		{
			int min = Math.min(a.y, b.y);
			int max = Math.max(a.y,b.y);
			if(p.y>=min && p.y<=max)
				return true;
		}
		else if(a.y == p.y)
		{
			int min = Math.min(a.x, b.x);
			int max = Math.max(a.x,b.x);
			if(p.x>=min && p.x<=max)
				return true;
		}
		return false;
	}
	
	public ArrayList<Path> split(Point p, Intersection ip)
	{
		ArrayList<Path> spl = new ArrayList<Path>();
		spl.add(new Path(a,p,ia,ip));
		spl.add(new Path(p,b,ip,ib));
		if(a.x==b.x)
		{
			ip.getDirs().put(1, a.y<b.y?a:b);
			ip.getDirs().put(3, a.y>b.y?a:b);
		}
		else
		{
			ip.getDirs().put(0, a.x>b.x?a:b);
			ip.getDirs().put(2, a.x<b.x?a:b);
		}
		return spl;
	}
}

public class WorldGenerator
{
	private static final int roadDist = 10;
	private static final int roadMinDist = 15;
	private static final int roadMaxDist = 30;
	private static final int roadGridSize = 3;
	
	private int width, height;
	private long seed;
	
	private Block[][] blocks;
	private int[][] data;
	private Random r;
	
	private ArrayList<Intersection> intersections;
	private ArrayList<Path> paths;
	
	public WorldGenerator(int width, int height)
	{
		this(width, height, System.currentTimeMillis());
	}
	
	public WorldGenerator(int width, int height, long seed)
	{
		this.width = width;
		this.height = height;
		this.seed = seed;
		this.intersections = new ArrayList<Intersection>();
		this.paths = new ArrayList<Path>();
		CarsLogger.info("Initialized world with size ["+width+", "+height+"] on seed "+seed);
	}
	
	public void generate(Block[][] blocks, int[][] data, Random r)
	{
		this.blocks = blocks;
		this.data = data;
		this.r = r;
		
		//Fill grass
		for(int x=0;x<blocks.length;x++)
		{
			for(int y=0;y<blocks[0].length;y++)
			{
				blocks[x][y] = Blocks.grass;
			}
		}
		
		int startSide = r.nextInt(4);
		Point cur;
		switch(startSide)
		{
		case 0:
			cur = new Point(1,r.nextInt(blocks[0].length));
			break;
		case 1:
			cur = new Point(r.nextInt(blocks.length),blocks[0].length-2);
			break;
		case 2:
			cur = new Point(blocks.length-2,r.nextInt(blocks[0].length));
			break;
		default:
			cur = new Point(r.nextInt(blocks.length),1);
			break;
		}
		CarsLogger.dev("Starting road point: "+cur.toString());
		
		//make roads
		roads(0,cur,-1,new Intersection(new Point(cur)));
		
		//fix intersection studs
		ArrayList<Intersection> tofix = new ArrayList<Intersection>();
		for(Intersection i : intersections)
		{
			if(i.getDirs().size()==1)tofix.add(i);
		}
		CarsLogger.dev("Fixing intersection studs");
		boolean changed = true;
		while(tofix.size()>0 && changed)
		{
			changed = false;
			for(int i=0;i<tofix.size()&&!changed;i++)
			{
				changed = fixIntersectionStuds(tofix.get(i));
				if(changed)
					tofix.remove(i);
			}
		}
		
		
		
		//fix intersections data vals
		for(Intersection i : intersections)
		{
			Point p = i.getPoint();
			for(int x=-1;x<=1;x++)
			{
				for(int y=-1;y<=1;y++)
				{
					Point z = new Point(p.x+x,p.y+y);
					blocks[z.x][z.y] = Blocks.road;
					data[z.x][z.y] = 0;
				}
			}
			Set<Integer> d = i.getDirs().keySet();
			if(d.contains(0))
			{
				data[p.x+1][p.y-1] = 2;
				data[p.x+1][p.y] = 3;
				data[p.x+1][p.y+1] = 2;
			}
			else
			{
				data[p.x+1][p.y-1] = 0;
				data[p.x+1][p.y] = 0;
				data[p.x+1][p.y+1] = 0;
			}
			if(d.contains(1))
			{
				data[p.x-1][p.y-1] = 0;
				data[p.x][p.y-1] = 1;
				data[p.x+1][p.y-1] = d.contains(0)?10:0;
			}
			else
			{
				data[p.x-1][p.y-1] = 2;
				data[p.x][p.y-1] = 2;
				data[p.x+1][p.y-1] = d.contains(0)?2:9;
			}
			if(d.contains(2))
			{
				data[p.x-1][p.y-1] = d.contains(1)?11:2;
				data[p.x-1][p.y] = 3;
				data[p.x-1][p.y+1] = 2;
			}
			else
			{
				data[p.x-1][p.y-1] = d.contains(1)?0:8;
				data[p.x-1][p.y] = 0;
				data[p.x-1][p.y+1] = 0;
			}
			if(d.contains(3))
			{
				data[p.x-1][p.y+1] = d.contains(2)?9:0;
				data[p.x][p.y+1] = 1;
				data[p.x+1][p.y+1] = d.contains(0)?8:0;
			}
			else
			{
				data[p.x-1][p.y+1] = d.contains(2)?2:10;
				data[p.x][p.y+1] = 2;
				data[p.x+1][p.y+1] = d.contains(0)?2:11;
			}
			if(d.size()==4)
			{
				
			}
			else if(d.contains(1) && d.contains(2) && d.contains(3))
			{
				data[p.x][p.y] = 12;
			}
			else if(d.contains(0) && d.contains(1) && d.contains(3))
			{
				data[p.x][p.y] = 13;
			}
			else if(d.contains(0) && d.contains(1) && d.contains(2))
			{
				data[p.x][p.y] = 14;
			}
			else if(d.contains(0) && d.contains(2) && d.contains(3))
			{
				data[p.x][p.y] = 15;
			}
			else if(d.contains(0) && d.contains(3))
			{
				data[p.x][p.y] = 7;
			}
			else if(d.contains(2) && d.contains(3))
			{
				data[p.x][p.y] = 5;
			}
			else if(d.contains(1) && d.contains(0))
			{
				data[p.x][p.y] = 6;
			}
			else if(d.contains(1) && d.contains(2))
			{
				data[p.x][p.y] = 4;
			}
		}
	}
	
	private boolean fixIntersectionStuds(Intersection i)
	{
		int dir = 0;
		for(int k : i.getDirs().keySet())dir = k;
		
		int newDir = r.nextInt(4);
		Point p = null;
		for(int j=0;j<2&&p==null;j++)
		{
			newDir++;
			if(newDir>=4)newDir=0;
			while(newDir==dir || newDir==opp(dir))
			{
				newDir++;
				if(newDir>=4)newDir=0;
			}
			int dist = getDistToNonGrass(i.getPoint(), newDir)+roadMinDist+1;
			p = add(i.getPoint(),newDir,dist);
			if(isValid(p))
			{
				if(blocks[p.x][p.y] != Blocks.road)
				{
					p = null;
				}
			}
			else
			{
				p = null;
			}
		}
		if(p != null && isValid(p))
		{
			makePath(i.getPoint(),p,Blocks.road);
			Intersection k = null;
			for(Intersection is : intersections)
			{
				if(is.getPoint().equals(p))
					k = is;
			}
			if(k == null)
			{
				Path path = null;
				for(Path pp : paths)
				{
					if(pp.contains(p))
					{
						path = pp;
					}
				}
				k = new Intersection(p);
				intersections.add(k);
				ArrayList<Path> nPaths = path.split(p, k);
				paths.addAll(nPaths);
				paths.remove(path);
			}
			i.getDirs().put(newDir, p);
			k.getDirs().put(opp(newDir), i.getPoint());
			paths.add(new Path(new Point(i.getPoint()),new Point(p),i,k));
			return true;
		}
		else
		{
			return false;
		}
	}
	
	private void roads(int iter, Point cur, int lastDir, Intersection intersection)
	{
		if(!intersections.contains(intersection))intersections.add(intersection);
		if(iter>Math.max(blocks.length, blocks[0].length))return;
		int dir = lastDir;
		while(dir==lastDir || dir==opp(lastDir))dir = r.nextInt(4);
		int max = getDistToNonGrass(cur,dir);
		if(max>=roadMinDist)
		{
			if(max>roadMaxDist)max = roadMaxDist;
			int dist = roadMinDist+r.nextInt(max-roadMinDist+1);
			while(dist%roadGridSize!=0)dist--;
			Point to = add(cur,dir,dist);
			if(isValid(to))
			{
				makePath(cur,to,Blocks.road);
				intersection.getDirs().put(dir,to);
				Intersection n = new Intersection(new Point(to));
				n.getDirs().put(opp(dir), cur);
				paths.add(new Path(new Point(cur),new Point(to),intersection,n));
				roads(iter+1,to,dir,n);
			}
		}
		roads(iter+1,cur,lastDir,intersection);
	}
	
	private int getDistToNonGrass(Point cur, int dir)
	{
		int max = 0;
		if(dir==0)
		{
			max = blocks.length-2-cur.x;
			for(int x=cur.x+2;x<blocks.length-1;x++)
			{
				for(int y=-roadDist;y<=roadDist;y++)
				{
					Point p = new Point(x,cur.y+y);
					if(isValid(p))
					{
						Block b = blocks[p.x][p.y];
						if(b != Blocks.grass)
						{
							max = x-cur.x-roadMinDist;
							x = blocks.length;
						}
					}
				}
			}
		}
		else if(dir==2)
		{
			max = cur.x-1;
			for(int x=cur.x-2;x>=1;x--)
			{
				for(int y=-roadDist;y<=roadDist;y++)
				{
					Point p = new Point(x,cur.y+y);
					if(isValid(p))
					{
						Block b = blocks[p.x][p.y];
						if(b != Blocks.grass)
						{
							max = cur.x-x-roadMinDist;
							x = -1;
						}
					}
				}
			}
		}
		else if(dir==1)
		{
			max = cur.y-1;
			for(int y=cur.y-2;y>=1;y--)
			{
				for(int x=-roadDist;x<=roadDist;x++)
				{
					Point p = new Point(cur.x+x,y);
					if(isValid(p))
					{
						Block b = blocks[p.x][p.y];
						if(b != Blocks.grass)
						{
							max = cur.y-y-roadMinDist;
							y = -1;
						}
					}
				}
			}
		}
		else
		{
			max = blocks[0].length-2-cur.y;;
			for(int y=cur.y+2;y<blocks[0].length-1;y++)
			{
				for(int x=-roadDist;x<=roadDist;x++)
				{
					Point p = new Point(cur.x+x,y);
					if(isValid(p))
					{
						Block b = blocks[p.x][p.y];
						if(b != Blocks.grass)
						{
							max = y-cur.y-roadMinDist;
							y = blocks.length;
						}
					}
				}
			}
		}
		return max;
	}
	
	private int opp(int dir)
	{
		switch(dir)
		{
		case 0:
		case 1:
			return dir+2;
		default:
			return dir-2;
		}
	}
	
	private boolean isValid(Point p)
	{
		return isValid(p.x,p.y);
	}
	
	private boolean isValid(int x, int y)
	{
		return x>=0 && y>=0 && x<blocks.length && y<blocks[0].length;
	}
	
	private Point add(Point s, int dir, int len)
	{
		switch(dir)
		{
		case 0:
			return new Point(s.x+len,s.y);
		case 1:
			return new Point(s.x,s.y-len);
		case 2:
			return new Point(s.x-len,s.y);
		case 3:
			return new Point(s.x,s.y+len);
		}
		return s;
	}
	
	private void makePath(Point a, Point b, Block toset)
	{
		CarsLogger.dev("Road from ["+a.x+", "+a.y+"] to ["+b.x+", "+b.y+"]");
		int lx = a.x<b.x?a.x:b.x;
		int hx = a.x>b.x?a.x:b.x;
		int ly = a.y<b.y?a.y:b.y;
		int hy = a.y>b.y?a.y:b.y;
		if(a.x != b.x)
		{
			for(int x=lx;x<=hx;x++)
			{
				setBlock(x,ly-1,toset,2);
				setBlock(x,ly,toset,3);
				setBlock(x,ly+1,toset,2);
			}
		}
		else
		{
			for(int y=ly;y<=hy;y++)
			{
				setBlock(lx-1,y,toset,0);
				setBlock(lx,y,toset,1);
				setBlock(lx+1,y,toset,0);
			}
		}
	}
	
	private void setBlock(int x, int y, Block b, int data)
	{
		if(!isValid(x,y))return;
		blocks[x][y] = b;
		this.data[x][y] = data;
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
