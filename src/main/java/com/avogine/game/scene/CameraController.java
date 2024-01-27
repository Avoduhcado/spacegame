package com.avogine.game.scene;

import java.lang.Math;
import java.nio.DoubleBuffer;

import org.joml.*;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;

import com.avogine.entity.Transform;
import com.avogine.game.Game;
import com.avogine.game.util.*;
import com.avogine.io.event.*;
import com.avogine.io.listener.*;
import com.avogine.util.MathUtil;

/**
 *
 */
public class CameraController implements KeyboardListener, MouseMotionListener, MouseClickListener, Updateable {
	
	private static float FOV = 90f;
	private static float NEAR_PLANE = 0.1f;
	private static float FAR_PLANE = 15000.0f;
	
	private float aspectRatio;
	private final Matrix4f projectionMatrix;
	private final Matrix4f cameraViewMatrix;

	private float slerpTime;
	
	private float lastX;
	private float lastY;
	
	private Vector3f cameraVelocity;
	
	private final Vector3f inputDirection;
	
	private Transform focusTransform;
	
	/**
	 * @param projection 
	 * @param view 
	 * 
	 */
	public CameraController(Matrix4f projection, Matrix4f view) {
		projectionMatrix = projection;
		cameraViewMatrix = view;
		
		cameraVelocity = new Vector3f();
		focusTransform = new Transform();
		focusTransform.rotation().lookAlong(new Vector3f(0, 0, 1), new Vector3f(0, 1, 0));
		inputDirection = new Vector3f(0);
	}
	
	@Override
	public void onRegister(Game game) {
		this.aspectRatio = game.getWindow().getAspectRatio();
		projectionMatrix.perspective((float) Math.toRadians(FOV), aspectRatio, NEAR_PLANE, FAR_PLANE);
		cameraViewMatrix.lookAlong(new Vector3f(0, 0, -1), new Vector3f(0, 1, 0));
		
		try (MemoryStack stack = MemoryStack.stackPush()) {
			DoubleBuffer xPos = stack.mallocDouble(1);
			DoubleBuffer yPos = stack.mallocDouble(1);

			GLFW.glfwGetCursorPos(game.getWindow().getId(), xPos, yPos);
			lastX = (float) xPos.get();
			lastY = (float) yPos.get();
		}
		
		game.getWindow().getInput().add(this);
	}
	
