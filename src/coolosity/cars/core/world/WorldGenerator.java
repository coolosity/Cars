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
	private HashMap<Integer,Path> dirs;
	
	public Intersection(Point p, HashMap<Integer,Path> dirs)
	{
		this.p = new Point(p);
		this.dirs = dirs;
	}
	
	public Intersection(Point p)
	{
		this(p,new HashMap<Integer,Path>());
	}
	
	public Point getPoint()
	{
		return p;
	}
	
	public HashMap<Integer,Path> getDirs()
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
		this.a = new Point(a);
		this.b = new Point(b);
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
	
	public Intersection getOther(Intersection i)
	{
		return i==ia?ib:ia;
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
		Path pa = new Path(a,p,ia,ip);
		Path pb = new Path(p,b,ip,ib);
		spl.add(pa);
		spl.add(pb);
		if(a.x==b.x)
		{
			Path up = a.y<b.y?pa:pb;
			Path down = a.y>b.y?pa:pb;
			Intersection iup = a.y<b.y?ia:ib;
			Intersection idown = a.y>b.y?ia:ib;
			iup.getDirs().put(3, up);
			idown.getDirs().put(1, down);
			ip.getDirs().put(1, up);
			ip.getDirs().put(3, down);
		}
		else
		{
			Path left = a.x<b.x?pa:pb;
			Path right = a.x>b.x?pa:pb;
			Intersection ileft = a.x<b.x?ia:ib;
			Intersection iright = a.x>b.x?ia:ib;
			ileft.getDirs().put(0, left);
			iright.getDirs().put(2, right);
			ip.getDirs().put(0, right);
			ip.getDirs().put(2, left);
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
	
	public void generate(Block[][] blocks, int[][] data, Random r, CarsWorld world)
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
		
		Point start, end;
		switch(r.nextInt(2))
		{
		case 0:
			start = new Point(1,blocks[0].length/2);
			end = new Point(blocks.length-2,blocks[0].length/2);
			break;
		default:
			start = new Point(blocks.length/2,1);
			end = new Point(blocks.length/2,blocks[0].length-2);
			break;
		}
		buildRoads(start,end,roadMinDist,roadMaxDist);
		ArrayList<Intersection> cln = new ArrayList<Intersection>(intersections);
		int lastdir = r.nextInt(4);
		for(Intersection i : cln)
		{
			if(r.nextInt(2)==0)
			{
				int dir = lastdir+2;
				while(dir>=4)dir -= 4;
				while(i.getDirs().containsKey(dir))
				{
					dir++;
					if(dir>=4)dir=0;
				}
				lastdir = dir;
				Point str = i.getPoint();
				Point ed;
				switch(dir)
				{
				case 0:
					ed = new Point(blocks.length-2,str.y);
					break;
				case 1:
					ed = new Point(str.x,1);
					break;
				case 2:
					ed = new Point(1,str.y);
					break;
				default:
					ed = new Point(str.x,blocks[0].length-2);
					break;
				}
				buildRoads(str, ed, roadMinDist, roadMaxDist);
			}
		}
		
		cln.clear();
		cln.addAll(intersections);
		//make roads
		for(Intersection i : cln)
		{
			roads(0,i.getPoint(),-1,i);
		}
		
		//fix intersection studs
		ArrayList<Intersection> tofix = new ArrayList<Intersection>();
		for(Intersection i : intersections)
		{
			if(i.getDirs().size()==1)tofix.add(i);
		}
		CarsLogger.dev("Fixing intersection studs");
		while(tofix.size()>0)
		{
			doFixes(tofix);
			if(tofix.size()>0)
			{
				Intersection i = tofix.get(r.nextInt(tofix.size()));
				if(i.getDirs().size()==1)
				{
					for(int k : i.getDirs().keySet())
					{
						Path p = i.getDirs().get(k);
						CarsLogger.dev("Removing path from ["+p.getA().x+", "+p.getA().y+"] to ["+p.getB().x+", "+p.getB().y+"]");
						makePath(p.getA(),p.getB(),Blocks.grass);
						p.getOther(i).getDirs().remove(opp(k));
						paths.remove(p);
					}
					intersections.remove(i);
				}
				tofix.remove(i);
			}
			tofix.clear();
			for(Intersection i : intersections)
			{
				if(i.getDirs().size()==1)tofix.add(i);
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
				data[p.x][p.y] = 3;
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
				data[p.x][p.y] = 1;
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
				data[p.x][p.y] = 3;
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
				data[p.x][p.y] = 1;
			}
			else
			{
				data[p.x-1][p.y+1] = d.contains(2)?2:10;
				data[p.x][p.y+1] = 2;
				data[p.x+1][p.y+1] = d.contains(0)?2:11;
			}
			if(d.size()==4)
			{
				data[p.x][p.y] = 16;
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
		
		//buildings
		Building[] onlyOne = {
				
		};
	}
	
	private void buildRoads(Point a, Point b, int minspace, int maxspace)
	{
		CarsLogger.dev("Building roads from ["+a.x+", "+a.y+"] to ["+b.x+", "+b.y+"]");
		int lx = a.x<b.x?a.x:b.x;
		int hx = a.x>b.x?a.x:b.x;
		int ly = a.y<b.y?a.y:b.y;
		int hy = a.y>b.y?a.y:b.y;
		Intersection old = getIntersection(new Point(lx,ly));
		if(old==null)
		{
			old = new Intersection(new Point(lx,ly));
			intersections.add(old);
		}
		Point cur = new Point(old.getPoint());
		if(ly==hy)
		{
			for(int x=lx+minspace;x<=hx;)
			{
				cur.x = x;
				Intersection n = getIntersection(cur);
				CarsLogger.dev("Adding intersection at ["+cur.x+", "+cur.y+"]");
				if(n==null)
				{
					n = new Intersection(cur);
					intersections.add(n);
				}
				Path p = new Path(old.getPoint(),n.getPoint(),old,n);
				paths.add(p);
				old.getDirs().put(0, p);
				n.getDirs().put(2, p);
				old = n;
				makePath(p.getA(),p.getB(),Blocks.road);
				if(x<hx)
				{
					x += minspace+r.nextInt(maxspace-minspace+1);
					if(x>hx)x = hx;
				}
				else
				{
					x++;
				}
			}
		}
		else
		{
			for(int y=ly+minspace;y<=hy;)
			{
				cur.y = y;
				CarsLogger.dev("Adding intersection at ["+cur.x+", "+cur.y+"]");
				Intersection n = getIntersection(cur);
				if(n==null)
				{
					n = new Intersection(cur);
					intersections.add(n);
				}
				Path p = new Path(old.getPoint(),n.getPoint(),old,n);
				paths.add(p);
				old.getDirs().put(3, p);
				n.getDirs().put(1, p);
				old = n;
				makePath(p.getA(),p.getB(),Blocks.road);
				if(y<hy)
				{
					y += minspace+r.nextInt(maxspace-minspace+1);
					if(y>hy)y = hy;
				}
				else
				{
					y++;
				}
			}
		}
	}
	
	private Intersection getIntersection(Point p)
	{
		for(Intersection i : intersections)
			if(i.getPoint().equals(p))
				return i;
		return null;
	}
	
	private void doFixes(ArrayList<Intersection> tofix)
	{
		CarsLogger.dev("Fixing "+tofix.size()+" intersections");
		boolean changed = true;
		while(tofix.size()>0 && changed)
		{
			CarsLogger.dev("start:");
			changed = false;
			for(int i=0;i<tofix.size()&&!changed;i++)
			{
				changed = fixIntersectionStuds(tofix.get(i));
				if(changed)
					tofix.remove(i);
			}
		}
	}
	
	private boolean fixIntersectionStuds(Intersection i)
	{
		CarsLogger.dev("Fixing ["+i.getPoint().x+", "+i.getPoint().y+"]");
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
				CarsLogger.dev("Adding intersection ["+p.x+", "+p.y+"] between ["+path.getA().x+", "+path.getA().y+"] and ["+path.getB().x+", "+path.getB().y+"]");
				intersections.add(k);
				ArrayList<Path> nPaths = path.split(p, k);
				paths.addAll(nPaths);
				paths.remove(path);
			}
			Path pn = new Path(new Point(i.getPoint()),new Point(p),i,k);
			paths.add(pn);
			i.getDirs().put(newDir, pn);
			k.getDirs().put(opp(newDir), pn);
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
				Intersection n = new Intersection(new Point(to));
				Path pn = new Path(new Point(cur),new Point(to),intersection,n);
				paths.add(pn);
				intersection.getDirs().put(dir,pn);
				n.getDirs().put(opp(dir), pn);
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
		CarsLogger.dev("Path from ["+a.x+", "+a.y+"] to ["+b.x+", "+b.y+"] ("+toset.getID()+")");
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
