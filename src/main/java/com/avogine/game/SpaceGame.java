package com.avogine.game;

import com.avogine.game.scene.TitleScene;
import com.avogine.game.ui.nuklear.NuklearUI;
import com.avogine.io.*;

/**
 *
 */
public class SpaceGame extends Game {
	
	/**
	 * 
	 */
	public SpaceGame() {
		super();
		setScene(new TitleScene());
//		setScene(new SpaceScene());
	}
	
	@Override
	public void init(Window window, Audio audio, NuklearUI gui) {
		super.init(window, audio, gui);
		getCurrentScene().init(this, window);
	}
	
}
