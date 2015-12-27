package coolosity.cars.main;

import coolosity.cars.core.CarsGame;
import coolosity.cars.core.display.CarsDisplay;
import coolosity.cars.core.display.Resources;
import coolosity.cars.core.world.CarsWorld;
import coolosity.cars.core.world.WorldGenerator;

public class CarsMain
{

	public static void main(String[] args)
	{
		Resources.loadResources();
		new CarsMain();
	}
	
	private CarsGame game;
	private CarsDisplay display;
	
	public CarsMain()
	{
		CarsWorld world = CarsWorld.generate(new WorldGenerator(200,200,1451083229212L));
		game = new CarsGame(world,false);
		display = new CarsDisplay("Cars",800,600,this,game,game);
		try {
			Thread.sleep(0);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		go();
	}
	
	private void go()
	{
		long lasttick = System.currentTimeMillis();
		while(true)
		{
			long cur = System.currentTimeMillis();
			game.tick(cur-lasttick);
			lasttick = cur;
		}
	}
	
	public CarsGame getGame()
	{
		return game;
	}
	
	public CarsDisplay getDisplay()
	{
		return display;
	}
}
