package coolosity.cars.core;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;

import coolosity.cars.core.display.Camera;
import coolosity.cars.core.entities.Car;
import coolosity.cars.core.entities.Location;
import coolosity.cars.core.world.CarsWorld;

public class CarsGame implements KeyListener
{
	private static final int W = 87;
	private static final int A = 65;
	private static final int S = 83;
	private static final int D = 68;
	
	private HashMap<Integer,Boolean> keysPressed;
	private CarsWorld world;
	private Camera camera;
	private Car player;
	
	public CarsGame(CarsWorld world, boolean cameraRotate)
	{
		keysPressed = new HashMap<Integer,Boolean>();
		{
			keysPressed.put(W, false);
			keysPressed.put(A, false);
			keysPressed.put(S, false);
			keysPressed.put(D, false);
		}
		this.world = world;
		this.camera = new Camera(world.getWidth()/2,world.getHeight()/2,1.0,0,cameraRotate);
		this.player = new Car(new Location(world.getWidth()/2,world.getHeight()/2), 8.0, 140, 7, 400, 5.0, 400);
		player.getLocation().setX(player.getLocation().getX()-player.getWidth()/2);
		player.getLocation().setY(player.getLocation().getY()-player.getHeight()/2);
		world.addEntity(player);
	}
	
	public void tick(long millis)
	{
		double per = millis/1000.0;
		double rotSpeed = player.getRotSpeed();
		double speed = player.getSpeed();
		boolean isRot = false;
		boolean isSpeed = false;
		if(keysPressed.get(A))
		{
			isRot = true;
			if(rotSpeed<=0)
				rotSpeed -= per*player.getRotAccel();
			else
				rotSpeed -= per*player.getRotDeccel();
			if(rotSpeed<-player.getMaxRotSpeed())
				rotSpeed = -player.getMaxRotSpeed();
		}
		if(keysPressed.get(D))
		{
			isRot = true;
			if(rotSpeed>=0)
				rotSpeed += per*player.getRotAccel();
			else
				rotSpeed += per*player.getRotDeccel();
			if(rotSpeed>player.getMaxRotSpeed())
				rotSpeed = player.getMaxRotSpeed();
		}
		if(keysPressed.get(W))
		{
			isSpeed = true;
			if(speed<0)
				speed += per*player.getDeccel();
			else
				speed += per*player.getAccel();
			if(speed>player.getMaxSpeed())
				speed = player.getMaxSpeed();
		}
		if(keysPressed.get(S))
		{
			isSpeed = true;
			if(speed>0)
				speed -= per*player.getDeccel();
			else
				speed -= per*player.getAccel();
			if(speed<-player.getMaxSpeed())
				speed = -player.getMaxSpeed();
		}
		if(!isRot)
		{
			if(rotSpeed>0)
			{
				double dec = per*player.getRotDeccel();
				if(dec>rotSpeed)dec = rotSpeed;
				rotSpeed -= dec;
			}
			else if(rotSpeed<0)
			{
				double inc = per*player.getRotDeccel();
				if(inc>-rotSpeed)inc = -rotSpeed;
				rotSpeed += inc;
			}
		}
		if(!isSpeed)
		{
			if(speed>0)
			{
				double dec = per*player.getDeccel();
				if(dec>speed)dec = speed;
				speed -= dec;
			}
			else if(rotSpeed<0)
			{
				double inc = per*player.getDeccel();
				if(inc>-speed)inc = -speed;
				speed += inc;
			}
		}
		
		player.setRotSpeed(rotSpeed);
		player.setSpeed(speed);

		double rot = player.getRotation()+per*rotSpeed;
		double dist = per*speed;
		double x = player.getLocation().getX()-Math.cos(Math.toRadians(rot))*dist;
		double y = player.getLocation().getY()-Math.sin(Math.toRadians(rot))*dist;
		
		player.setRotation(rot);
		player.getLocation().setX(x);
		player.getLocation().setY(y);
		camera.setX(player.getLocation().getX()+player.getWidth()/2);
		camera.setY(player.getLocation().getY()+player.getHeight()/2);
		if(camera.getRotate())
			camera.setRot(90-player.getRotation());
	}
	
	public CarsWorld getWorld()
	{
		return world;
	}
	
	public Camera getCamera()
	{
		return camera;
	}

	@Override
	public void keyPressed(KeyEvent e)
	{
		keysPressed.put(e.getKeyCode(), true);
	}

	@Override
	public void keyReleased(KeyEvent e)
	{
		keysPressed.put(e.getKeyCode(), false);
	}

	@Override
	public void keyTyped(KeyEvent e) {}
}
