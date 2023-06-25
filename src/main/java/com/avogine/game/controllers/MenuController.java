package com.avogine.game.controllers;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.glfw.GLFW;

import com.avogine.game.Game;
import com.avogine.game.scene.TitleScene;
import com.avogine.io.event.KeyboardEvent;
import com.avogine.io.listener.KeyboardListener;

/**
 *
 */
public class MenuController implements KeyboardListener {

	private Game game;
	
	/**
	 * @param game 
	 * 
	 */
	public MenuController(Game game) {
		this.game = game;
	}
	
	@Override
	public void keyPressed(KeyboardEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyboardEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyboardEvent event) {
		if (event.key() == GLFW.GLFW_KEY_TAB) {
			glfwSetInputMode(game.getWindow().getId(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
			game.queueSceneSwap(new TitleScene());
		}
	}

}
