package com.avogine.game.entity.systems;

import java.util.UUID;

import org.joml.*;

import com.avogine.ecs.*;
import com.avogine.ecs.addons.ModelCache;
import com.avogine.ecs.components.TransformComponent;
import com.avogine.game.Game;
import com.avogine.game.entity.components.CubemapModelComponent;
import com.avogine.game.scene.ECSScene;
import com.avogine.game.scene.render.PlanetShader;
import com.avogine.game.util.*;

/**
 *
 */
public class PlanetRenderSystem extends EntitySystem implements Renderable {

	private record PlanetArchetype(UUID id, TransformComponent transform, CubemapModelComponent model) implements EntityArchetype {};
	
	private PlanetShader planetShader;
	
	@Override
	public void onRegister(Game game) {
		planetShader = new PlanetShader("planetVertex.glsl", "planetFragment.glsl");
	}

	@Override
	public void onRender(SceneState sceneState) {
		if (sceneState.scene() instanceof ECSScene scene) {
			render(scene);
		}
	}
	
	private void render(ECSScene scene) {
		planetShader.bind();

		var modelCache = scene.getEntityManager().getAddon(ModelCache.class)
				.orElseGet(ModelCache.registerModelCache(scene.getEntityManager()));
		
		planetShader.projection.loadMatrix(scene.getProjection());
		planetShader.view.loadMatrix(scene.getView());
		
		var projView = new Matrix4f();
		scene.getProjection().mul(scene.getView(), projView);
		
		scene.getEntityManager().query(PlanetArchetype.class).forEach(planet -> {
			var modelMat = new Matrix4f();
			modelMat.translationRotateScale(
					planet.transform.position().x, planet.transform.position().y, planet.transform.position().z,
					planet.transform.orientation().x, planet.transform.orientation().y, planet.transform.orientation().z, planet.transform.orientation().w,
					planet.transform.scale().x, planet.transform.scale().y, planet.transform.scale().z);
			planetShader.model.loadMatrix(modelMat);
			var normalMatrix = new Matrix3f();
//			scene.getView().mul(modelMat, new Matrix4f()).get3x3(normalMatrix);
			// These aren't actually being used but this should be a proper normalmatrix calculation
			modelMat.get3x3(normalMatrix);
			normalMatrix.invert().transpose();
			planetShader.normalMatrix.loadMatrix(normalMatrix);
			
			modelCache.getModel(planet.model.model(), "").render();
		});
		
		planetShader.unbind();
	}

}
