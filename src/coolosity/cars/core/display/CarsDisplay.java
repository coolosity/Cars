package coolosity.cars.core.display;

import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import coolosity.cars.main.CarsMain;

public class CarsDisplay implements Runnable
{

	private JFrame frame;
	private JLabel label;
	private CarsMain main;
	
	public CarsDisplay(String title, int width, int height, CarsMain main, KeyListener input)
	{
		this.main = main;
		frame = new JFrame(title);
		if(input != null)
			frame.addKeyListener(input);
		frame.setSize(width, height);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		label = new JLabel();
		frame.add(label);
		
		frame.setVisible(true);
		
		(new Thread(this)).start();
	}

	@Override
	public void run() {
		long lastdraw = System.currentTimeMillis();
		while(true)
		{
			BufferedImage img = new BufferedImage(label.getWidth(),label.getHeight(),BufferedImage.TYPE_INT_ARGB);
			main.getGame().getWorld().draw(img, main.getGame().getCamera());
			while(System.currentTimeMillis()-lastdraw<1000/200);
			lastdraw = System.currentTimeMillis();
			label.setIcon(new ImageIcon(img));
		}
	}
}
