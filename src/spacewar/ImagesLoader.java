/****************************************************************************
 * Copyright (c) 2012-2015 Zachary L. Stauber
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 ****************************************************************************/

package spacewar;

import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

public class ImagesLoader
{
	String IMAGE_DIR = "Images";
	GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
	
	public BufferedImage loadImage(String fnm)
	{
		try
		{
			BufferedImage im = ImageIO.read(getClass().getResource(IMAGE_DIR + "/" + fnm));
			
			int transparency = im.getColorModel().getTransparency();
			BufferedImage copy = gc.createCompatibleImage(im.getWidth(), im.getHeight(), transparency);
			
			// create a graphics context
			Graphics2D g2d = copy.createGraphics();
			
			// copy image
			g2d.drawImage(im, 0, 0, null);
			g2d.dispose();
			return copy;
		}
		catch (IOException e)
		{
			System.out.println("Load Image error for " + IMAGE_DIR + "/" + fnm + ":\n" + e);
			return null;
		}
	} // end of LoadImage() using ImageIO

	public BufferedImage[] loadStripImageArray(String fnm, int number)
	{
		if (number <= 0)
		{
			System.out.println("number <= 0; returning null");
			return null;
		}
		
		BufferedImage stripIm;
		if ((stripIm = loadImage(fnm)) == null){
			System.out.println("Returning null");
			return null;
		}
		
		int imWidth = (int)stripIm.getWidth() / number;
		int height = stripIm.getHeight();
		int transparency = stripIm.getColorModel().getTransparency();
		
		BufferedImage[] strip = new BufferedImage[number];
		Graphics2D stripGC;
		
		// each BufferedImage from the strip file is stored in strip[]
		for (int i=0; i < number; i++)
		{
			strip[i] = gc.createCompatibleImage(imWidth, height, transparency);
			// create a graphics context
			stripGC = strip[i].createGraphics();
			
			// copy image
			stripGC.drawImage(stripIm,  0, 0, imWidth, height, i * imWidth, 0, (i * imWidth) + imWidth, height, null);
			stripGC.dispose();
		}
		return strip;
	} // end of loadStripImageArray()	
}
