package com.avogine.game.entity.systems;

import java.util.*;

import com.avogine.ecs.*;
import com.avogine.ecs.components.TransformComponent;
import com.avogine.game.Game;
import com.avogine.game.entity.components.*;
import com.avogine.game.scene.ECSScene;
import com.avogine.game.util.*;

/**
 * TODO Scrap this for a real physics engine, or if you're crazy, don't have n^2 checks
 */
public class ProjectileSystem extends EntitySystem implements Updateable {

	private static record ProjectileArchetype(UUID id, ProjectileComponent projectile, TransformComponent transform, PhysicsComponent physics) implements EntityArchetype {}
	private static record Hittable(UUID id, TransformComponent transform, ShootableTag tag) implements EntityArchetype {}
	private static record Collidable(UUID id, TransformComponent transform, ColliderComponent collider) implements EntityArchetype {}
	
	/**
	 * 
	 */
	public ProjectileSystem() {
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
		Set<UUID> projectilesToRemove = new HashSet<>();
		Set<UUID> hitList = new HashSet<>();
		
		// TODO The physics system should handle updating collider body positions and collision resolution with other entities
//		List<Collidable> collidables = manager.query(Collidable.class).collect(Collectors.toList());
//		collidables.forEach(collidable -> {
//			var flag = collidable.collider.collisionFlag();
//			collidables.stream()
//				.filter(coll -> (coll.collider.collisionFlag() ^ flag) != 0)
//				.forEach(hittable -> {
//				});
//		});
		
		manager.query(ProjectileArchetype.class).forEach(projectable -> {
//			var pDirection = projectable.transform.orientation().transform(new Vector3f(0, 0, -1));
//			var ray = new RayAabIntersection(projectable.transform.position().x, projectable.transform.position().y, projectable.transform.position().z,
//					pDirection.x, pDirection.y, pDirection.z);
//			var pSpeed = projectable.physics.getVelocity().length() * delta;
//			
//			// Loop through all hittable targets and check for collisions, the takeWhile will short-circuit the search if something's already been hit
//			manager.query(Hittable.class)
//			.takeWhile(hittable -> !projectilesToRemove.contains(projectable.id))
//			.forEach(hittable -> {
//				float distance = projectable.transform.position().distance(hittable.transform.position());
//				if (distance > pSpeed * 2) {
//					return;
//				}
//				
//				boolean rayTest = ray.test(hittable.transform.position().x - hittable.transform.scale().x, hittable.transform.position().y - hittable.transform.scale().y, hittable.transform.position().z - hittable.transform.scale().z,
//						hittable.transform.position().x + hittable.transform.scale().x, hittable.transform.position().y + hittable.transform.scale().y, hittable.transform.position().z + hittable.transform.scale().z);
//				if (rayTest) {
//					AvoLog.log().debug("Hit a planet at distance: {}", distance);
//					hitList.add(hittable.id);
//					projectilesToRemove.add(projectable.id);
//				}
//			});
//			// Remove any hittable targets before the next iteration
//			hitList.forEach(id -> manager.removeEntity(id));
//			hitList.clear();
			
			projectable.projectile.setTimeToLive(projectable.projectile.getTimeToLive() - delta);
			if (projectable.projectile.getTimeToLive() <= 0) {
				projectilesToRemove.add(projectable.id);
			}
		});
		
		projectilesToRemove.forEach(id -> manager.removeEntity(id));
	}

}
