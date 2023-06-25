package com.avogine.game.entity.systems;

import java.lang.Math;
import java.util.UUID;

import org.joml.*;

import com.avogine.ecs.*;
import com.avogine.ecs.components.TransformComponent;
import com.avogine.game.Game;
import com.avogine.game.entity.components.PhysicsComponent;
import com.avogine.game.scene.ECSScene;
import com.avogine.game.util.*;
import com.avogine.util.*;

/**
 *
 */
public class PhysicsSystem extends EntitySystem implements Updateable {

	private static record PhysicalArchetype(UUID id, TransformComponent transform, PhysicsComponent physics) implements EntityArchetype {}
	
	private static final Vector3f ZERO_VECTOR = new Vector3f();
	
	/**
	 * 
	 */
	public PhysicsSystem() {
	}
	
	@Override
	public void onRegister(Game game) {
	}
	
	@Override
	public void onUpdate(GameState gameState) {
		if (gameState.scene() instanceof ECSScene scene) {
			process(scene.getEntityManager(), gameState.delta());
		}
	}
	
	private void process(EntityManager manager, float delta) {
		final var acceleration = new Vector3f();
		final var orientedVelocity = new Vector3f();

		final var rotationQuaternion = new Quaternionf();

		manager.query(PhysicalArchetype.class).forEach(physical -> {
			performPhysics(physical.transform, physical.physics, acceleration, orientedVelocity, rotationQuaternion, delta);
		});
	}

	private void performPhysics(TransformComponent transform, PhysicsComponent physics, Vector3f acceleration, Vector3f orientedVelocity, Quaternionf rotationQuaternion, float delta) {
		if (isMoving(physics)) {
			float speed = physics.getMaxSpeed();
			
			physics.getImpulse().mul(speed, acceleration);
			acceleration.mul(delta);
			
			if (physics.getVelocity().length() + acceleration.length() > speed
					&& acceleration.length() != 0
					&& acceleration.angle(physics.getVelocity()) < Math.toRadians(30)) {
				acceleration.normalize(MathUtil.clamp(speed - physics.getVelocity().length(), 0, acceleration.length()));
			}
			physics.getVelocity().add(acceleration);

//				AvoLog.log().debug("Speed: {} \tBoost: {} \tTotal: {}", physics.getSpeed(), physics.getSpeedBoost(), physics.getTotalSpeed());
//				AvoLog.log().debug("Velocity: {} \t\tAcceleration: {}m/s^2 \t\tDirection: {}", VectorUtil.printVector(physics.getVelocity()), VectorUtil.printVector(acceleration), VectorUtil.printVector(physics.getImpulse()));
			
			transform.orientation().transform(physics.getVelocity(), orientedVelocity).mul(delta);
			transform.position().add(orientedVelocity);
			
//				if (physics.getFriction() != 0 && physics.getVelocity().length() > physics.getSpeed()) {
//					float friction = physics.getFriction();
//					if (physics.getVelocity().length() > physics.getSpeed() && !physics.isBoost()) {
//						friction *= 2;
//					}
//					 
//					physics.getVelocity().normalize(orientedVelocity).mul(friction, acceleration);
//					acceleration.mul(delta);
//					
//					if (physics.getVelocity().length() - acceleration.length() < 0) {
//						acceleration.normalize(physics.getVelocity().length());
//					}
//					physics.getVelocity().sub(acceleration);
//				}
			if (physics.getFriction() != 0 && physics.getVelocity().length() > 0) {
				var deceleration = physics.getVelocity().mul(physics.getFriction() * delta, new Vector3f()).negate();
				
				VectorUtil.clampDirection(physics.getVelocity(), deceleration);
				if (physics.getVelocity().length() < 1f / 60f) {
					physics.getVelocity().zero();
				}
			}
			
			physics.getImpulse().zero();
		}
		
		if (physics.getRotationalVelocity().length() != 0) {
			rotationQuaternion.fromAxisAngleDeg(physics.getRotationalVelocity(), physics.getRotationalVelocity().length() * delta);
			transform.orientation().mul(rotationQuaternion);
			physics.getRotationalVelocity().lerp(ZERO_VECTOR, physics.getRotationalFriction() * delta);
		}
	}
	
	private boolean isMoving(PhysicsComponent physics) {
		return physics.getVelocity().length() > 0f || physics.getImpulse().length() > 0;
	}

}
