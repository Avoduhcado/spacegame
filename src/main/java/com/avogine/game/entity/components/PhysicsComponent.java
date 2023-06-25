package com.avogine.game.entity.components;

import org.joml.Vector3f;

import com.avogine.ecs.EntityComponent;

/**
 *
 */
public class PhysicsComponent implements EntityComponent {

	private static final float DEFAULT_SPEED = 15f;
	
	private static final float DEFAULT_FRICTION = 1.5f;
	private static final float DEFAULT_ROTATIONAL_FRICTION = 10f;
	
	private final Vector3f velocity;
	private final Vector3f impulse;
	private final Vector3f rotationalVelocity;
	
	private float speed;
	private boolean boost;
	
	private float friction;
	private float rotationalFriction;
	
	/**
	 * Construct new component with default velocities.
	 */
	public PhysicsComponent() {
		this(new Vector3f(), new Vector3f(), DEFAULT_SPEED, DEFAULT_FRICTION, DEFAULT_ROTATIONAL_FRICTION);
	}
	
	/**
	 * Copy constructor to create a new component with the same values as the supplied component.
	 * @param copy The component to copy.
	 */
	public PhysicsComponent(PhysicsComponent copy) {
		this.velocity = new Vector3f(copy.velocity);
		this.impulse = new Vector3f(copy.impulse);
		this.rotationalVelocity = new Vector3f(copy.rotationalVelocity);
		this.speed = copy.speed;
		this.friction = copy.friction;
		this.rotationalFriction = copy.rotationalFriction;
	}
	
	/**
	 * Construct new component with given velocities.
	 * @param velocity
	 * @param rotationalVelocity
	 * @param speed
	 * @param friction 
	 * @param rotationalFriction 
	 */
	public PhysicsComponent(Vector3f velocity, Vector3f rotationalVelocity, float speed, float friction, float rotationalFriction) {
		this.velocity = velocity;
		this.impulse = new Vector3f();
		this.rotationalVelocity = rotationalVelocity;
		this.speed = speed;
		this.friction = friction;
		this.rotationalFriction = rotationalFriction;
	}
	
	/**
	 * @return the velocity
	 */
	public Vector3f getVelocity() {
		return velocity;
	}
	
	/**
	 * @return the impulse
	 */
	public Vector3f getImpulse() {
		return impulse;
	}
	
	/**
	 * @return the rotationalVelocity
	 */
	public Vector3f getRotationalVelocity() {
		return rotationalVelocity;
	}
	
	/**
	 * @return speed + speedBoost
	 */
	public float getMaxSpeed() {
		return speed + (boost ? speed : 0);
	}
	
	/**
	 * @return the speed Distance traveled in meters per second.
	 */
	public float getSpeed() {
		return speed;
	}
	
	/**
	 * @param speed the speed to set
	 */
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	
	/**
	 * @return the boost
	 */
	public boolean isBoost() {
		return boost;
	}
	
	/**
	 * @param boost the boost to set
	 */
	public void setBoost(boolean boost) {
		this.boost = boost;
	}
	
	/**
	 * @return the friction
	 */
	public float getFriction() {
		return friction;
	}
	
	/**
	 * @param friction the friction to set
	 */
	public void setFriction(float friction) {
		this.friction = friction;
	}
	
	/**
	 * @return the rotationalFriction
	 */
	public float getRotationalFriction() {
		return rotationalFriction;
	}
	
}
