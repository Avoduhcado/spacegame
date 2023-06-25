package com.avogine.game.entity.components;

import org.joml.primitives.AABBf;

import com.avogine.ecs.EntityComponent;

/**
 * @param aabb 
 * @param collisionFlag 
 *
 */
public record ColliderComponent(AABBf aabb, int collisionFlag) implements EntityComponent {
	
	public static final int 
		PLAYER	= 1 << 1,
		ENEMY	= 1 << 2;

}
