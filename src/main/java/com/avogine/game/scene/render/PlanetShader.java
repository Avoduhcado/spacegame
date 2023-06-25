package com.avogine.game.scene.render;

import com.avogine.render.shader.ShaderProgram;
import com.avogine.render.shader.uniform.*;

/**
 *
 */
public class PlanetShader extends ShaderProgram {

	public UniformMat4 projection = new UniformMat4();
	public UniformMat4 view = new UniformMat4();
	public UniformMat4 model = new UniformMat4();
	
	public UniformMat3 normalMatrix = new UniformMat3();
	
	public UniformSampler Texture0 = new UniformSampler();
	
	public PlanetShader(String vertexShader, String fragmentShader) {
		super(vertexShader, fragmentShader);
		storeAllUniformLocations(projection, view, model, normalMatrix, Texture0);
		loadTexUnit();
	}
	
	private void loadTexUnit() {
		bind();
		Texture0.loadTexUnit(0);
		unbind();
	}

}
