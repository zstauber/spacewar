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
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

public class ImagesOperator
{
	static GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	static GraphicsConfiguration gc = ge.getDefaultScreenDevice().getDefaultConfiguration();

	public static BufferedImage getRotatedImage(BufferedImage src, int angle)
	{
		if (src == null)
		{
			System.out.println("getRotatedImage: input image is null");
			return null;
		}
	
		int transparency = src.getColorModel().getTransparency();
		BufferedImage dest =  gc.createCompatibleImage(src.getWidth(), src.getHeight(), transparency);
		Graphics2D g2d = dest.createGraphics();
		
		AffineTransform origAT = g2d.getTransform();  // save original
		
		// rotate the coord. system of the dest. image around its center
		AffineTransform rot = new AffineTransform();
		rot.rotate(Math.toRadians(angle), src.getWidth()/2, src.getHeight()/2);
		g2d.transform(rot);
		
		g2d.drawImage(src, 0, 0, null);   // copy in the image
		
		g2d.setTransform(origAT);    // restore original transform
		g2d.dispose();
		
		return dest;
	}
}
