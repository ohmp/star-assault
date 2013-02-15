package net.obviam.starassault.controller;

import java.util.HashMap;
import java.util.Map;

import net.obviam.starassault.model.Block;
import net.obviam.starassault.model.Bob;
import net.obviam.starassault.model.Bob.State;
import net.obviam.starassault.model.World;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;

public class WorldController {

	enum Keys {
		LEFT, RIGHT, JUMP, FIRE
	}

	private static final long LONG_JUMP_PRESS 	= 100l;
	private static final float X_ACCELERATION 	= 4f;
	private static final float Y_ACCELERATION 	= 6f;
	private static final float GRAVITY 			= -20f;
	private static final float MAX_JUMP_SPEED		= 8f;
	
	private World 	world;
	private Bob 	bob;
	private long	jumpPressedTime;
	private boolean jumpingPressed;
	private float	xAcceleration = 0f;
	private float	yAcceleration = 0f;
	private Vector2 acceleration = new Vector2();
	
	private boolean collidedLeft	= false;
	private boolean collidedRight	= false;
	private boolean collidedBottom	= false;
	private boolean collidedTop		= false;
	
	
	static Map<Keys, Boolean> keys = new HashMap<WorldController.Keys, Boolean>();
	static {
		keys.put(Keys.LEFT, false);
		keys.put(Keys.RIGHT, false);
		keys.put(Keys.JUMP, false);
		keys.put(Keys.FIRE, false);
	};

	public WorldController(World world) {
		this.world = world;
		this.bob = world.getBob();
	}

	// ** Key presses and touches **************** //
	
	public void leftPressed() {
		keys.get(keys.put(Keys.LEFT, true));
	}
	
	public void rightPressed() {
		keys.get(keys.put(Keys.RIGHT, true));
	}
	
	public void jumpPressed() {
		keys.get(keys.put(Keys.JUMP, true));
	}
	
	public void firePressed() {
		keys.get(keys.put(Keys.FIRE, false));
	}
	
	public void leftReleased() {
		keys.get(keys.put(Keys.LEFT, false));
	}
	
	public void rightReleased() {
		keys.get(keys.put(Keys.RIGHT, false));
	}
	
	public void jumpReleased() {
		keys.get(keys.put(Keys.JUMP, false));
		jumpingPressed = false;
	}
	
	public void fireReleased() {
		keys.get(keys.put(Keys.FIRE, false));
	}
	
	/** The main update method **/
	public void update(float delta) {
		processInput();
		if (bob.getState().equals(State.JUMPING) && !jumpingPressed) {
			bob.getAcceleration().add(0, GRAVITY);
		} else if (jumpingPressed) {
			bob.getVelocity().y = MAX_JUMP_SPEED; 
		}
		if (collidedLeft && bob.getVelocity().x < 0) {
			bob.getVelocity().x = 0f;
		}
		if (collidedRight && bob.getVelocity().x > 0) {
			bob.getVelocity().x = 0f;
		}
		if (collidedBottom && bob.getVelocity().y < 0) {
			bob.getVelocity().y = 0f;
		}
		if (collidedTop && bob.getVelocity().y > 0) {
			bob.getVelocity().y = 0f;
		}
		bob.getVelocity().y += delta * GRAVITY;
		bob.update(delta);
		if (bob.getPosition().y < 0) {
			bob.getPosition().y = 0f;
			bob.setPosition(bob.getPosition());
			bob.setState(State.IDLE);
		}
		// check collision with blocks
		for (Block block : world.getBlocks() ) {
			if (Intersector.intersectRectangles(bob.getBounds(), block.getBounds())) {
				collideBob(block);
				if (bob.getState().equals(State.JUMPING)) {
				}
			}
		}
	}

	private void collideBob(Block block) {
		if (bob.getVelocity().x < 0) {
			collidedLeft = true;
			collidedRight = false;
		} else if (bob.getVelocity().x > 0) {
			collidedLeft = false;
			collidedRight = true;
		}
		if (bob.getVelocity().y > 0 && bob.getState().equals(State.JUMPING)) {
			jumpingPressed = false;
			collidedTop = true;
			collidedBottom = false;
		}
		if (bob.getVelocity().y < 0 && bob.getState().equals(State.JUMPING)) {
			bob.setState(State.IDLE);
			jumpingPressed = false;
			collidedBottom = true;
			collidedTop = false;
		}
//		bob.setState(State.IDLE);
//		bob.getVelocity().y = 0;
//		jumping = false;
	}

	/** Change Bob's state and parameters based on input controls **/
	private boolean processInput() {
		if (keys.get(Keys.JUMP)) {
			if (!bob.getState().equals(State.JUMPING)) {
				jumpingPressed = true;
				jumpPressedTime = System.currentTimeMillis();
				bob.setState(State.JUMPING);
				bob.getVelocity().y = MAX_JUMP_SPEED; 
			} else {
				if (jumpingPressed && ((System.currentTimeMillis() - jumpPressedTime) >= LONG_JUMP_PRESS)) {
					jumpingPressed = false;
				}
			}
		}
		if (keys.get(Keys.LEFT)) {
			// left is pressed
			bob.setFacingLeft(true);
			if (!bob.getState().equals(State.JUMPING)) {
				bob.setState(State.WALKING);
			}
			bob.getVelocity().x = -Bob.SPEED;
		}
		if (keys.get(Keys.RIGHT)) {
			// left is pressed
			bob.setFacingLeft(false);
			if (!bob.getState().equals(State.JUMPING)) {
				bob.setState(State.WALKING);
			}
			bob.getVelocity().x = Bob.SPEED;
		}
		// need to check if both or none direction are pressed, then Bob is idle
		if ((keys.get(Keys.LEFT) && keys.get(Keys.RIGHT)) ||
				(!keys.get(Keys.LEFT) && !(keys.get(Keys.RIGHT))) && !bob.getState().equals(State.JUMPING)) {
			if (!bob.getState().equals(State.JUMPING)) {
				bob.setState(State.IDLE);
			}
			// acceleration is 0 on the x
			bob.getAcceleration().x = 0;
			// horizontal speed is 0
			bob.getVelocity().x = 0;
		}
		return false;
	}

}
