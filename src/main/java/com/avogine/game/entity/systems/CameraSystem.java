package com.avogine.game.entity.systems;

import java.lang.Math;
import java.util.UUID;

import org.joml.*;

import com.avogine.ecs.*;
import com.avogine.ecs.components.TransformComponent;
import com.avogine.game.Game;
import com.avogine.game.entity.components.*;
import com.avogine.game.scene.ECSScene;
import com.avogine.game.util.*;
import com.avogine.util.MathUtil;

/**
 *
 */
public class CameraSystem extends EntitySystem implements Updateable {

	private static float FOV = 90f;
	private static float NEAR_PLANE = 0.1f;
	private static float FAR_PLANE = 15000.0f;
	
	private float aspectRatio;
	private final Matrix4f projectionMatrix;
	private final Matrix4f cameraViewMatrix;
	
	private static record CameraArchetype(UUID id, TransformComponent transform, CameraTag tag) implements EntityArchetype {}
	private static record FocusArchetype(UUID id, TransformComponent transform, PhysicsComponent physics, PlayerTag tag) implements EntityArchetype {}
	
	private float slerpTime;
	private float zoomBlend;
	
	/**
	 * @param projection 
	 * @param view 
	 * 
	 */
	public CameraSystem(Matrix4f projection, Matrix4f view) {
		projectionMatrix = projection;
		cameraViewMatrix = view;
	}
	
	@Override
	public void onRegister(Game game) {
		this.aspectRatio = game.getWindow().getAspectRatio();
		projectionMatrix.perspective((float) Math.toRadians(FOV), aspectRatio, NEAR_PLANE, FAR_PLANE);
		cameraViewMatrix.lookAlong(new Vector3f(0, 0, -1), new Vector3f(0, 1, 0));
	}

	@Override
	public void onUpdate(GameState gameState) {
		if (gameState.scene() instanceof ECSScene scene) {
			scene.getEntityManager().query(CameraArchetype.class).findFirst().ifPresent(camera -> {
				scene.getEntityManager().query(FocusArchetype.class).findFirst().ifPresent(focus -> {
					updateCameraView(camera.transform, focus.transform, focus.physics, gameState.delta());
				});
			});
		}
	}
	
	private void updateCameraView(TransformComponent cameraTransform, TransformComponent focusTransform, PhysicsComponent focusPhysics, float delta) {
		// Set up camera target ahead of spaceship
		var cameraTarget = focusTransform.orientation().transform(new Vector3f(0, 0, -1f));
		cameraTarget.add(focusTransform.position());
		cameraTransform.setPosition(cameraTarget.x, cameraTarget.y, cameraTarget.z);

		// Slerp the target's orientation to match the spaceship's if the camera angle exceeds 30 degrees.
		var diffQuat = cameraTransform.orientation().difference(focusTransform.orientation(), new Quaternionf());
		if (diffQuat.angle() > Math.toRadians(5)) {
			slerpTime = MathUtil.clamp(slerpTime + delta, 0, 1.5f);
		} else if (diffQuat.angle() < Math.toRadians(1)) {
			slerpTime = MathUtil.clamp(slerpTime - delta, 0, 1.5f);
		}
		cameraTransform.orientation().slerp(focusTransform.orientation(), slerpTime);

		// Narrow the FOV when moving faster
		if (focusPhysics.getVelocity().length() > 0) {
			zoomBlend = MathUtil.clamp(zoomBlend + delta, 0, MathUtil.clamp(focusPhysics.getVelocity().length() / 50, 0, 1));
		} else {
			zoomBlend = MathUtil.clamp(zoomBlend - delta, 0, zoomBlend);
		}
		float fovAdjust = MathUtil.lerp(0, 50, zoomBlend);
		projectionMatrix.setPerspective((float) Math.toRadians(FOV + fovAdjust), aspectRatio, NEAR_PLANE, FAR_PLANE);

		// Position the camera to look at the target position slightly behind the spaceship
		var cameraPosition = cameraTransform.orientation().transform(new Vector3f(0, 1f, 7.5f));
		cameraPosition.add(cameraTransform.position());
		cameraViewMatrix.identity().lookAt(cameraPosition, cameraTarget, focusTransform.orientation().transformPositiveY(new Vector3f()));

		// Apply random screen shake when moving fast
		if (fovAdjust > 10) {
			float jitter = (float) ((Math.random() * 5) - 2.5f);
			cameraViewMatrix.rotateLocalZ((float) Math.toRadians(jitter * (fovAdjust / 50)));
		}
	}

}
