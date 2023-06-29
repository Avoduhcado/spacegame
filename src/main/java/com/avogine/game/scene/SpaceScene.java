package com.avogine.game.scene;

import static org.lwjgl.opengl.GL11.*;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Random;

import org.joml.*;

import com.avogine.ecs.Models;
import com.avogine.ecs.components.*;
import com.avogine.ecs.system.*;
import com.avogine.game.Game;
import com.avogine.game.controllers.*;
import com.avogine.game.entity.components.*;
import com.avogine.game.entity.systems.*;
import com.avogine.game.ui.nuklear.*;
import com.avogine.io.Window;
import com.avogine.render.data.material.TexturedMaterial;
import com.avogine.render.data.mesh.Model;
import com.avogine.render.loader.parshapes.ParShapesBuilder;
import com.avogine.render.loader.texture.TextureCache;

/**
 * 
 */
public class SpaceScene extends ECSScene {
	
	/**
	 * 
	 */
	public SpaceScene() {
		super(new Matrix4f(), new Matrix4f());
	}
	
	@Override
	public void init(Game game, Window window) {
		Spaceship spaceship = loadPlayer();
		loadPlanet();
		game.playAudio("Burning_Heat_DDR2.ogg", true);
		
		var cameraTransform = new TransformComponent();
		entityManager.createEntityWith(cameraTransform, new CameraTag());
		
		game.register(new CameraSystem());
		game.register(new ThirdPersonCharacterController(spaceship.physics));
		game.register(new SpaceshipShootController(spaceship.transform, spaceship.physics, spaceship.audio));
		
		game.register(new RenderSystem());
		game.register(new PhysicsSystem());
		game.register(new ProjectileSystem());
		game.register(new SpaceRenderSystem());
		game.register(new PlanetRenderSystem());
		game.register(new EnemySystem());
		game.register(new AudioSystem());
		game.register(new GameMenu(game, TitleScene::new));
		game.register(new DebugInfo());
		
		game.addInputListener(new CursorController(window.getId()));
		game.addInputListener(new WireFrameController());
//		game.addInputListener(new MenuController(game));
	}

	@Override
	public void prepareRender() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}
	
	private void loadPlanet() {
//		var agdgMaterial = new TexturedMaterial(TextureCache.getInstance().getTexture("AGDG Logo.png"));
//		var planetMaterial = new TexturedMaterial(TextureCache.getInstance().getTexture("mars.jpg"));
//		var gasGiantMaterial = new TexturedMaterial(TextureCache.getInstance().getCubemap("tan-gas-giant-textures", "png"));
		var gasGiantMaterial = new TexturedMaterial(TextureCache.getInstance().getCubemap("kuromi", "png"));
		
//		var planetMesh = new ParShapesBuilder().createSphere(64, 16).rotate(180, new float[] {1, 0, 0}).build();
		var planetMesh = new ParShapesBuilder().createSphere(4).build();
		var planetModel = new Model(planetMesh, gasGiantMaterial);
		Models.CACHE.putModel("planet", planetModel);
		var planetPosition = new Vector3f(1000, 1200, -3500);
		var axisAngle = new AxisAngle4f((float) Math.toRadians(-100), 1, 0, 0);
		var planetOrientation = new Quaternionf(axisAngle);
		var planetTransform = new TransformComponent(planetPosition.x, planetPosition.y, planetPosition.z, 
				planetOrientation.x, planetOrientation.y, planetOrientation.z, planetOrientation.w,
				500);
		var planetPhysics = new PhysicsComponent(new Vector3f(), new Vector3f(0, 0, 12.5f), 0, 100, 0);
		entityManager.createEntityWith(planetTransform, new CubemapModelComponent("planet"), planetPhysics);
		
		float asteroidHalf = 1000;
		float asteroidDistance = asteroidHalf * 2;
//		var asteroidMaterial = new Material(new Vector3f(71f / 255f, 68f / 255f, 57f / 255f), null, 1);
		var asteroidMaterial = new TexturedMaterial(TextureCache.getInstance().getTexture("asteroid.jpg"));

		int uniqueAsteroidCount = 5;
		var asteroidRandom = new Random();
		for (int i = 0; i < uniqueAsteroidCount; i++) {
			var asteroidMesh = new ParShapesBuilder().createRock(asteroidRandom.nextInt(), 2).build();
			var asteroidModel = new Model(asteroidMesh, asteroidMaterial);
			Models.CACHE.putModel("rock" + i, asteroidModel);
		}
		
		var asteroidPosition = new Vector3f();
		var asteroidRotation = new AxisAngle4f();
		var asteroidOrientation = new Quaternionf();
		for (int i = 0; i < 1000; i++) {
			var asteroidPhysics = new PhysicsComponent(new Vector3f(), new Vector3f(0, (float) (1.5f + (Math.random() * 4f)), 0), 0, 0, 0);
			asteroidPosition.set((float) ((Math.random() * asteroidDistance) - asteroidHalf), (float) ((Math.random() * asteroidDistance) - asteroidHalf), (float) ((Math.random() * asteroidDistance) - asteroidHalf));
			asteroidRotation.set((float) Math.toRadians(Math.random() * 360), new Vector3f((float) Math.random(), (float) Math.random(), (float) Math.random()));
			asteroidOrientation.set(asteroidRotation);
			var asteroidTransform = new TransformComponent(
					asteroidPosition.x, asteroidPosition.y, asteroidPosition.z,
					asteroidOrientation.x, asteroidOrientation.y, asteroidOrientation.z, asteroidOrientation.w);
			entityManager.createEntityWith(asteroidTransform, new ModelComponent("rock" + (int) (Math.random() * uniqueAsteroidCount)), asteroidPhysics);
		}
	}
	
	private Spaceship loadPlayer() {
		var material = new TexturedMaterial(TextureCache.getInstance().getTexture("AGDG Logo.png"));

		var spaceshipTransform = new TransformComponent();
		var spaceshipPhysics = new PhysicsComponent();
		var squareMesh = new ParShapesBuilder().createCube().translate(-0.5f, -0.5f, -0.5f).build();
		var squareModel = new Model(squareMesh, material);
		Models.CACHE.putModel("spaceship", squareModel);
		var spaceshipModel = new ModelComponent("spaceship");
		var spaceshipAudio = new AudioComponent(new ArrayList<>());
		entityManager.createEntityWith(spaceshipTransform, spaceshipModel, spaceshipPhysics, new PlayerTag(), new AudioListenerTag(), spaceshipAudio);
		
		return new Spaceship(spaceshipPhysics, spaceshipTransform, spaceshipAudio);
	}
	
	private record Spaceship(PhysicsComponent physics, TransformComponent transform, AudioComponent audio) {
		
	}
	
}
