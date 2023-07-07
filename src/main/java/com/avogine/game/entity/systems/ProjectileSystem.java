package com.avogine.game.entity.systems;

import java.util.*;

import org.joml.*;

import com.avogine.ecs.EntityManager;
import com.avogine.ecs.components.TransformComponent;
import com.avogine.game.Game;
import com.avogine.game.entity.components.*;
import com.avogine.game.scene.ECSScene;
import com.avogine.game.util.*;
import com.avogine.logging.AvoLog;

/**
 * TODO Scrap this for a real physics engine, or if you're crazy, don't have n^2 checks
 */
public class ProjectileSystem implements Updateable {

	private final Set<UUID> projectilesToRemove;
	private final Set<UUID> hitList;
	
	private final Quaternionf projectileOrientation;
	private final Vector3f projectileDirection;
	
	/**
	 * 
	 */
	public ProjectileSystem() {
		projectilesToRemove = new HashSet<>();
		hitList = new HashSet<>();
		projectileOrientation = new Quaternionf();
		projectileDirection = new Vector3f();
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
		projectilesToRemove.clear();
		hitList.clear();
		
		// TODO The physics system should handle updating collider body positions and collision resolution with other entities
//		List<Collidable> collidables = manager.query(Collidable.class).collect(Collectors.toList());
//		collidables.forEach(collidable -> {
//			var flag = collidable.collider.collisionFlag();
//			collidables.stream()
//				.filter(coll -> (coll.collider.collisionFlag() ^ flag) != 0)
//				.forEach(hittable -> {
//				});
//		});
		
		var shootables = manager.query(ShootableTag.class, TransformComponent.class).toList();
		
		manager.query(ProjectileComponent.class, TransformComponent.class, PhysicsComponent.class).forEach(chunk -> {
			for (int i = 0; i < chunk.getChunkSize(); i++) {
				var projectile = chunk.getAs(ProjectileComponent.class, i);
				
				projectile.setTimeToLive(projectile.getTimeToLive() - delta);
				if (projectile.getTimeToLive() <= 0) {
					projectilesToRemove.add(chunk.getID(i));
					continue;
				}
				
				var transform = chunk.getAs(TransformComponent.class, i);
				var physics = chunk.getAs(PhysicsComponent.class, i);
				
				transform.orientation(projectileOrientation);
				projectileOrientation.transformUnitPositiveZ(projectileDirection).mul(-1);
				var ray = new RayAabIntersection(transform.x(), transform.y(), transform.z(),
						projectileDirection.x, projectileDirection.y, projectileDirection.z);
				float pSpeed = physics.getVelocity().length() * delta;
				UUID id = chunk.getID(i);
				
				shootables.stream()
				.takeWhile(shootChunk -> !projectilesToRemove.contains(id))
				.forEach(shootChunk -> {
					for (int j = 0; j < shootChunk.getChunkSize(); j++) {
						if (hitList.contains(shootChunk.getID(j))) {
							continue;
						}
						var targetTransform = shootChunk.getAs(TransformComponent.class, j);
						if (Vector3f.distance(transform.x(), transform.y(), transform.z(), targetTransform.x(), targetTransform.y(), targetTransform.z()) > pSpeed * 2) {
							continue;
						}
						
						boolean rayTest = ray.test(targetTransform.x() - targetTransform.sx(), targetTransform.y() - targetTransform.sy(), targetTransform.z() - targetTransform.sz(),
								targetTransform.x() + targetTransform.sx(), targetTransform.y() + targetTransform.sy(), targetTransform.z() + targetTransform.sz());
						if (rayTest) {
							hitList.add(shootChunk.getID(j));
							AvoLog.log().debug("Removing index {} in chunk with size {}", j, shootChunk.getChunkSize());
							projectilesToRemove.add(id);
							continue;
						}
					}
				});
			}
		});
		
		hitList.forEach(manager::removeEntity);
		projectilesToRemove.forEach(manager::removeEntity);
	}

}
