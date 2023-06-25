package com.avogine.game.scene;

import static org.lwjgl.opengl.GL11.*;

import com.avogine.game.Game;
import com.avogine.game.ui.nuklear.TitleMenu;
import com.avogine.io.Window;

/**
 *
 */
public class TitleScene extends Scene {

	@Override
	public void init(Game game, Window window) {
		game.register(new TitleMenu(game, SpaceScene::new));
	}

	@Override
	public void prepareRender() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	}

}
