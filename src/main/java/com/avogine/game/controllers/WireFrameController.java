package com.avogine.game.controllers;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import com.avogine.io.event.KeyboardEvent;
import com.avogine.io.listener.KeyboardListener;

/**
 *
 */
public class WireFrameController implements KeyboardListener {

	private boolean renderWireframe;
	
	@Override
	public void keyPressed(KeyboardEvent event) {
	}

	@Override
	public void keyReleased(KeyboardEvent event) {
	}

	@Override
	public void keyTyped(KeyboardEvent event) {
		if (event.key() == GLFW.GLFW_KEY_F3) {
			renderWireframe = !renderWireframe;
			if (renderWireframe) {
				GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
			} else {
				GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
			}
		}
	}

}
