package com.avogine.game.controllers;

import static java.lang.Math.toRadians;

import org.joml.*;
import org.lwjgl.glfw.GLFW;

import com.avogine.audio.data.*;
import com.avogine.audio.loader.AudioCache;
import com.avogine.ecs.*;
import com.avogine.ecs.components.*;
import com.avogine.game.Game;
import com.avogine.game.entity.components.*;
import com.avogine.game.scene.ECSScene;
import com.avogine.game.util.*;
import com.avogine.io.event.MouseClickEvent;
import com.avogine.io.listener.MouseClickListener;
import com.avogine.render.data.material.TexturedMaterial;
import com.avogine.render.data.mesh.*;
import com.avogine.render.loader.parshapes.ParShapesBuilder;
import com.avogine.render.loader.texture.TextureCache;
import com.avogine.util.MathUtil;

/**
 * 
 */
public class SpaceshipShootController implements MouseClickListener, Updateable {

	private final TransformComponent spaceshipTransform;
	private final PhysicsComponent spaceshipPhysics;
	private final AudioComponent spaceshipAudio;
	private static final String BULLET_MODEL_NAME = "bullet";
	
	private final Quaternionf spaceshipOrientation;
	
	private final Vector3f bulletPosition;
	
	private boolean shot;
	private float shootCooldown;
	
	private AudioBuffer laserBuffer;
	
	/**
	 * @param spaceshipTransform 
	 * @param spaceshipPhysics 
	 * @param spaceshipAudio 
	 * 
	 */
	public SpaceshipShootController(TransformComponent spaceshipTransform, PhysicsComponent spaceshipPhysics, AudioComponent spaceshipAudio) {
		this.spaceshipTransform = spaceshipTransform;
		this.spaceshipPhysics = spaceshipPhysics;
		this.spaceshipAudio = spaceshipAudio;
		this.shootCooldown = 1.0f / 12.0f;
		
		spaceshipOrientation = new Quaternionf();
		bulletPosition = new Vector3f();
	}
	
	@Override
	public void mouseClicked(MouseClickEvent event) {
		if (event.button() == GLFW.GLFW_MOUSE_BUTTON_1 && shootCooldown == 0) {
			shot = true;
		}
	}
	
	@Override
	public void onRegister(Game game) {
		// TODO Introduce some sort of window cache that's available for the game to query during instances such as registering.
		game.getWindow().getInput().add(this);
		
		var material = new TexturedMaterial(TextureCache.getInstance().getTexture("laser.png"));
		Mesh bulletMesh = new ParShapesBuilder().createCapsule(1f, 2.5f).scale(0.2f, 0.2f, 0.2f).rotate((float) toRadians(90), new float[] {1, 0, 0}).build();
		Models.CACHE.putModel(BULLET_MODEL_NAME, new Model(bulletMesh, material));
		
		laserBuffer = AudioCache.getInstance().getSound("blaster-3.ogg");
	}

	@Override
	public void onUpdate(GameState gameState) {
		shootCooldown = MathUtil.clamp(shootCooldown - gameState.delta(), 0, shootCooldown);
		
		if (shot) {
			shootCooldown = 1.0f / 12.0f;
			
			if (gameState.scene() instanceof ECSScene scene) {
				buildBullets(scene.getEntityManager());
			}
			var audioSource = new AudioSource(false, false);
			audioSource.setGain(1f);
			audioSource.play(laserBuffer);
			spaceshipAudio.sources().add(audioSource);
			
			shot = false;
		}
	}
	
	private void buildBullets(EntityManager manager) {
		spaceshipTransform.orientation(spaceshipOrientation);
		
		bulletPosition.set(-0.5f, 0, -1f).rotate(spaceshipOrientation).add(spaceshipTransform.x(), spaceshipTransform.y(), spaceshipTransform.z());
		var bulletTransform = new TransformComponent(bulletPosition.x, bulletPosition.y, bulletPosition.z,
				spaceshipOrientation.x, spaceshipOrientation.y, spaceshipOrientation.z, spaceshipOrientation.w);
		// TODO Bullet speed should be relative to spaceship speed, or else if you're moving too fast bullets will trail behind you
		var bulletPhysics = new PhysicsComponent(new Vector3f(0, 0, -200f).add(spaceshipPhysics.getVelocity()), new Vector3f(), 200f, 0, 0);
		var model = new ModelComponent(BULLET_MODEL_NAME);
		var projectile = new ProjectileComponent(3.5f);
		manager.createEntityWith(bulletTransform, bulletPhysics, model, projectile);

		bulletPosition.set(0.5f, 0, -1f).rotate(spaceshipOrientation).add(spaceshipTransform.x(), spaceshipTransform.y(), spaceshipTransform.z());
		bulletTransform = new TransformComponent(bulletPosition.x, bulletPosition.y, bulletPosition.z,
				spaceshipOrientation.x, spaceshipOrientation.y, spaceshipOrientation.z, spaceshipOrientation.w);
		model = new ModelComponent(BULLET_MODEL_NAME);
		manager.createEntityWith(bulletTransform, new PhysicsComponent(bulletPhysics), model, new ProjectileComponent(projectile));
	}

	/**
	 * @return the shootCooldown
	 */
	public float getShootCooldown() {
		return shootCooldown;
	}
	
	/**
	 * @param shootCooldown the shootCooldown to set
	 */
	public void setShootCooldown(float shootCooldown) {
		this.shootCooldown = shootCooldown;
	}

}
