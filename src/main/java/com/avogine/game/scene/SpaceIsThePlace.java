package com.avogine.game.scene;

import static org.lwjgl.opengl.GL11.*;

import java.util.*;

import com.avogine.entity.Entity;
import com.avogine.game.Game;
import com.avogine.game.controllers.*;
import com.avogine.game.entity.systems.StarmapRender;
import com.avogine.game.scene.controllers.FreeCam;
import com.avogine.io.Window;
import com.avogine.render.SceneRender;
import com.avogine.render.data.experimental.AMaterial;
import com.avogine.render.loader.assimp.ModelLoader;
import com.avogine.render.loader.parshapes.ParShapesBuilder;
import com.avogine.render.loader.parshapes.experimental.AMeshBuilder;

/**
 *
 */
public class SpaceIsThePlace extends Scene {
	
	@Override
	public void init(Game game, Window window) {
		game.register(new SceneRender());
		game.register(new StarmapRender());
		game.register(new FreeCam(projection, view));
		
		game.addInputListener(new CursorController(window.getId()));
		game.addInputListener(new WireFrameController());
		
		String backModelId = "backpack";
		var model = ModelLoader.load(backModelId, "backpack.obj");
		
//		getModelMap().put(backModelId, model);
		
		Random rando = new Random();
//		for (int i = 0; i < 1500; i++) {
//			var entity = new Entity(UUID.randomUUID().toString(), backModelId);
//			entity.getTransform().translation().add(rando.nextFloat(-100, 100), rando.nextFloat(-100, 100), rando.nextFloat(-100, 100));
//			entity.getTransform().rotation().rotateAxis((float) Math.toRadians(rando.nextInt(360)), rando.nextFloat(1), rando.nextFloat(1), rando.nextFloat(1));
//			entity.getTransform().scale().set(rando.nextFloat(0.5f, 2f));
//			entity.getTransform().updateModelMatrix();
//			model.getEntities().add(entity);
//		}
		
		var cubeMaterial = new AMaterial();
		var parShapesBuilder = new ParShapesBuilder();
		var cubeMesh = parShapesBuilder.createSphere(6, 6).build(new AMeshBuilder());
		cubeMaterial.setDiffuseTexturePath("cube.png");
		cubeMaterial.getMeshes().add(cubeMesh);
		
		String cubeModelId = "cube";
//		var cubeModel = new AModel(cubeModelId, List.of(cubeMaterial));
//		var cubeModel = ModelLoader.load(cubeModelId, "cube.obj");
		var cubeModel = ModelLoader.load(cubeModelId, "kuromiball.obj");
		getModelMap().put(cubeModelId, cubeModel);
		
		for (int i = 0; i < 1500; i++) {
			var entity = new Entity(UUID.randomUUID().toString(), cubeModelId);
			entity.getTransform().translation().add(rando.nextFloat(-1000, 1000), rando.nextFloat(-1000, 1000), rando.nextFloat(-1000, 1000));
			entity.getTransform().rotation().rotateAxis((float) Math.toRadians(rando.nextInt(360)), rando.nextFloat(1), rando.nextFloat(1), rando.nextFloat(1));
			entity.getTransform().scale().set(rando.nextFloat(0.5f, 2f));
			entity.getTransform().updateModelMatrix();
			cubeModel.getEntities().add(entity);
		}
	}

	@Override
	public void prepareRender() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}

}
