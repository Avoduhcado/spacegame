package com.avogine.game.entity.systems;

import java.lang.Math;
import java.util.Set;

import org.joml.*;

import com.avogine.ecs.EntityComponent;
import com.avogine.ecs.components.TransformComponent;
import com.avogine.game.Game;
import com.avogine.game.entity.components.*;
import com.avogine.game.scene.*;
import com.avogine.game.util.*;
import com.avogine.util.MathUtil;

/**
 *
 */
public class CameraSystem implements Updateable {

	private static float FOV = 90f;
	private static float NEAR_PLANE = 0.1f;
	private static float FAR_PLANE = 15000.0f;
	
	// TODO Investigate a way to query for singular tagged entities, would require some enforcement during insertion
	private static final Set<Class<? extends EntityComponent>> cameraArchetype = Set.of(TransformComponent.class, CameraTag.class);
	private static final Set<Class<? extends EntityComponent>> focusArchetype = Set.of(TransformComponent.class, PhysicsComponent.class, PlayerTag.class);
		
	private float aspectRatio;
	
	private final Vector3f cameraTarget;
	private final Vector3f cameraPosition;
	private final Vector3f cameraUp;
	
	private final Quaternionf cameraOrientation;
	private final Quaternionf focusOrientation;
	private final Quaternionf orientationDifference;
	
	private float slerpTime;
	private float zoomBlend;
	
	/**
	 * 
	 */
	public CameraSystem() {
		cameraTarget = new Vector3f();
		cameraPosition = new Vector3f();
		cameraUp = new Vector3f();
		
		cameraOrientation = new Quaternionf();
		focusOrientation = new Quaternionf();
		orientationDifference = new Quaternionf();
	}
	
	@Override
	public void onRegister(Game game) {
		this.aspectRatio = game.getWindow().getAspectRatio();
		game.getCurrentScene().getProjection().perspective((float) Math.toRadians(FOV), aspectRatio, NEAR_PLANE, FAR_PLANE);
		cameraPosition.set(0, 1f, 7.5f);
		cameraTarget.set(0, 0, -1);
		cameraUp.set(0, 1, 0);
		game.getCurrentScene().getView().lookAt(cameraPosition, cameraTarget, cameraUp);
	}

	@Override
	public void onUpdate(GameState gameState) {
		if (gameState.scene() instanceof ECSScene scene) {
			scene.getEntityManager().query(cameraArchetype).findFirst().ifPresent(cameraChunk -> {
				scene.getEntityManager().query(focusArchetype).findFirst().ifPresent(focusChunk -> {
					updateCameraView(cameraChunk.getAs(TransformComponent.class, 0), focusChunk.getAs(TransformComponent.class, 0), focusChunk.getAs(PhysicsComponent.class, 0), gameState);
				});
			});
		}
	}
	
	private void updateCameraView(TransformComponent cameraTransform, TransformComponent focusTransform, PhysicsComponent focusPhysics, GameState gameState) {
		float delta = gameState.delta();
		Scene scene = gameState.scene();
		focusTransform.orientation(focusOrientation);
		cameraTransform.orientation(cameraOrientation);
		cameraTarget.set(0, 0, -1).rotate(focusOrientation);
		cameraPosition.set(0, 1f, 7.5f);
		
		// Set up camera target ahead of spaceship and reposition to focus' position
		cameraTarget.add(focusTransform.x(), focusTransform.y(), focusTransform.z());

		// Slerp the target's orientation to match the spaceship's if the camera angle exceeds 30 degrees.
		cameraOrientation.difference(focusOrientation, orientationDifference);
		if (orientationDifference.angle() > Math.toRadians(5)) {
			slerpTime = MathUtil.clamp(slerpTime + delta, 0, 1.5f);
		} else if (orientationDifference.angle() < Math.toRadians(1)) {
			slerpTime = MathUtil.clamp(slerpTime - delta, 0, 1.5f);
		}
		cameraOrientation.slerp(focusOrientation, slerpTime);
		cameraTransform.setOrientation(cameraOrientation);

		// Narrow the FOV when moving faster
		if (focusPhysics.getVelocity().length() > 0) {
			zoomBlend = MathUtil.clamp(zoomBlend + delta, 0, MathUtil.clamp(focusPhysics.getVelocity().length() / 50, 0, 1));
		} else {
			zoomBlend = MathUtil.clamp(zoomBlend - delta, 0, zoomBlend);
		}
		float fovAdjust = MathUtil.lerp(0, 50, zoomBlend);
		scene.getProjection().setPerspective((float) Math.toRadians(FOV + fovAdjust), aspectRatio, NEAR_PLANE, FAR_PLANE);

		// Position the camera to look at the target position slightly behind the spaceship
		cameraOrientation.transform(cameraPosition).add(cameraTarget);
		cameraTransform.setPosition(cameraPosition);
		scene.getView().setLookAt(cameraPosition, cameraTarget, focusOrientation.transformPositiveY(cameraUp));

		// Apply random screen shake when moving fast
		if (fovAdjust > 10) {
			float jitter = (float) ((Math.random() * 5) - 2.5f);
			scene.getView().rotateLocalZ((float) Math.toRadians(jitter * (fovAdjust / 50)));
		}
	}

}
