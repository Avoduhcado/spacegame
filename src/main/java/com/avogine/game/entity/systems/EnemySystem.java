package com.avogine.game.entity.systems;

import org.joml.*;
import org.joml.Math;

import com.avogine.ecs.*;
import com.avogine.ecs.components.*;
import com.avogine.game.Game;
import com.avogine.game.entity.components.*;
import com.avogine.game.scene.ECSScene;
import com.avogine.game.util.*;
import com.avogine.render.data.material.TexturedMaterial;
import com.avogine.render.data.mesh.*;
import com.avogine.render.loader.parshapes.ParShapesBuilder;
import com.avogine.render.loader.texture.TextureCache;

/**
 *
 */
public class EnemySystem implements Updateable {

	private static final String ENEMY_MODEL_NAME = "Enemy";
	
	private final Vector3f enemyPosition;
	private final Quaternionf enemyOrientation;
	private final Vector3f followDistance;
	
	/**
	 * 
	 */
	public EnemySystem() {
		enemyPosition = new Vector3f();
		enemyOrientation = new Quaternionf();
		followDistance = new Vector3f();
	}

	@Override
	public void onRegister(Game game) {
		var material = new TexturedMaterial(TextureCache.getInstance().getTexture("AGDG Logo.png"));
		Mesh enemyMesh = new ParShapesBuilder().createCube().translate(-0.5f, -0.5f, -0.5f).build();
		Models.CACHE.putModel(ENEMY_MODEL_NAME, new Model(enemyMesh, material));
	}
	
	@Override
	public void onUpdate(GameState gameState) {
		if (gameState.scene() instanceof ECSScene scene) {
			processEnemies(scene.getEntityManager());
		}
	}
	
	private void processEnemies(EntityManager manager) {
		long enemyCount = manager.query(EnemyTag.class, TransformComponent.class).mapToLong(EntityChunk::getChunkSize).sum();
		
		manager.query(PlayerTag.class, TransformComponent.class).findFirst().ifPresent(playerChunk -> {
			var playerTransform = playerChunk.getAs(TransformComponent.class, 0);
			if (enemyCount < 10) {
				long enemiesToGenerate = 30 - enemyCount;
				while (enemiesToGenerate > 0) {
					generateEnemy(manager, playerTransform);
					enemiesToGenerate--;
				}
			}
			
			manager.query(EnemyTag.class, TransformComponent.class).forEach(chunk -> {
				for (int i = 0; i < chunk.getChunkSize(); i++) {
					var enemyTransform = chunk.getAs(TransformComponent.class, i);
					// Set the enemy to look at the player's position
					enemyOrientation.identity().lookAlong(playerTransform.x() - enemyTransform.x(), playerTransform.y() - enemyTransform.y(), playerTransform.z() - enemyTransform.z(),
							0, 1, 0).invert();
					enemyTransform.setOrientation(enemyOrientation);
//					
					// Super jank "follow" system
					enemyTransform.position(enemyPosition);
					float distanceFromPlayer = enemyPosition.distance(playerTransform.x(), playerTransform.y(), playerTransform.z());
					enemyPosition.sub(playerTransform.x(), playerTransform.y(), playerTransform.z(), followDistance);
					followDistance.mul(1 - (distanceFromPlayer / 50));
					enemyPosition.add(followDistance);
					enemyTransform.x(enemyPosition.x).y(enemyPosition.y).z(enemyPosition.z);
				}
			});
		});
	}
	
	private void generateEnemy(EntityManager manager, TransformComponent playerTransform) {
		float enemySpawnDistance = 50;
		
		enemyPosition
		.set((float) ((Math.random() * 2) - 1), (float) ((Math.random() * 2) - 1), (float) ((Math.random() * 2) - 1))
		.mul(enemySpawnDistance)
		.add(playerTransform.x(), playerTransform.y(), playerTransform.z());
		
		enemyOrientation.identity().lookAlong(playerTransform.x() - enemyPosition.x(), playerTransform.y() - enemyPosition.y(), playerTransform.z() - enemyPosition.z(),
				0, 1, 0).invert();
		
		var enemyTransform = new TransformComponent(enemyPosition.x(), enemyPosition.y(), enemyPosition.z(),
				enemyOrientation.x(), enemyOrientation.y(), enemyOrientation.z(), enemyOrientation.w());
		var model = new ModelComponent(ENEMY_MODEL_NAME);
		manager.createEntityWith(enemyTransform, model, new ShootableTag(), new EnemyTag());
	}

}
