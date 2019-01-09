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

import java.awt.image.BufferedImage;	

/* This is the most advanced sprite.  It moves, is controllable, shoots, takes damage, dies.  It is used for player ships. */
public class IntelligentSprite extends DynamicSprite
{
	private float topSpeed;
	private int topEnergy;
	private int weaponEnergy;
	private int shieldEnergy;
	private Regen lastRegenerated;
	public static enum Regen
	{
		SHIELD, WEAPON;
	}
	private long lastRegenTime;
	private long lastCloakTime;

	// accessors
	public float getTopSpeed() { return this.topSpeed; }
	void setTopSpeed(float topSpeed) { this.topSpeed = topSpeed; }
	public int getTopEnergy() { return this.topEnergy; }
	void setTopEnergy(int topEnergy) { this.topEnergy = topEnergy; }
	public int getWeaponEnergy() { return this.weaponEnergy; }
	void setWeaponEnergy(int weaponEnergy) { this.weaponEnergy = weaponEnergy; } 
	public int getShieldEnergy() { return this.shieldEnergy; }
	void setShieldEnergy(int shieldEnergy) { this.shieldEnergy = shieldEnergy; } 
	public long getLastRegenTime() { return this.lastRegenTime; }
	void setLastRegenTime(long lastRegenTime) { this.lastRegenTime = lastRegenTime; } 
	public long getLastCloakTime() { return this.lastCloakTime; }
	void setLastCloakTime(long lastCloakTime) { this.lastCloakTime = lastCloakTime; } 

	// constructors
	IntelligentSprite() {
		this.lastRegenerated = Regen.WEAPON;
		this.lastRegenTime = 0;
	}
	IntelligentSprite(BufferedImage bufImg)
	{
		this.lastRegenerated = Regen.WEAPON;
		this.lastRegenTime = 0;
		this.bufImg = bufImg;
		this.rotate(0);
	}
	
	void accelerate(int acceleration)
	{
		// find the rotation in Cartesian radians
		double phi = (this.getRotation() * -22.5) * (Math.PI / 180.0);
		float newHVelocity = this.hVelocity + (float)Math.cos(phi);
		float newVVelocity = this.vVelocity - (float)Math.sin(phi);
		// thrust has no effect if we're already at the speed limit
		if ((float)Math.pow(Math.pow(newHVelocity, 2.0) + Math.pow(newVVelocity, 2.0), 0.5) <= this.topSpeed)
		{
			this.setHVelocity(newHVelocity);
			this.setVVelocity(newVVelocity);
		}
	}
	
	void transferWtoS()
	{
		if (this.weaponEnergy > 0 && this.shieldEnergy < this.topEnergy)
		{
			this.weaponEnergy--;
			this.shieldEnergy++;
		}
	}
	
	void transferStoW()
	{
		if (this.shieldEnergy > 0 && this.weaponEnergy < this.topEnergy)
		{
			this.shieldEnergy--;
			this.weaponEnergy++;
		}
	}
	
	void damage(int points)
	{
		if (this.shieldEnergy >= 0)	
			this.shieldEnergy = this.shieldEnergy - points;
		if (this.shieldEnergy < 0)
		{
			this.visible = false;
			this.alive = false;
		}
	}
	
	void regen()
	{ // it alternates between weapons and shields, until one is full, then only goes to the other
		if (this.lastRegenerated == Regen.SHIELD || this.shieldEnergy == this.topEnergy)
		{
			if (this.weaponEnergy < this.topEnergy)
				this.weaponEnergy++;
			this.lastRegenerated = Regen.WEAPON;
		}
		else if (this.lastRegenerated == Regen.WEAPON || this.weaponEnergy == this.topEnergy)
		{
			if (this.shieldEnergy < this.topEnergy)
				this.shieldEnergy++;
			this.lastRegenerated = Regen.SHIELD;
		}
	}
}
