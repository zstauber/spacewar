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

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

/* This is the next sprite class up from StaticSprite.  It moves, but does not fire, and is not controllable.  It can die.  It is used for projectiles. */
public class DynamicSprite extends StaticSprite
{
	protected boolean alive;
	protected float hVelocity;
	protected float vVelocity;
	private int rotation;
	private BufferedImage rotBufImg;

	// accessors
	public boolean getAlive() { return this.alive; }
	public void setAlive(boolean alive) { this.alive = alive; }
	public float getHVelocity() { return this.hVelocity; }
	public float getVVelocity() { return this.vVelocity; }
	public void setHVelocity(float hVelocity) { this.hVelocity = hVelocity; }
	public void setVVelocity(float vVelocity) { this.vVelocity = vVelocity; }
	public int getRotation() { return this.rotation; }
	public void setRotation(int rotation) { this.rotation = rotation; }
	public BufferedImage getRotBufImg() { return this.rotBufImg; };
	public void setRotBufImg(BufferedImage imgRotatedShip) { this.rotBufImg = imgRotatedShip; };

	// constructors
	DynamicSprite() { this.alive = true; }
	DynamicSprite(BufferedImage bufImg)
	{
		this.alive = true; 
		this.bufImg = bufImg;
		this.rotate(0);
	}

	// overridden draw method, draws the rotated image, not the regular one
	void draw(Graphics g, ImageObserver imOb)
	{
		if (this.visible == true)
			g.drawImage(this.rotBufImg, Math.round(this.X) - this.width/2, Math.round(this.Y) - this.height/2, this.width, this.height, imOb);
	}

	void rotate(int rotation)
	{
		this.rotation = this.rotation + rotation;
		this.rotBufImg = ImagesOperator.getRotatedImage(this.bufImg, (int)(this.rotation * 22.5));
		//this.setRotBufImg(ImagesOperator.getRotatedImage(this.getBufImg(), (int)(this.getRotation() * 22.5)));
	}
	
	void damage()
	{
		this.alive = false;
	}
}
