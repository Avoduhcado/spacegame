package com.avogine.game.entity.systems;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL14.GL_DEPTH_COMPONENT24;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.*;

import org.joml.Matrix4f;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryUtil;

import com.avogine.game.Game;
import com.avogine.game.scene.Scene;
import com.avogine.game.scene.render.*;
import com.avogine.game.util.*;
import com.avogine.render.data.Cubemap;
import com.avogine.render.data.mesh.Mesh;
import com.avogine.render.loader.parshapes.ParShapesLoader;

/**
 *
 */
public class StarmapRender implements Renderable, Cleanupable {
	
	private StarmapShader starmapShader;
	
	private Cubemap starCubeMap;
	private Mesh starCube;
	
	private final Matrix4f projectionView;
	private final Matrix4f noTranslationView;
	
	/**
	 * 
	 */
	public StarmapRender() {
		projectionView = new Matrix4f();
		noTranslationView = new Matrix4f();
	}
	
	@Override
	public void onRegister(Game game) {
		SpaceShader spaceShader = new SpaceShader("spaceVertex.glsl", "spaceGeometry.glsl", "spaceFragment.glsl");
		starmapShader = new StarmapShader("starmapVertex.glsl", "starmapFragment.glsl");
		
		float starHalf = 100;
		float starDistance = starHalf * 2;
		//			float planetHalf = (float) Math.sqrt(Math.pow(FAR_PLANE, 2) / 2);
		//			float planetDistance = planetHalf * 2;
		int starsToRender = 1500;

		int starVao = glGenVertexArrays();
		int vbo = glGenBuffers();

		glBindVertexArray(starVao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);

		FloatBuffer vertexData = MemoryUtil.memAllocFloat(starsToRender * 3);

		for (int i = 0; i < starsToRender; i++) {
			// Load up random XYZ coordinate for each star
			vertexData.put((float) ((Math.random() * starDistance) - starHalf));
			vertexData.put((float) ((Math.random() * starDistance) - starHalf));
			vertexData.put((float) ((Math.random() * starDistance) - starHalf));
		}

		vertexData.flip();
		glBufferData(GL_ARRAY_BUFFER, vertexData, GL_STREAM_DRAW);
		MemoryUtil.memFree(vertexData);

		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

		starCubeMap = new Cubemap(glGenTextures());
		starCubeMap.bind();
		int cubemapSize = 4096; 
		for (int i = 0; i < 6; i++) {
			glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGBA8, cubemapSize, cubemapSize, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
		}
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_WRAP_R, GL12.GL_CLAMP_TO_EDGE);

		Matrix4f projection = new Matrix4f().perspective(90f, 1f, 0.1f, starDistance);
		Matrix4f view = new Matrix4f();

		int fbo = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, fbo);
		glDrawBuffer(GL_COLOR_ATTACHMENT0);

		int depthBuffer = glGenRenderbuffers();
		glBindRenderbuffer(GL_RENDERBUFFER, depthBuffer);
		glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT24, cubemapSize, cubemapSize);
		glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, depthBuffer);

		glViewport(0, 0, cubemapSize, cubemapSize);

		spaceShader.bind();
		glDepthFunc(GL_LEQUAL);
		glDisable(GL_CULL_FACE);
		spaceShader.projection.loadMatrix(projection);
		for (int i = 0; i < 6; i++) {
			glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, starCubeMap.getId(), 0);

			orientView(view, i);
			spaceShader.view.loadMatrix(game.getCurrentScene().getView());

			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			glDrawArrays(GL_POINTS, 0, starsToRender);
		}

		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glBindVertexArray(0);
		spaceShader.unbind();
		// TODO Wrap this in some Render/Window call so it can poll properties
		glEnable(GL_CULL_FACE);
		glDepthFunc(GL_LESS);
		spaceShader.cleanup();

		glViewport(0, 0, game.getWindow().getWidth(), game.getWindow().getHeight());

		glDeleteRenderbuffers(depthBuffer);
		glDeleteFramebuffers(fbo);
		glDeleteBuffers(starVao);

		starCube = ParShapesLoader.loadCubemap();
	}
	
	private void orientView(Matrix4f view, int direction) {
		view.identity().rotateZ((float) Math.toRadians(180));
		switch (direction) {
			case 0 -> view.rotateY((float) Math.toRadians(90));
			case 1 -> view.rotateY((float) Math.toRadians(-90));
			case 2 -> view.rotateX((float) Math.toRadians(-90)).rotateY((float) Math.toRadians(180));
			case 3 -> view.rotateX((float) Math.toRadians(90)).rotateY((float) Math.toRadians(180));
			case 4 -> view.rotateY((float) Math.toRadians(180));
		}
	}
	
	@Override
	public void onRender(SceneState sceneState) {
		renderStarmap(sceneState.scene());
	}
	
	private void renderStarmap(Scene scene) {
		starmapShader.bind();
		glCullFace(GL_FRONT);
		glDepthFunc(GL_LEQUAL);
		
		noTranslationView.set3x3(scene.getView());
		projectionView.set(scene.getProjection()).mul3x3(noTranslationView.m00(), noTranslationView.m01(), noTranslationView.m02(), 
				noTranslationView.m10(), noTranslationView.m11(), noTranslationView.m12(), 
				noTranslationView.m20(), noTranslationView.m21(), noTranslationView.m22());
		starmapShader.projectionView.loadMatrix(projectionView);
		
		starCubeMap.bind();
		starCube.render();
		
		starmapShader.unbind();
		glDepthFunc(GL_LESS);
		glCullFace(GL_BACK);
	}
	
	@Override
	public void onCleanup() {
		starmapShader.cleanup();
		
		starCubeMap.cleanup();
		starCube.cleanup();
	}
	
}
