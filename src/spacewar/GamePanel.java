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

import javax.swing.*;

import spacewar.StaticSprite.Level;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class GamePanel extends JApplet implements Runnable, KeyListener, MouseListener
{
	private static final long serialVersionUID = 1L;
	private static final int PWIDTH = 800;
	private static final int PHEIGHT = 600;
	private static final int MAX_FRAME_SKIPS = 5;
	private static final int MAX_FRAME_RATE = 60;
	private static final int SHIP_TOP_SPEED = 6;
	private static final int SHIP_TOP_ENERGY = 50;
	private static final int SLUG_SPEED = 6;
	private static final int TORPEDO_SPEED = 6;
	private static final int CLOAK_SECONDS = 4;
	private static final int TORPEDO_DAMAGE = 10;
	private static final int SLUG_DAMAGE = 5;
	private static final int PLANET_DAMAGE = 5;
	private static final int SHIP_DAMAGE = 2;
	private Thread animator; // for the animation
	private ArrayList<SpriteAnimation> spriteAnimations = new ArrayList<SpriteAnimation>();
	private double avgups, avgfps;
	
	public static enum GameState
	{
		PRE, RUNNING, PAUSED, OVER;
	}
	private GameState gameState = GameState.PRE;

	// global variables for off-screen rendering
	private Graphics2D dbg;
	private Image dbImage = null;

	int mouseX = 0, mouseY = 0;
	int player1Wins = 0, player2Wins = 0;
	
	final double G = 2.5; // 6.674e-11 // gravitational constant
	final double M = 2.5; // 5.9722e24 // Earth mass
	final double R = 0.0; // 6.3671e6 // Earth radius
	ImagesLoader il = new ImagesLoader();
	//BufferedImage imgShip1 = il.loadImage("Ship1.png");
	//BufferedImage imgShip2 = il.loadImage("Ship2.png");
	BufferedImage imgShip1 = il.loadImage("Ship1.png");
	BufferedImage imgShip2 = il.loadImage("Ship2.png");
	BufferedImage imgSlug = il.loadImage("Slug.png");
	BufferedImage imgTorpedo = il.loadImage("Torpedo.png");
	BufferedImage imgPlanet = il.loadImage("Planet.png");
	BufferedImage imgStarfield = il.loadImage("Starfield.png");
	BufferedImage[] imgsExplosion = il.loadStripImageArray("Explosion.png", 10);
	BufferedImage[] imgsShield = il.loadStripImageArray("Shield.png", 1);
	
	AudioClip ship_explosion = new AudioClip("ship_explosion.wav");
	AudioClip ship_warp = new AudioClip("ship_warp.wav");
	AudioClip slug_launch = new AudioClip("slug_launch.wav");
	AudioClip torpedo_explosion = new AudioClip("torpedo_explosion.wav");
	AudioClip torpedo_launch = new AudioClip("torpedo_launch.wav");
	
	StaticSprite planet = new StaticSprite(imgPlanet);
	IntelligentSprite player1 = new IntelligentSprite(imgShip1);
	IntelligentSprite player2 = new IntelligentSprite(imgShip2);
	
	ArrayList<StaticSprite> planets = new ArrayList<StaticSprite>();
	//List<DynamicSprite> slugs = Collections.synchronizedList(new ArrayList<DynamicSprite>());
	ArrayList<DynamicSprite> slugs = new ArrayList<DynamicSprite>();
	ArrayList<DynamicSprite> torpedoes = new ArrayList<DynamicSprite>();
	ArrayList<IntelligentSprite> ships = new ArrayList<IntelligentSprite>();

	public GamePanel()
	{
		//String userDir = System.getProperty("user.dir");
		//JOptionPane.showMessageDialog(null, userDir);
		setBackground(Color.BLACK);
		setPreferredSize(new Dimension(PWIDTH, PHEIGHT));
		setFocusable(true);
		requestFocus(); // JPanel now receives key events
		readyForTermination();

		initializeGame();
	} // end of GamePanel() constructor
	
	public void addNotify()
	{
		/* Wait for the JPanel to be added to the FRame/JApplet before starting. */
		super.addNotify(); // creates the peer
		startGame(); // start the thread
	} // and of addNotify()
	
	private void readyForTermination()
	{
		addKeyListener(this);
		addMouseListener(this);
	} // end of readyForTermination()
	
	public void keyPressed(KeyEvent ke)
	{
		switch (ke.getKeyCode())
		{
			case KeyEvent.VK_Q:
				if (gameState == GameState.RUNNING) shootSlug(player1);
				break;
			case KeyEvent.VK_W:
				if (gameState == GameState.RUNNING) cloak(player1);
				break;
			case KeyEvent.VK_E:
				if (gameState == GameState.RUNNING) shootTorpedo(player1);
				break;
			case KeyEvent.VK_A:
				if (gameState == GameState.RUNNING) player1.rotate(-1); // rotate left
				break;
			case KeyEvent.VK_S:
				if (gameState == GameState.RUNNING) player1.accelerate(1); // rotate right
				break;
			case KeyEvent.VK_D:
				if (gameState == GameState.RUNNING) player1.rotate(1); // rotate right
				break;
			case KeyEvent.VK_Z:
				if (gameState == GameState.RUNNING) player1.transferStoW(); // weapon energy
				break;
			case KeyEvent.VK_X:
				if (gameState == GameState.RUNNING) hyperspace(player1);
				break;
			case KeyEvent.VK_C:
				if (gameState == GameState.RUNNING) player1.transferWtoS(); // shield energy
				break;
			case KeyEvent.VK_NUMPAD7:
				if (gameState == GameState.RUNNING) shootSlug(player2);
				break;
			case KeyEvent.VK_NUMPAD8:
				if (gameState == GameState.RUNNING) cloak(player2);
				break;
			case KeyEvent.VK_NUMPAD9:
				if (gameState == GameState.RUNNING) shootTorpedo(player2);
				break;
			case KeyEvent.VK_NUMPAD4:
				if (gameState == GameState.RUNNING) player2.rotate(-1); // rotate left
				break;
			case KeyEvent.VK_NUMPAD5:
				if (gameState == GameState.RUNNING) player2.accelerate(1); // rotate right
				break;
			case KeyEvent.VK_NUMPAD6:
				if (gameState == GameState.RUNNING) player2.rotate(1); // rotate right
				break;
			case KeyEvent.VK_NUMPAD1:
				if (gameState == GameState.RUNNING) player2.transferStoW(); // weapon energy
				break;
			case KeyEvent.VK_NUMPAD2:
				if (gameState == GameState.RUNNING) hyperspace(player2);
				break;
			case KeyEvent.VK_NUMPAD3:
				if (gameState == GameState.RUNNING) player2.transferWtoS(); // shield energy
				break;
			case KeyEvent.VK_P:
				if (gameState == GameState.RUNNING)
					stopGame();
				else
					resumeGame();
				break;
			case KeyEvent.VK_ESCAPE:
				this.endGame();
		}
	}
	public void keyReleased(KeyEvent ke) {}
	public void keyTyped(KeyEvent ke) {}

	public void mouseClicked(MouseEvent e)
	{
		mouseX = e.getX();
		mouseY = e.getY();
		spriteAnimations.add(new SpriteAnimation(imgsExplosion, 1.0, false, mouseX, mouseY));
		torpedo_explosion.play();
	}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}

	public void run()
	{
		/* 
		 * Repeatedly update, render, sleep
		 * We intend to separate updates from renders to have an adaptive frame
		 * rate.  Since our timer is System.nanoTime(), we will divide all values
		 * by 1e6d (a million) to convert ns -> ms.
		 * If we are achieving rendering and update speeds of less than 10 ms
		 * (100 FPS) we sleep.  If we are achieving rendering and update speeds
		 * of greater than 33 ms (30 FPS), we start skipping frames, up to 5.
		 * In any case we want 100 UPS.
		 */
		int updates, extraUpdates;
		long totalUpdates = 0L, totalFrames = 0L;
		double beginTime, startTime, updateTime, totalTime, frameTime, excessTime, periodTime;
		beginTime = System.nanoTime() / 1e6d;
		periodTime = 1e3d / MAX_FRAME_RATE; // desired time for an update/render in ms

		while (gameState != GameState.OVER)
		{
			updates = 0;
			extraUpdates = 0;
			startTime = System.nanoTime() / 1e6d;

			gameUpdate(); // if running, game state is updated
			updates++;
			totalUpdates++;
			
			updateTime = System.nanoTime() / 1e6d - startTime;
			
			gameRender(); // render to a buffer
			repaint(); // paint with the buffer
			//try { Thread.sleep(50); } catch (InterruptedException ex) {};
			totalFrames++;
			
			totalTime = System.nanoTime() / 1e6d - startTime;
			frameTime = totalTime - updateTime;
			excessTime = periodTime - totalTime;
			
			if (excessTime > 0)
				/* We computed an ideal frame rate (period) and see if we're meeting that
				 * speed.  If we have time left over, we just sleep it off and cycle the
				 * animation.
				 */
				try
				{
					Thread.sleep(Math.round(excessTime)); // sleep a bit
				}
				catch (InterruptedException ex) {}
			else if (excessTime < 0)
			{
				/* If we went over the period with the frame rendering, we still need
				 * to update every period, but we can drop frames.  So we compute how
				 * many periods we can reliably do a frame once while still doing an
				 * update every period.
				 */
				extraUpdates = (int)Math.ceil(frameTime / (periodTime - updateTime));
				while (updates <= extraUpdates && updates <= MAX_FRAME_SKIPS)
				{
					/* Do these extra updates, unless we run up against the user defined
					 * MAX_FRAME_SKIPS.
					 */
					gameUpdate();
					updates++;
					totalUpdates++;
				}
				totalTime = System.nanoTime() / 1e6d - startTime;
				excessTime = extraUpdates * periodTime - totalTime;
				if (excessTime >= 0)
					/* Even though we did a number of updates without frames we still want
					 * to wait for the end of a full period before cycling the animation.
					 */
					try
					{
						Thread.sleep(Math.round(excessTime));
					}
					catch (InterruptedException ex) {}
			}
			/* Compute some totals for statistics. */
			avgups = totalUpdates / (System.nanoTime() / 1e6d - beginTime) * 1000d;
			avgfps = totalFrames / (System.nanoTime() / 1e6d - beginTime) * 1000d;
		}
		System.exit(0); // so enclosing JFrame/JApplet exits
	} // end of run()
	
	private void initializeGame()
	{
		gameState = GameState.PRE;

		planets.clear();
		ships.clear();
		slugs.clear();
		torpedoes.clear();

		// add starting sprites
		planet.setX(PWIDTH/2);
		planet.setY(PHEIGHT/2);
		planet.setWidth(100);
		planet.setHeight(100);
		planet.setBufImg(imgPlanet);
		planet.setVisible(true);
		planet.setExistsOnLevel(Level.PLANET);
		planet.setCollidesWithLevels(new Level[] {Level.PLAYER1,Level.PLAYER2,Level.SLUG,Level.TORPEDO});
		planets.add(planet);
		
		player1.setX(400);
		player1.setY(150);
		player1.setWidth(20);
		player1.setHeight(20);
		player1.setVisible(true);
		player1.setExistsOnLevel(Level.PLAYER1);
		player1.setCollidesWithLevels(new Level[] {Level.PLANET,Level.PLAYER2,Level.SLUG,Level.TORPEDO});
		player1.setHVelocity(3);
		player1.setVVelocity(0);
		player1.setRotation(0);
		player1.rotate(0); // necessary on game reset if ship is not rotated to 0
		player1.setTopSpeed(SHIP_TOP_SPEED);
		player1.setTopEnergy(SHIP_TOP_ENERGY);
		player1.setWeaponEnergy(SHIP_TOP_ENERGY);
		player1.setShieldEnergy(SHIP_TOP_ENERGY);
		player1.setAlive(true);
		ships.add(player1);

		player2.setX(400);
		player2.setY(450);
		player2.setWidth(20);
		player2.setHeight(20);
		player2.setVisible(true);
		player2.setExistsOnLevel(Level.PLAYER2);
		player2.setCollidesWithLevels(new Level[] {Level.PLANET,Level.PLAYER1,Level.SLUG,Level.TORPEDO});
		player2.setHVelocity(-3);
		player2.setVVelocity(0);
		player2.setRotation(0);
		player2.rotate(8);
		player2.setTopSpeed(SHIP_TOP_SPEED);
		player2.setTopEnergy(SHIP_TOP_ENERGY);
		player2.setWeaponEnergy(SHIP_TOP_ENERGY);
		player2.setShieldEnergy(SHIP_TOP_ENERGY);
		player2.setAlive(true);
		ships.add(player2);
	}
	
	private void startGame()
	{
		// initialize and start the thread
		{
			if (animator == null || gameState == GameState.RUNNING)
			{
				animator = new Thread(this);
				animator.start();
			}
		}
	}
	
	public void stopGame()
	{
		// called by the user to pause execution
		gameState = GameState.PAUSED;
	}

	public void resumeGame()
	{
		// called by the user to resume execution
		gameState = GameState.RUNNING;
	}
	
	public void endGame()
	{
		// called by the user to stop execution
		gameState = GameState.OVER;
	}

	private void gameUpdate()
	{
		long currentTime = (long) (System.nanoTime() / 1e9);
		
		if (gameState == GameState.RUNNING) // update game state
		{
			for (int i = slugs.size() - 1; i >= 0; i--)
			{
				// much thanks for the formulas: http://physics.stackexchange.com/questions/17285/split-gravitational-force-into-x-y-and-z-componenets
				// alter velocity based on gravity well			
				double x = 0.0, y = 0.0;
	
				DynamicSprite d = slugs.get(i);
				x = PWIDTH/2.0 - d.getX();
				y = PHEIGHT/2.0 - d.getY();
				
				// move ship based on velocity
				d.setX(d.getX() + d.getHVelocity());
				d.setY(d.getY() + d.getVVelocity());
				
				// wrap around screen if necessary
				if (d.getX() <= 0 )
					d.setX(PWIDTH);
				else if (d.getX() >= PWIDTH)
					d.setX(0);
				if (d.getY() < 0 )
					d.setY(PHEIGHT);
				else if (d.getY() >= PHEIGHT)
					d.setY(0);

				collide(d, planet);
				collide(ships.get(0), d);
				collide(ships.get(1), d);
				for (int j = torpedoes.size() - 1; j >= 0; j--)
				{
					DynamicSprite d2 = torpedoes.get(j);
					collide(d2, d);
				}
				if (d.getAlive() == false)
					slugs.remove(i);
			}
			for (int i = torpedoes.size() - 1; i >= 0; i--)
			{
				DynamicSprite d = torpedoes.get(i);

				// much thanks for the formulas: http://physics.stackexchange.com/questions/17285/split-gravitational-force-into-x-y-and-z-componenets
				// alter velocity based on gravity well			
				double x = 0.0, y = 0.0, rsq = 0.0, a = 0.0, ax = 0.0, ay = 0.0;
	
				x = PWIDTH/2.0 - d.getX();
				y = PHEIGHT/2.0 - d.getY();
				
				rsq = Math.pow(x,2.0) + Math.pow(y,2.0);
				
				// compute each component of acceleration
				ax = (G * M) * x / rsq;
				ay = (G * M) * y / rsq;
	
				// don't need to worry about top speed here since DynamicSprites can neither increase nor decrease speed
				float newHVelocity = d.getHVelocity() + (float)ax;
				float newVVelocity = d.getVVelocity() + (float)ay;
				float newVelocity = (float)Math.pow(Math.pow(newHVelocity, 2.0) + Math.pow(newVVelocity, 2.0), 0.5);
 
				d.setHVelocity(newHVelocity);
				d.setVVelocity(newVVelocity);
				
				// move torpedo based on velocity
				d.setX(d.getX() + d.getHVelocity());
				d.setY(d.getY() + d.getVVelocity());
				
				// wrap around screen if necessary
				if (d.getX() <= 0 )
					d.setX(PWIDTH);
				else if (d.getX() >= PWIDTH)
					d.setX(0);
				if (d.getY() < 0 )
					d.setY(PHEIGHT);
				else if (d.getY() >= PHEIGHT)
					d.setY(0);
				
				collide(d, planet);
				collide(ships.get(0), d);
				collide(ships.get(1), d);
				/* The next loop adds not only torpedoes that just collided, but the ones the
				 * slugs collided with in the above slugs loop.
				 */
				if (d.getAlive() == false)
					torpedoes.remove(d);
			}
			for (int j = ships.size() - 1; j >= 0; j--)
			{
				IntelligentSprite i = ships.get(j);
				// much thanks for the formulas: http://physics.stackexchange.com/questions/17285/split-gravitational-force-into-x-y-and-z-componenets
				// alter velocity based on gravity well			
				double x = 0.0, y = 0.0, rsq = 0.0, a = 0.0, ax = 0.0, ay = 0.0;
	
				x = PWIDTH/2.0 - i.getX();
				y = PHEIGHT/2.0 - i.getY();
				
				rsq = Math.pow(x,2.0) + Math.pow(y,2.0);
				
				// compute each component of acceleration
				ax = (G * M) * x / rsq;
				ay = (G * M) * y / rsq;
	
				// tricky here, unlike thruster acceleration, we still need the ships to be affected even if they are already at their speed limit
				// so we need to find the magnitude in each direction and scale them back if they're above the top speed
				float newHVelocity = i.getHVelocity() + (float)ax;
				float newVVelocity = i.getVVelocity() + (float)ay;
				float newVelocity = (float)Math.pow(Math.pow(newHVelocity, 2.0) + Math.pow(newVVelocity, 2.0), 0.5);
 
				if (newVelocity <= i.getTopSpeed())
				{
					i.setHVelocity(newHVelocity);
					i.setVVelocity(newVVelocity);
				}
				else
				{
					float scaleFactor = i.getTopSpeed() / newVelocity;
					i.setHVelocity(newHVelocity * scaleFactor);
					i.setVVelocity(newVVelocity * scaleFactor);
				}
				
				// move ship based on velocity
				i.setX(i.getX() + i.getHVelocity());
				i.setY(i.getY() + i.getVVelocity());
				
				// wrap around screen if necessary
				if (i.getX() <= 0 )
					i.setX(PWIDTH);
				else if (i.getX() >= PWIDTH)
					i.setX(0);
				if (i.getY() < 0 )
					i.setY(PHEIGHT);
				else if (i.getY() >= PHEIGHT)
					i.setY(0);

				// Move shields along with sprites as soon as possible
				for (int i2 = spriteAnimations.size() - 1; i2 >= 0; i2--)
				{
					SpriteAnimation s = spriteAnimations.get(i2);
					if (s.getSprite() == i)
					{
						s.setX(s.getSprite().getX());
						s.setY(s.getSprite().getY());
					}
				}
				
				// now we regenerate energy at the rate of 1 point per second
				if (currentTime > i.getLastRegenTime())
				{
					i.regen();
					i.setLastRegenTime(currentTime);
				}
				// if either player is cloaked, we check to see if time is up and set to visible
				if (i.getVisible() == false)
				{
					if (currentTime > i.getLastCloakTime() + CLOAK_SECONDS)
						i.setVisible(true);
				}
			}
			collide(ships.get(0), ships.get(1));
			collide(ships.get(0), planet);
			collide(ships.get(1), planet);
			/* Iterate over SpriteAnimations updating each one.  If attached
			 * to a StaticSprite, update it's X,Y.
			 */
			for (int j = ships.size() - 1; j >= 0; j--)
			{
				IntelligentSprite i = ships.get(j);
				if (i.getAlive() == false && spriteAnimations.size() == 0)
				{
					if (i == player1)
						player2Wins++;
					else if (i == player2)
						player1Wins++;
					
					initializeGame();
					break;
				}
			}
		}
	}
	
	private void gameRender()
	{
		// draw the current frame to an image buffer
		if (dbImage == null) // create the buffer
		{
			dbImage = createImage(PWIDTH, PHEIGHT);
			if (dbImage == null)
			{
				System.out.println("dbImage is null");
				return;
			}
			else
				dbg = (Graphics2D)dbImage.getGraphics();
		}
		// clear the background
		//dbg.setColor(Color.BLACK);
		//dbg.fillRect(0, 0, PWIDTH, PHEIGHT);
		dbg.drawImage(imgStarfield, 0, 0, null);
		
		// draw game elements
		for (int i = planets.size() - 1; i >= 0; i--)
		{
			StaticSprite s = planets.get(i);
			s.draw(dbg, this);
		}
		for (int i = slugs.size() - 1; i >= 0; i--)
		{
			DynamicSprite d = slugs.get(i);
			d.draw(dbg, this);
		}
		for (int i = torpedoes.size() - 1; i >= 0; i--)
		{
			DynamicSprite d = torpedoes.get(i);
			d.draw(dbg, this);
		}
		for (int j = ships.size() - 1; j >= 0; j--)
		{
			IntelligentSprite i = ships.get(j);
			i.draw(dbg, this);
		}
		
		if (gameState == GameState.PRE)
		{ // title screen and player wins
			Font oldFont = dbg.getFont();
			dbg.setFont(new Font("SansSerif", Font.BOLD, 48));
			dbg.setColor(Color.WHITE);
			dbg.drawString("SPACEWAR", 260, 100);
			dbg.setFont(oldFont);

			dbg.setColor(Color.RED);
			dbg.drawString("Wins: " + player1Wins, 150, 160);
			dbg.drawString("Q", 75, 200);
			dbg.drawString("Fire", 75, 220);
			dbg.drawString("Slug",  75,  240);
			dbg.drawString("A", 75, 270);
			dbg.drawString("Rotate", 75, 290);
			dbg.drawString("CCW", 75, 310);
			dbg.drawString("Z", 75, 340);
			dbg.drawString("Weapon", 75, 360);
			dbg.drawString("Energy", 75, 380);
			dbg.drawString("W", 150, 200);
			dbg.drawString("", 150, 220);
			dbg.drawString("Cloak",  150,  240);
			dbg.drawString("S", 150, 270);
			dbg.drawString("Engine", 150, 290);
			dbg.drawString("Thrust", 150, 310);
			dbg.drawString("X", 150, 340);
			dbg.drawString("Hyper", 150, 360);
			dbg.drawString("Space", 150, 380);
			dbg.drawString("E", 225, 200);
			dbg.drawString("Fire", 225, 220);
			dbg.drawString("Torpedo",  225,  240);
			dbg.drawString("D", 225, 270);
			dbg.drawString("Rotate", 225, 290);
			dbg.drawString("CW", 225, 310);
			dbg.drawString("C", 225, 340);
			dbg.drawString("Shield", 225, 360);
			dbg.drawString("Energy", 225, 380);
			dbg.drawLine(50, 175, 275, 175);
			dbg.drawLine(50, 250, 275, 250);
			dbg.drawLine(50, 325, 275, 325);
			dbg.drawLine(50, 400, 275, 400);
			dbg.drawLine(50, 175, 50, 400);
			dbg.drawLine(125, 175, 125, 400);
			dbg.drawLine(200, 175, 200, 400);
			dbg.drawLine(275, 175, 275, 400);

			dbg.setColor(Color.BLUE);
			dbg.drawString("Wins: " + player2Wins, 625, 160);
			dbg.drawString("7", 550, 200);
			dbg.drawString("Fire", 550, 220);
			dbg.drawString("Slug",  550,  240);
			dbg.drawString("4", 550, 270);
			dbg.drawString("Rotate", 550, 290);
			dbg.drawString("CCW", 550, 310);
			dbg.drawString("1", 550, 340);
			dbg.drawString("Weapon", 550, 360);
			dbg.drawString("Energy", 550, 380);
			dbg.drawString("8", 625, 200);
			dbg.drawString("", 625, 220);
			dbg.drawString("Cloak",  625,  240);
			dbg.drawString("5", 625, 270);
			dbg.drawString("Engine", 625, 290);
			dbg.drawString("Thrust", 625, 310);
			dbg.drawString("2", 625, 340);
			dbg.drawString("Hyper", 625, 360);
			dbg.drawString("Space", 625, 380);
			dbg.drawString("9", 700, 200);
			dbg.drawString("Fire", 700, 220);
			dbg.drawString("Torpedo",  700,  240);
			dbg.drawString("6", 700, 270);
			dbg.drawString("Rotate", 700, 290);
			dbg.drawString("CW", 700, 310);
			dbg.drawString("3", 700, 340);
			dbg.drawString("Shield", 700, 360);
			dbg.drawString("Energy", 700, 380);
			dbg.drawLine(525, 175, 750, 175);
			dbg.drawLine(525, 250, 750, 250);
			dbg.drawLine(525, 325, 750, 325);
			dbg.drawLine(525, 400, 750, 400);
			dbg.drawLine(525, 175, 525, 400);
			dbg.drawLine(600, 175, 600, 400);
			dbg.drawLine(675, 175, 675, 400);
			dbg.drawLine(750, 175, 750, 400);
			dbg.setColor(Color.WHITE);
			dbg.drawString("P: start / pause / resume", 87, 450);
			dbg.drawString("Esc: quit", 562, 450);
		}
		else if (gameState == GameState.RUNNING || gameState == GameState.PAUSED)
		{ // ships, planet, bullets, etc., all gameplay elements
			// iterate over sprites updating each one.  If currentFrame is -1, delete, otherwise, draw current frame
			for (int i = spriteAnimations.size() - 1; i >= 0; i--)
			{
				SpriteAnimation si = spriteAnimations.get(i);
				si.updateFrame();
				if (si.getCurrentFrame() == -1)
					spriteAnimations.remove(i);
				else
					si.draw(dbg, this);
			}
				
			/* dbg.setColor(Color.WHITE);
			dbg.drawString("Acceleration a: " + String.format("%3.2f",a) + "\n ax: " + String.format("%3.2f",ax) + "\n ay: " + String.format("%3.2f",ay), 20, 40);
			dbg.drawString("Coordinates rsq: " + String.format("%6.2f",rsq) + "\n x: " + String.format("%3.2f",x) + "\n y: " + String.format("%3.2f",y), 20, 80);
			dbg.drawString("Average UPS: " + String.format("%4.1f",avgups), 20, 120);
			dbg.drawString("Average FPS: " + String.format("%4.1f",avgfps), 20, 160);
			dbg.drawString("Mouse Click: " + String.format("X: %d Y: %d", mouseX, mouseY), 20, 200); */
			dbg.setColor(Color.RED);
			dbg.drawString("S", 20, 530);
			dbg.fillRect(35, 525, player1.getShieldEnergy() * 3, 2);
			dbg.drawString("W", 20, 550);
			dbg.fillRect(35, 545, player1.getWeaponEnergy() * 3, 2);
			dbg.setColor(Color.BLUE);
			dbg.drawString("S", 760, 530);
			dbg.fillRect(755 - player2.getShieldEnergy() * 3, 525, player2.getShieldEnergy() * 3, 2);
			dbg.drawString("W", 760, 550);
			dbg.fillRect(755 - player2.getWeaponEnergy() * 3, 545, player2.getWeaponEnergy() * 3, 2);
			
			if (gameState == GameState.OVER)
				gameOverMessage(dbg);
		}
	} // end of gameRender();
	
	/* possible collisions
	 * static sprite: planet
	 * dynamic sprite: slug or missile
	 * intelligent sprite: player 1 or player 2 ships
	 * intelligent sprite vs. intelligent sprite: both bounce, take moderate damage
	 * intelligent sprite vs. dynamic sprite: intelligent takes minor damage, dynamic is destroyed
	 * intelligent sprite vs. static sprite: intelligent bounces, takes major damage
	 * dynamic sprite vs. dynamic sprite: both are destroyed
	 * dynamic sprite vs. static sprite: dynamic is destroyed
	 * static sprite vs. static sprite: no collisions possible, neither are capable of movement
	 */
	public boolean doCollide(StaticSprite a, StaticSprite b)
	{
		/* We first check if the two sprites even can collide
		 * and if so, then we check if their circles intersect
		 */
		Level aLevel = a.getExistsOnLevel();
		Level aLevels[] = a.getCollidesWithLevels();
		Level bLevel = b.getExistsOnLevel();
		Level bLevels[] = b.getCollidesWithLevels();
		for (int i = 0, j = aLevels.length; i < j; i++ )
		{
			if (aLevels[i] == bLevel)
			{
				for (int k = 0, l = bLevels.length; k < l; k++ )
				{
					if (bLevels[k] == aLevel)
					{
						if (Math.pow((b.getX() - a.getX()) * (b.getX() - a.getX()) + (b.getY() - a.getY()) * (b.getY() - a.getY()), 0.5) <= (a.getWidth()/2 + b.getWidth()/2))
							return true;
						else
							return false;
					}
				}
			}
		}
		return false;
	}
	
	public boolean collide(IntelligentSprite a, IntelligentSprite b)
	{ // ship with ship
		//if (b instanceof IntelligentSprite) {} // stop checking using instanceof and do it using overloading
		
		// thanks to http://archive.ncsa.illinois.edu/Classes/MATH198/townsend/math.html
		// check for collision
		if (doCollide(a, b))
		{
			// find trajectories of each ball
			double a_dir_before = Math.atan2(a.getVVelocity(),a.getHVelocity());
			double b_dir_before = Math.atan2(((IntelligentSprite)b).getVVelocity(),((IntelligentSprite)b).getHVelocity());
			
			// find combined vector velocity of each ball
			double a_vel_before = Math.pow(a.getHVelocity() * a.getHVelocity() + a.getVVelocity() * a.getVVelocity(), 0.5);
			double b_vel_before = Math.pow(((IntelligentSprite)b).getHVelocity() * ((IntelligentSprite)b).getHVelocity() + ((IntelligentSprite)b).getVVelocity() * ((IntelligentSprite)b).getVVelocity(), 0.5);

			// find normal of collision
			double normal = Math.atan2(b.getY() - a.getY(), b.getX() - a.getX());
			
			// find separation between ball trajectories and normal
			double a_dir_normal_before = a_dir_before - normal;
			double b_dir_normal_before = b_dir_before - normal;
			
			// find the velocities of each ball along the normal and tangent directions
			double a_vel_nor_before = a_vel_before * Math.cos(a_dir_normal_before);
			double a_vel_tan_before = a_vel_before * Math.sin(a_dir_normal_before);
			
			double b_vel_nor_before = b_vel_before * Math.cos(b_dir_normal_before);
			double b_vel_tan_before = b_vel_before * Math.sin(b_dir_normal_before);
			
			// find velocities after collision, relative to the normal
			// balls keep their normal velocities but exchange tangent velocities
			double a_vel_nor_after = b_vel_nor_before;
			double a_vel_tan_after = a_vel_tan_before;

			double b_vel_nor_after = a_vel_nor_before;
			double b_vel_tan_after = b_vel_tan_before;
			
			// find velocities after collision, total
			double a_vel_after = Math.pow(a_vel_nor_after * a_vel_nor_after + a_vel_tan_after * a_vel_tan_after, 0.5);
			double b_vel_after = Math.pow(b_vel_nor_after * b_vel_nor_after + b_vel_tan_after * b_vel_tan_after, 0.5);
			
			// shrink speeds to speed limit
			if (a_vel_after > ((IntelligentSprite)a).getTopSpeed()) a_vel_after = ((IntelligentSprite)a).getTopSpeed(); 
			if (b_vel_after > ((IntelligentSprite)b).getTopSpeed()) b_vel_after = ((IntelligentSprite)b).getTopSpeed(); 

			// find trajectory (relative to normal), after collision
			double a_dir_normal_after = Math.atan2(a_vel_tan_after, a_vel_nor_after);
			double b_dir_normal_after = Math.atan2(b_vel_tan_after, b_vel_nor_after);
			
			// find trajectory (relative to original coordinate system), after collision
			// ball keeps normal velocity but tangent velocity goes negative since it is reflecting at an angle equal to the angle of incidence
			double a_dir_after = a_dir_normal_after + normal;
			double b_dir_after = b_dir_normal_after + normal;
			
			// find vector velocities (relative to original coordinate system), after collision
			a.setHVelocity((float)(a_vel_after * Math.cos(a_dir_after)));
			a.setVVelocity((float)(a_vel_after * Math.sin(a_dir_after)));
			/* a.setCollidable(false); */

			((IntelligentSprite)b).setHVelocity((float)(b_vel_after * Math.cos(b_dir_after)));
			((IntelligentSprite)b).setVVelocity((float)(b_vel_after * Math.sin(b_dir_after)));
			
			// both take minor damage
			a.damage(SHIP_DAMAGE);
			b.damage(SHIP_DAMAGE);
			if (a.getAlive() == true)
				spriteAnimations.add(new SpriteAnimation(imgsShield, 0.1, false, a));
			else
			{
				spriteAnimations.add(new SpriteAnimation(imgsExplosion, 1.0, false, a));
				ship_explosion.play();
			}
			if (b.getAlive() == true)
				spriteAnimations.add(new SpriteAnimation(imgsShield, 0.1, false, b));
			else
			{
				spriteAnimations.add(new SpriteAnimation(imgsExplosion, 1.0, false, b));
				ship_explosion.play();
			}
			return true;
		}
		else
			return false;
	}

	public boolean collide(IntelligentSprite a, DynamicSprite b)
	{ // ship with slug or torpedo
		if (doCollide(a, b))
		{	
			// ship takes moderate damage, slug or torpedo
			if (b.getExistsOnLevel() == Level.SLUG)
				a.damage(SLUG_DAMAGE);
			else
			{	// torpedo causes explosion on itself
				spriteAnimations.add(new SpriteAnimation(imgsExplosion, 1.0, false, b));
				torpedo_explosion.play();
				a.damage(TORPEDO_DAMAGE);
			}
			b.damage();
			if (a.getAlive() == true) // shield flickers or ship explodes if it dies
				spriteAnimations.add(new SpriteAnimation(imgsShield, 0.1, false, a));
			else
			{
				spriteAnimations.add(new SpriteAnimation(imgsExplosion, 1.0, false, a));
				ship_explosion.play();
			}
			return true;
		}
		else
			return false;
	}

	public boolean collide(IntelligentSprite a, StaticSprite b)
	{ // ship with planet
		/* if a ship gets inside a planet's radius before bouncing out due to a threading pause
		 * it will start colliding rapidly, die, but still collide and produce animations of
		 * exploding forever, and therefore the game will never reset.  This doesn't eliminate
		 * the problem of snagging on the planet, but at least once the ship is dead it won't
		 * continue to collide and produce animations, so the game will reset.
		 */
		if (a.getAlive() == false)
			return false;
		if (doCollide(a, b))
		{
			// find trajectory of ship
			double a_dir_before = Math.atan2(a.getVVelocity(),a.getHVelocity());
			
			// find combined vector velocity of ship
			double a_vel_before = Math.pow(a.getHVelocity() * a.getHVelocity() + a.getVVelocity() * a.getVVelocity(), 0.5);
		
			// find normal of collision
			double normal = Math.atan2(b.getY() - a.getY(), b.getX() - a.getX());
			
			// find separation between ship trajectory and normal
			double a_dir_normal_before = a_dir_before - normal;
			
			// find the velocity of ball along the normal and tangent directions
			double a_vel_nor_before = a_vel_before * Math.cos(a_dir_normal_before);
			double a_vel_tan_before = a_vel_before * Math.sin(a_dir_normal_before);
			
			// find velocity after collision, relative to the normal
			double a_vel_nor_after = -a_vel_nor_before;
			double a_vel_tan_after = a_vel_tan_before;
			
			// find velocity after collision, total
			double a_vel_after = Math.pow(a_vel_nor_after * a_vel_nor_after + a_vel_tan_after * a_vel_tan_after, 0.5);
			
			// do not need to check speed limit because ship can't increase speed on bounce
			
			// find trajectory (relative to normal), after collision
			double a_dir_normal_after = Math.atan2(a_vel_tan_after, a_vel_nor_after);
			
			// find trajectory (relative to original coordinate system), after collision
			double a_dir_after = a_dir_normal_after + normal;
			
			// find vector velocity (relative to original coordinate system), after collision
			a.setHVelocity((float)(a_vel_after * Math.cos(a_dir_after)));
			a.setVVelocity((float)(a_vel_after * Math.sin(a_dir_after)));
			/* a.setCollidable(false); */
			
			// ship takes major damage
			a.damage(PLANET_DAMAGE);
			if (a.getAlive() == true)
				spriteAnimations.add(new SpriteAnimation(imgsShield, 0.1, false, a));
			else
			{
				spriteAnimations.add(new SpriteAnimation(imgsExplosion, 1.0, false, a));
				ship_explosion.play();
			}
			return true;
		}
		else
			return false;
	}
	
	public boolean collide(DynamicSprite a, DynamicSprite b)
	{ // slug with torpedo
		if (doCollide(a, b))
		{
			// Both slugs/torpedoes die, they have no shield points
			if (a.getExistsOnLevel () == Level.TORPEDO)
			{
				spriteAnimations.add(new SpriteAnimation(imgsExplosion, 1.0, false, a));
				torpedo_explosion.play();
			}
			if (b.getExistsOnLevel () == Level.TORPEDO)
			{
				spriteAnimations.add(new SpriteAnimation(imgsExplosion, 1.0, false, b));
				torpedo_explosion.play();
			}
			a.damage();
			b.damage();
			return true;
		}
		else
			return false;
	}
	
	public boolean collide(DynamicSprite a, StaticSprite b)
	{ // slug or torpedo with planet
		if (doCollide(a, b))
		{
			// Planet cannot be damaged, slug/torpedo dies
			if (a.getExistsOnLevel () == Level.TORPEDO)
			{
				spriteAnimations.add(new SpriteAnimation(imgsExplosion, 1.0, false, a));
				torpedo_explosion.play();
			}
			a.damage();
			return true;
		}
		else
			return false;
	}
	
	public boolean collide(StaticSprite a, StaticSprite b)
	{ // planet with planet
		if (doCollide(a, b))
			// not really possible, planets don't move
			return true;
		else
			return false;
	}

	public void shootSlug(IntelligentSprite i)
	{
		if (i.getWeaponEnergy() > 0)
		{
			i.setWeaponEnergy(i.getWeaponEnergy() - 1);
			DynamicSprite slug = new DynamicSprite(imgSlug);
			slug.setX(i.getX());
			slug.setY(i.getY());
			slug.setWidth(2);
			slug.setHeight(2);
			slug.setVisible(true);
			slug.setExistsOnLevel(Level.SLUG);
			if (i == player1)
				slug.setCollidesWithLevels(new Level[] {Level.PLANET, Level.PLAYER2, Level.TORPEDO});
			else // player2
				slug.setCollidesWithLevels(new Level[] {Level.PLANET, Level.PLAYER1, Level.TORPEDO});
			float firingAngleRadians = (float)(i.getRotation() * 22.5 * Math.PI / 180.0f);
			slug.setHVelocity(i.getHVelocity() + (float)(Math.cos(firingAngleRadians) * SLUG_SPEED));
			slug.setVVelocity(i.getVVelocity() + (float)(Math.sin(firingAngleRadians) * SLUG_SPEED));
			slug.setRotation(i.getRotation());
			slugs.add(slug);
			slug_launch.play();
		}
	}
	
	public void shootTorpedo(IntelligentSprite i)
	{
		if (i.getWeaponEnergy() > 0)
		{
			i.setWeaponEnergy(i.getWeaponEnergy() - 5);
			DynamicSprite torpedo = new DynamicSprite(imgTorpedo);
			torpedo.setX(i.getX());
			torpedo.setY(i.getY());
			torpedo.setWidth(8);
			torpedo.setHeight(8);
			torpedo.setRotation(0);
			torpedo.rotate(i.getRotation());
			torpedo.setVisible(true);
			torpedo.setExistsOnLevel(Level.TORPEDO);
			if (i == player1)
				torpedo.setCollidesWithLevels(new Level[] {Level.PLANET, Level.PLAYER2, Level.SLUG, Level.TORPEDO});
			else // player2
				torpedo.setCollidesWithLevels(new Level[] {Level.PLANET, Level.PLAYER1, Level.SLUG, Level.TORPEDO});
			float firingAngleRadians = (float)(i.getRotation() * 22.5 * Math.PI / 180.0f);
			torpedo.setHVelocity(i.getHVelocity() + (float)(Math.cos(firingAngleRadians) * TORPEDO_SPEED));
			torpedo.setVVelocity(i.getVVelocity() + (float)(Math.sin(firingAngleRadians) * TORPEDO_SPEED));
			torpedo.setRotation(i.getRotation());
			torpedoes.add(torpedo);
			torpedo_launch.play();
		}
	}
	
	public void hyperspace(IntelligentSprite i)
	{
		if (i.getWeaponEnergy() >= 10)
		{
			i.setX((float)Math.random() * PWIDTH);
			i.setY((float)Math.random() * PHEIGHT);
			i.setWeaponEnergy(i.getWeaponEnergy() - 10);
			ship_warp.play();
		}
	}
	
	public void cloak(IntelligentSprite i)
	{
		if (i.getWeaponEnergy() >= 10)
		{
			i.setLastCloakTime((long) (System.nanoTime() / 1e9));
			i.setVisible(false);
			i.setWeaponEnergy(i.getWeaponEnergy() - 10);
		}
	}

	@Override 
	public void update(Graphics g) { paint(g); } 
	 
	@Override 
	public void paint(Graphics g) // why not paintComponent()?
	{
		//super.paint(g);  // why JApplet doesn't have paintComponent()?  and why do I get flicker if I use this?
		if (dbImage != null)
			g.drawImage(dbImage, 0, 0, null);
	}
	
	private void gameOverMessage(Graphics g)
	{
		// center the game-over message
		String msg = "Game Over";
		// code to calculate x and y
		int x = PWIDTH / 2;
		int y = PHEIGHT / 2;
		g.drawString(msg, x, y);
	} // end of gameOverMessage();
}
