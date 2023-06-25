package com.avogine.game.controllers;

import static org.lwjgl.glfw.GLFW.*;

import com.avogine.io.event.*;
import com.avogine.io.listener.*;

/**
 *
 */
public class CursorController implements MouseClickListener, KeyboardListener {

	/**
	 * @param windowId The ID of the Window to control the cursor of.
	 * 
	 */
	public CursorController(long windowId) {
		glfwSetInputMode(windowId, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
	}
	
	@Override
	public void mouseClicked(MouseClickEvent event) {
		if (event.button() == GLFW_MOUSE_BUTTON_1) {
			glfwSetInputMode(event.window(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
		}
	}

	@Override
	public void keyPressed(KeyboardEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyboardEvent event) {
		if (event.key() == GLFW_KEY_LEFT_ALT || event.key() == GLFW_KEY_RIGHT_ALT) {
			glfwSetInputMode(event.window(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
		}
	}

	@Override
	public void keyTyped(KeyboardEvent event) {
		// TODO Auto-generated method stub
		
	}

}
