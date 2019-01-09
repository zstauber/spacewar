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
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

/* The basic sprite has only an image, location, size, and collision polygon */
public class StaticSprite
{
	protected float X;
	protected float Y;
	protected int width;
	protected int height;
	protected BufferedImage bufImg;
	protected boolean visible;
	private Shape collisionPolygon;
	private Level existsOnLevel;
	private Level collidesWithLevels[];
	public static enum Level
	{
		PLANET, PLAYER1, PLAYER2, SLUG, TORPEDO;
	}

	// accessors
	public float getX() { return this.X; }
	public float getY() { return this.Y; }
	void setX(float X) { this.X = X; }
	void setY(float Y) { this.Y = Y; }
	public int getWidth() { return this.width; }
	public int getHeight() { return this.height; }
	void setWidth(int width) { this.width = width; }
	void setHeight(int height) { this.height = height; }
	public BufferedImage getBufImg() { return this.bufImg; };
	public void setBufImg(BufferedImage bufImg) { this.bufImg = bufImg; };
	public boolean getVisible() { return this.visible; }
	void setVisible(boolean visible) { this.visible = visible; }
	public Shape getCollisionPolygon() { return this.collisionPolygon; }
	void setCollisionPolygon(Shape collisionPolygon) { this.collisionPolygon = collisionPolygon; }
	public Level getExistsOnLevel() { return this.existsOnLevel; }
	void setExistsOnLevel(Level existsOnLevel) { this.existsOnLevel = existsOnLevel; }
	public Level[] getCollidesWithLevels() { return this.collidesWithLevels; }
	void setCollidesWithLevels(Level[] collidesWithLevels) { this.collidesWithLevels = collidesWithLevels; }

	// constructors
	StaticSprite() { }
	StaticSprite(BufferedImage bufImg) { this.bufImg = bufImg; }

	// draw method
	void draw(Graphics g, ImageObserver imOb)
	{
		if (this.visible == true) g.drawImage(this.bufImg, Math.round(this.X) - this.width/2, Math.round(this.Y) - this.height/2, this.width, this.height, imOb);
	}
	
	// collision detection method (stub)
	boolean collision(StaticSprite otherSprite)
	{
		return false;
	}
}
