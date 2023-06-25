import org.joml.Vector3f;

import com.avogine.util.VectorUtil;

/**
 *
 */
public class VelocityTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		var velocity = new Vector3f(15, -10, 0);
		var zeroVector = new Vector3f();
		
		//lerpToZero(velocity, zeroVector);
		clampToZero(velocity);
	}
	
	public static void clampToZero(Vector3f velocity) {
		int counter = 0;
		
		while (velocity.length() > 0 && counter < 100) {
			var deceleration = velocity.mul(0.1f, new Vector3f());
			
			System.out.println("Decel: \t" + vectorToString(deceleration));
			
			VectorUtil.clampDirection(velocity, deceleration);
			if (velocity.length() < 1f / 60f) {
				velocity.set(0);
			}
			System.out.println("Veloc: \t" + vectorToString(velocity));
			counter++;
		}
		System.out.println(counter);
	}
	
	public static void lerpToZero(Vector3f velocity, Vector3f zeroVector) {
		int counter = 0;
		
		while (velocity.length() > 0 && counter < 1000) {
			var lerped = zeroVector.lerp(velocity, 1f / 60f, new Vector3f());
			
			System.out.println("Lerped: \t" + vectorToString(lerped));
			
			velocity.sub(lerped);
			
			System.out.println("Velocity: \t" + vectorToString(velocity));
			counter++;
		}
		
		System.out.println(counter);
	}
	
	public static String vectorToString(Vector3f vector) {
		return vector.x + ", " + vector.y + ", " + vector.z + ": " + vector.length();
	}

}
