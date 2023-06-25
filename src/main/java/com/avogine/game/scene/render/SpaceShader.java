package com.avogine.game.scene.render;

import org.lwjgl.opengl.*;

import com.avogine.render.shader.*;
import com.avogine.render.shader.uniform.UniformMat4;

/**
 *
 */
public class SpaceShader extends ShaderProgram {
	
	public UniformMat4 projection = new UniformMat4();
	public UniformMat4 view = new UniformMat4();
	
	/**
	 * @param vertexShaderFile
	 * @param geometryShaderFile 
	 * @param fragmentShaderFile
	 */
	public SpaceShader(String vertexShaderFile, String geometryShaderFile, String fragmentShaderFile) {
		super(new ShaderFileType(vertexShaderFile, GL20.GL_VERTEX_SHADER), new ShaderFileType(geometryShaderFile, GL32.GL_GEOMETRY_SHADER), new ShaderFileType(fragmentShaderFile, GL20.GL_FRAGMENT_SHADER));
		storeAllUniformLocations(projection, view);
	}
	
	@Override
	public void bind() {
		super.bind();
		GL11.glEnable(GL33.GL_PROGRAM_POINT_SIZE);
	}
	
	@Override
	public void unbind() {
		super.unbind();
		GL11.glDisable(GL33.GL_PROGRAM_POINT_SIZE);
	}

}
