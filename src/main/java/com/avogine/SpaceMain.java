package com.avogine;

import com.avogine.game.SpaceGame;
import com.avogine.io.Window;

/**
 *
 */
public class SpaceMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		var avogine = new Avogine(new Window("Space Game"), new SpaceGame());
		avogine.start();
	}

}
