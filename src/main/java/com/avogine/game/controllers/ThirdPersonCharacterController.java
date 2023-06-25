package com.avogine.game.controllers;

import java.nio.DoubleBuffer;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;

import com.avogine.game.Game;
import com.avogine.game.entity.components.PhysicsComponent;
import com.avogine.game.util.*;
import com.avogine.io.event.*;
import com.avogine.io.listener.*;

/**
 *
 */
public class ThirdPersonCharacterController implements KeyboardListener, MouseMotionListener, MouseClickListener, Updateable {

	private PhysicsComponent characterPhysics;
	
	private float lastX;
	private float lastY;
	
	private boolean turboBoost;
	
	private final Vector3f inputDirection;
	
	/**
	 * @param characterPhysics
	 */
	public ThirdPersonCharacterController(PhysicsComponent characterPhysics) {
		this.characterPhysics = characterPhysics;
		this.inputDirection = new Vector3f();
		
		
	}

	@Override
	public void onRegister(Game game) {
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
		float sensitivity = 0.65f;
		boolean xInverted = false;
		boolean yInverted = false;
		xOffset *= sensitivity;
		yOffset *= sensitivity;

		characterPhysics.getRotationalVelocity().y += xInverted ? -xOffset : xOffset;
		characterPhysics.getRotationalVelocity().x += yInverted ? -yOffset : yOffset;
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		switch (event.key()) {
		case GLFW.GLFW_KEY_W -> inputDirection.z += -1f;
		case GLFW.GLFW_KEY_S -> inputDirection.z += 1f;
		case GLFW.GLFW_KEY_A -> inputDirection.x += -1f;
		case GLFW.GLFW_KEY_D -> inputDirection.x += 1f;
		case GLFW.GLFW_KEY_Q -> characterPhysics.getRotationalVelocity().z += 15f;
		case GLFW.GLFW_KEY_E -> characterPhysics.getRotationalVelocity().z += -15f;
		case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT -> characterPhysics.setBoost(true);
		case GLFW.GLFW_KEY_SPACE -> {
			if (turboBoost || characterPhysics.getVelocity().length() > characterPhysics.getMaxSpeed()) {
				return;
			}
			characterPhysics.getVelocity().add(0, 0, -1000);
			turboBoost = true;
		}
		}
	}

	@Override
	public void keyReleased(KeyboardEvent event) {
		switch (event.key()) {
		case GLFW.GLFW_KEY_LEFT_SHIFT, GLFW.GLFW_KEY_RIGHT_SHIFT -> characterPhysics.setBoost(false);
		case GLFW.GLFW_KEY_SPACE -> turboBoost = false;
		}
	}

	@Override
	public void keyTyped(KeyboardEvent event) {
	}
	
	@Override
	public void onUpdate(GameState gameState) {
		inputDirection.normalize();
		if (inputDirection.isFinite()) {
			characterPhysics.getImpulse().set(inputDirection);
		}
		inputDirection.zero();
	}

}
