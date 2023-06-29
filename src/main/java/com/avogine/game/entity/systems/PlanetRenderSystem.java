package com.avogine.game.entity.systems;

import org.joml.*;

import com.avogine.ecs.Models;
import com.avogine.ecs.components.TransformComponent;
import com.avogine.game.Game;
import com.avogine.game.entity.components.CubemapModelComponent;
import com.avogine.game.scene.ECSScene;
import com.avogine.game.scene.render.PlanetShader;
import com.avogine.game.util.*;

/**
 *
 */
public class PlanetRenderSystem implements Renderable {
	
	private final Matrix4f modelMatrix;
	private final Matrix3f normalMatrix;
	
	private PlanetShader planetShader;
	
	/**
	 * 
	 */
	public PlanetRenderSystem() {
		modelMatrix = new Matrix4f();
		normalMatrix = new Matrix3f();
	}
	
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

		planetShader.projection.loadMatrix(scene.getProjection());
		planetShader.view.loadMatrix(scene.getView());
		
		var projView = new Matrix4f();
		scene.getProjection().mul(scene.getView(), projView);
		
		scene.getEntityManager().query(TransformComponent.class, CubemapModelComponent.class).forEach(chunk -> {
			for (int i = 0; i < chunk.getChunkSize(); i++) {
				var transform = chunk.getAs(TransformComponent.class, i);
				var model = chunk.getAs(CubemapModelComponent.class, i);
				modelMatrix.identity().translationRotateScale(
						transform.x(), transform.y(), transform.z(),
						transform.rx(), transform.ry(), transform.rz(), transform.rw(),
						transform.sx(), transform.sy(), transform.sz());
				planetShader.model.loadMatrix(modelMatrix);
				//			scene.getView().mul(modelMat, new Matrix4f()).get3x3(normalMatrix);
				// These aren't actually being used but this should be a proper normalmatrix calculation
				modelMatrix.get3x3(normalMatrix);
				normalMatrix.invert().transpose();
				planetShader.normalMatrix.loadMatrix(normalMatrix);

				Models.CACHE.getModel(model.model(), "").render();
			}
		});
		
		planetShader.unbind();
	}

}
