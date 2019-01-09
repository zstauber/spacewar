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

/* If an X, Y is passed in, that will be the center of the SpriteAnimation.
 * If a StaticSprite pointer is passed in, instead, the X, Y will be taken from the attached sprite.
 */
public class SpriteAnimation
{
	private float X;
	private float Y;
	private BufferedImage[] frames = null;
	private double duration; // seconds, what the user enters
	private double period; // milliseconds, what the computer uses derived from duration
	private boolean loop; // replay when finished
	private double startTime; // milliseconds
	private double currentTime; // milliseconds
	private double timeElapsed; // milliseconds
	private int currentFrame;
	private StaticSprite sprite;
	
	SpriteAnimation(BufferedImage[] frames, double duration, boolean loop, float X, float Y)
	{
		this.X = X;
		this.Y = Y;
		this.frames = frames;
		this.duration = duration;
		this.loop = loop;
		period = (1e3d / duration) / frames.length;
		startTime = System.nanoTime() / 1e6d;
		this.sprite = null;
	}

	SpriteAnimation(BufferedImage[] frames, double duration, boolean loop, StaticSprite sprite)
	{
		this.X = sprite.getX();
		this.Y = sprite.getY();
		this.frames = frames;
		this.duration = duration;
		this.loop = loop;
		period = (1e3d / duration) / frames.length;
		startTime = System.nanoTime() / 1e6d;
		this.sprite = sprite;
	}

	public float getX() { return this.X; }
	public float getY() { return this.Y; }
	void setX(float X) { this.X = X; }
	void setY(float Y) { this.Y = Y; }
	public int getCurrentFrame() { return this.currentFrame; }
	public StaticSprite getSprite() { return this.sprite; }
	void setSprite(StaticSprite sprite) { this.sprite = sprite; }

	// drawing always updates the frame, but we update it manually in the game
	// loop to see if the currentFrame goes to -1, at which point it can be
	// disposed of (i.e. by removing it from the SpriteAnimation ArrayList)
	public void updateFrame()
	{
		currentTime = System.nanoTime() / 1e6d;
		timeElapsed = currentTime - startTime;
		if (timeElapsed > (duration * 1e3d) && loop == false)
		{
			// if the animation is over and it's not on a loop, just exit
			currentFrame = -1;
		}
		else
		{
			currentFrame = (int)(timeElapsed / period) % frames.length;
		}
		return;
	}
	
	void draw(Graphics g, ImageObserver imOb)
	{
		// this.updateFrame();
		g.drawImage(frames[currentFrame], (int)(X - 10), (int)(Y - 10), 20, 20, imOb);
	}
}
