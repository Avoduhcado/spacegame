package com.avogine.game.scene.render;

import com.avogine.render.shader.ShaderProgram;
import com.avogine.render.shader.uniform.*;

/**
 *
 */
public class StarmapShader extends ShaderProgram {

	public UniformMat4 projectionView = new UniformMat4();
	public UniformSampler starmap = new UniformSampler();
	
	/**
	 * @param vertexShader 
	 * @param fragmentShader 
	 */
	public StarmapShader(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader);
		storeAllUniformLocations(projectionView, starmap);
		loadTexUnit();
	}
	
	private void loadTexUnit() {
		bind();
		starmap.loadTexUnit(0);
		unbind();
	}
	
}
