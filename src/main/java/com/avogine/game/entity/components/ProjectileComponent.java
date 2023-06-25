package com.avogine.game.entity.components;

import com.avogine.ecs.EntityComponent;

/**
 *
 */
public class ProjectileComponent implements EntityComponent {

	private float timeToLive;
	
	// TODO Add parent shooter reference, probably as just an ID?
	
	/**
	 * @param timeToLive 
	 * 
	 */
	public ProjectileComponent(float timeToLive) {
		this.timeToLive = timeToLive;
	}
	
	/**
	 * Copy constructor to create a new component with the same values as the supplied component.
	 * @param copy The component to copy.
	 */
	public ProjectileComponent(ProjectileComponent copy) {
		this.timeToLive = copy.timeToLive;
	}
	
	/**
	 * @return the timeToLive
	 */
	public float getTimeToLive() {
		return timeToLive;
	}
	
	/**
	 * @param timeToLive the timeToLive to set
	 */
	public void setTimeToLive(float timeToLive) {
		this.timeToLive = timeToLive;
	}
	
}
