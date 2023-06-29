package com.avogine.game.entity.systems;

import java.lang.Math;

import org.joml.*;

import com.avogine.ecs.EntityManager;
import com.avogine.ecs.components.TransformComponent;
import com.avogine.ecs.queries.EntityBiQuery;
import com.avogine.game.Game;
import com.avogine.game.entity.components.PhysicsComponent;
import com.avogine.game.scene.ECSScene;
import com.avogine.game.util.*;
import com.avogine.util.*;

/**
 *
 */
public class PhysicsSystem implements Updateable {

	private static final Vector3f ZERO_VECTOR = new Vector3f();
	
	private static final class PhysicsQuery extends EntityBiQuery<TransformComponent, PhysicsComponent> {

		private final Quaternionf transformQuaternion = new Quaternionf();
		private final Vector3f transformPosition = new Vector3f();
		
		private final Vector3f acceleration = new Vector3f();
		private final Vector3f orientedVelocity = new Vector3f();
		
		private final Quaternionf rotationQuaternion = new Quaternionf();
		
		private float delta;
		
		public void loadDelta(float delta) {
			this.delta = delta;
		}
		
		@Override
		public void accept(TransformComponent transform, PhysicsComponent physics) {
			transformQuaternion.identity();
			transformPosition.set(0);
			
			acceleration.set(0);
			orientedVelocity.set(0);
			
			rotationQuaternion.identity();

			transformPosition.set(transform.x(), transform.y(), transform.z());
			transformQuaternion.set(transform.rx(), transform.ry(), transform.rz(), transform.rw());
			
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

//				AvoLog.log().debug("Speed: {}", physics.getSpeed());
//				AvoLog.log().debug("Velocity: {} \t\tAcceleration: {}m/s^2 \t\tDirection: {}", VectorUtil.printVector(physics.getVelocity()), VectorUtil.printVector(acceleration), VectorUtil.printVector(physics.getImpulse()));

				transformQuaternion.transform(physics.getVelocity(), orientedVelocity).mul(delta);
				transformPosition.add(orientedVelocity);
				transform.x(transformPosition.x).y(transformPosition.y).z(transformPosition.z);
				
//					if (physics.getFriction() != 0 && physics.getVelocity().length() > physics.getSpeed()) {
//						float friction = physics.getFriction();
//						if (physics.getVelocity().length() > physics.getSpeed() && !physics.isBoost()) {
//							friction *= 2;
//						}
//						 
//						physics.getVelocity().normalize(orientedVelocity).mul(friction, acceleration);
//						acceleration.mul(delta);
//						
//						if (physics.getVelocity().length() - acceleration.length() < 0) {
//							acceleration.normalize(physics.getVelocity().length());
//						}
//						physics.getVelocity().sub(acceleration);
//					}
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
				transformQuaternion.mul(rotationQuaternion);
				transform.rx(transformQuaternion.x).ry(transformQuaternion.y).rz(transformQuaternion.z).rw(transformQuaternion.w);
				physics.getRotationalVelocity().lerp(ZERO_VECTOR, physics.getRotationalFriction() * delta);
			}
		}
		
		private boolean isMoving(PhysicsComponent physics) {
			return physics.getVelocity().length() > 0f || physics.getImpulse().length() > 0;
		}
	};
	
	private static final PhysicsQuery physicsQuery = new PhysicsQuery();
	
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
		physicsQuery.loadDelta(delta);
		
		manager.queryAndProcess(physicsQuery);
	}
	
}
