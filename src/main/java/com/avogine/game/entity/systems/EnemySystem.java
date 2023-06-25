package com.avogine.game.entity.systems;

import java.util.UUID;

import org.joml.Vector3f;

import com.avogine.ecs.*;
import com.avogine.ecs.addons.ModelCache;
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
public class EnemySystem extends EntitySystem implements Updateable {

	private static record EnemyArchetype(UUID id, EnemyTag tag, TransformComponent transform) implements EntityArchetype {}
	private static record PlayerArchetype(UUID id, PlayerTag tag, TransformComponent transform) implements EntityArchetype {}
	
	private static final String ENEMY_MODEL_NAME = "Enemy";
	
	/**
	 * 
	 */
	public EnemySystem() {
	}

	@Override
	public void onRegister(Game game) {
	}
	
	@Override
	public void onUpdate(GameState gameState) {
		if (gameState.scene() instanceof ECSScene scene) {
			processEnemies(scene);
		}
	}
	
	private void processEnemies(ECSScene scene) {
		long enemyCount = scene.getEntityManager().query(EnemyArchetype.class).count();
		
		scene.getEntityManager().query(PlayerArchetype.class).findFirst().ifPresent(player -> {
			if (enemyCount < 10) {
				long enemiesToGenerate = 30 - enemyCount;
				while (enemiesToGenerate > 0) {
					generateEnemy(scene.getEntityManager(), player.transform);
					enemiesToGenerate--;
				}
			}
			
			scene.getEntityManager().query(EnemyArchetype.class).forEach(enemy -> {
				enemy.transform.orientation().identity().rotateTo(new Vector3f(0, 0, -1), enemy.transform.position().sub(player.transform.position(), new Vector3f()));
				
				float distanceFromPlayer = enemy.transform.position().distance(player.transform.position());
				var distanceVector = enemy.transform.position().sub(player.transform.position(), new Vector3f());
				distanceVector.mul(1 - (distanceFromPlayer / 50));
				enemy.transform.position().add(distanceVector);
			});
		});
	}
	
	private void generateEnemy(EntityManager manager, TransformComponent playerTransform) {
		var modelCache = manager.getAddon(ModelCache.class).orElseGet(ModelCache.registerModelCache(manager));
		if (!modelCache.contains(ENEMY_MODEL_NAME)) {
			var material = new TexturedMaterial(TextureCache.getInstance().getTexture("AGDG Logo.png"));
			Mesh enemyMesh = new ParShapesBuilder().createCube().translate(-0.5f, -0.5f, -0.5f).build();
			modelCache.putModel(ENEMY_MODEL_NAME, new Model(enemyMesh, material));
		}
		
		float enemySpawnDistance = 50;
		
		var enemyTransform = new TransformComponent(
				new Vector3f((float) ((Math.random() * 2) - 1), (float) ((Math.random() * 2) - 1), (float) ((Math.random() * 2) - 1)).mul(enemySpawnDistance).add(playerTransform.position()),
				new Vector3f(1f));
		enemyTransform.orientation().lookAlong(enemyTransform.position().sub(playerTransform.position(), new Vector3f()), new Vector3f(0, 1, 0));
		var model = new ModelComponent(ENEMY_MODEL_NAME);
		manager.createEntityWith(enemyTransform, model, new ShootableTag(), new EnemyTag());
	}

}
