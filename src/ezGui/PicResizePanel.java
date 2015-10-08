package ezGui;

import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.*;


public class PicResizePanel extends JPanel{

	/**
	 * @param args
	 * @throws IOException 
	 */
	
	BufferedImage image;
	JLabel label = new JLabel();
	static int counter = 1;
	public int width;
	public int height;
	
	// CONSTRUCTOR
	public PicResizePanel( Image pic)  {
		this.image = new BufferedImage(
		        pic.getWidth(null), 
		        pic.getHeight(null),
		        BufferedImage.TYPE_INT_RGB);
		
		Graphics2D g2 = image.createGraphics();
		g2.drawImage(pic, 0, 0, null);
		g2.dispose();
		
		this.setOpaque(true);
		this.setBackground(Color.BLACK);
		this.setBorder(BorderFactory.createBevelBorder(3));
		
	}//END KONSTRUKTORN
	
	 
	public void paintComponent(Graphics g){
		super.paintComponents(g);
		//g = this.getGraphics();
		
		int height = image.getHeight();
		int width = image.getWidth();
		int new_height;
		int new_width;
		
		int max_width;
		int max_height;
		
		if (this.getWidth() == 0 && this.getHeight() == 0){
			max_width = this.width;
			max_height = this.height;		
		}
		else {
			max_width = this.getWidth();
			max_height = this.getHeight();
		}
		
		if (height <= width){
			new_width = max_width;
			new_height = (int)(height*max_width/width);
		}
		else {
			new_height = max_height;
			new_width = (int)(width*max_height/height);
		}
		
		BufferedImage res_img = new BufferedImage ( new_width , new_height , BufferedImage.TYPE_INT_ARGB);
		Graphics g2 = res_img.getGraphics();
		g2.drawImage( image , 0	, 0 , new_width , new_height ,  this );
		boolean picDrawn = g.drawImage( res_img , 0	, 0 , this );
		
		if(picDrawn != true){
			System.out.println("Couldnt draw pic in paintComp!");
		}
	}
	
	public void callResize (Dimension d){
		this.width = (int) d.getWidth();
		this.height = (int) d.getHeight();
		this.repaint();
	}
}