	@Override
	public void mouseClicked(MouseClickEvent event) {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			DoubleBuffer xPos = stack.mallocDouble(1);
			DoubleBuffer yPos = stack.mallocDouble(1);

			GLFW.glfwGetCursorPos(event.window(), xPos, yPos);
			lastX = (float) xPos.get();
			lastY = (float) yPos.get();
		}
	}

	@Override
	public void mouseMoved(MouseMotionEvent event) {
		if (GLFW.glfwGetInputMode(event.window(), GLFW.GLFW_CURSOR) != GLFW.GLFW_CURSOR_DISABLED) {
			return;
		}
		
		float xOffset = lastX - event.xPosition();
		float yOffset = lastY - event.yPosition(); // reversed since y-coordinates go from bottom to top
		lastX = event.xPosition();
		lastY = event.yPosition();
		
		// TODO Make these customizable options
		float sensitivity = 0.065f;
		boolean xInverted = false;
		boolean yInverted = true;
		xOffset *= sensitivity;
		yOffset *= sensitivity;

		var mouseDelta = new Vector2f(xInverted ? -xOffset : xOffset, yInverted ? -yOffset : yOffset).mul(sensitivity);
		Quaternionf rotation = focusTransform.rotation();
		Quaternionf horizontal = new Quaternionf(new AxisAngle4f(mouseDelta.x, new Vector3f(0, 1, 0)));
		Quaternionf vertical = new Quaternionf(new AxisAngle4f(mouseDelta.y, new Vector3f(1, 0, 0)));
		focusTransform.rotation().set(horizontal.mul(rotation).mul(vertical));
//		characterPhysics.getRotationalVelocity().y += xInverted ? -xOffset : xOffset;
//		characterPhysics.getRotationalVelocity().x += yInverted ? -yOffset : yOffset;
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		switch (event.key()) {
		case GLFW.GLFW_KEY_W -> inputDirection.z += 1f;
		case GLFW.GLFW_KEY_S -> inputDirection.z += -1f;
		case GLFW.GLFW_KEY_A -> inputDirection.x += 1f;
		case GLFW.GLFW_KEY_D -> inputDirection.x += -1f;
//		case GLFW.GLFW_KEY_Q -> characterPhysics.getRotationalVelocity().z += 15f;
//		case GLFW.GLFW_KEY_E -> characterPhysics.getRotationalVelocity().z += -15f;
//		case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT -> characterPhysics.setBoost(true);
//		case GLFW.GLFW_KEY_SPACE -> {
//			if (turboBoost || characterPhysics.getVelocity().length() > characterPhysics.getMaxSpeed()) {
//				return;
//			}
//			characterPhysics.getVelocity().add(0, 0, -1000);
//			turboBoost = true;
//		}
		}
	}

	@Override
	public void keyReleased(KeyboardEvent event) {
//		switch (event.key()) {
//		case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT -> characterPhysics.setBoost(false);
//		case GLFW.GLFW_KEY_SPACE -> turboBoost = false;
//		}
	}

	@Override
	public void keyTyped(KeyboardEvent event) {
	}
	
	@Override
	public void onUpdate(GameState gameState) {
		float delta = gameState.delta();

		if (inputDirection.length() > 0) {
			inputDirection.normalize();
		}
		cameraVelocity.add(focusTransform.rotation().transform(inputDirection));
		inputDirection.zero();
		
		cameraVelocity.lerp(new Vector3f(), 5f * delta);
		focusTransform.translation().add(cameraVelocity.mul(delta, new Vector3f()));
		
		var target = new Vector3f(0, 0, 1);
		focusTransform.rotation().transform(target);
		target.add(focusTransform.translation());
		cameraViewMatrix.identity().lookAt(focusTransform.translation(), target, new Vector3f(0, 1, 0));
	}
	
	private void updateCameraView(Transform cameraTransform, float delta) {
		// Set up camera target ahead of spaceship
//		Vector3f cameraTarget = focusTransform.getRotation().transform(new Vector3f(0, 0, -1f));
//		cameraTarget.add(focusTransform.getTranslation());
//		cameraTransform.getTranslation().set(cameraTarget.x, cameraTarget.y, cameraTarget.z);

		// Slerp the target's orientation to match the spaceship's if the camera angle exceeds 30 degrees.
		var diffQuat = cameraTransform.rotation().difference(focusTransform.rotation(), new Quaternionf());
		if (diffQuat.angle() > Math.toRadians(5)) {
			slerpTime = MathUtil.clamp(slerpTime + delta, 0, 1.5f);
		} else if (diffQuat.angle() < Math.toRadians(1)) {
			slerpTime = MathUtil.clamp(slerpTime - delta, 0, 1.5f);
		}
		cameraTransform.rotation().slerp(focusTransform.rotation(), slerpTime);

		// Narrow the FOV when moving faster
//		if (focusPhysics.getVelocity().length() > 0) {
//			zoomBlend = MathUtil.clamp(zoomBlend + delta, 0, MathUtil.clamp(focusPhysics.getVelocity().length() / 50, 0, 1));
//		} else {
//			zoomBlend = MathUtil.clamp(zoomBlend - delta, 0, zoomBlend);
//		}
//		float fovAdjust = MathUtil.lerp(0, 50, zoomBlend);
//		projectionMatrix.setPerspective((float) Math.toRadians(FOV + fovAdjust), aspectRatio, NEAR_PLANE, FAR_PLANE);

		// Position the camera to look at the target position slightly behind the spaceship
		var cameraPosition = cameraTransform.rotation().transform(new Vector3f(0, 1f, 7.5f));
		cameraPosition.add(cameraTransform.translation());
		cameraViewMatrix.identity().lookAlong(focusTransform.translation(), new Vector3f(0, 1, 0));
//		cameraViewMatrix.identity().lookAt(cameraPosition, cameraTarget, focusTransform.getRotation().transformPositiveY(new Vector3f()));

		// Apply random screen shake when moving fast
//		if (fovAdjust > 10) {
//			float jitter = (float) ((Math.random() * 5) - 2.5f);
//			cameraViewMatrix.rotateLocalZ((float) Math.toRadians(jitter * (fovAdjust / 50)));
//		}
	}

}
