
public class Rocket {
	
	
	private int radius;
	private double angle;
	private int direction;
	private double speed;
	private int health;
	

	//Variables used to determine the hitbox.
	public double radius1;
	public double radius2;
	public double radius3;
	public double radius4;
	public double angle1;
	public double angle2;
	public double angle3;
	public double angle4;

	
	public Rocket(int r, int a, double s, int d) {
		radius = r;
		angle = a;
		speed = s;
		direction = d;
		health = 100;
		
		radius1 = Math.hypot(getX() + 15 * Game.ROCKETSIZE / 100, getY() - Game.ROCKETSIZE / 2);
		radius2 = Math.hypot(getX() - 2 * Game.ROCKETSIZE / 10, getY() - Game.ROCKETSIZE / 2);
		radius3 = Math.hypot(getX() - 2 * Game.ROCKETSIZE / 10, getY() + Game.ROCKETSIZE / 2);
		radius4 = Math.hypot(getX() + 15 * Game.ROCKETSIZE / 100, getY() + Game.ROCKETSIZE / 2);
		
		angle1 = Math.atan2((getY() - Game.ROCKETSIZE / 2) , (getX() + 15 * Game.ROCKETSIZE / 100));
		angle2 = Math.atan2((getY() - Game.ROCKETSIZE / 2) , (getX() - 2 * Game.ROCKETSIZE / 10));
		angle3 = Math.atan2((getY() + Game.ROCKETSIZE / 2) , (getX() + 15 * Game.ROCKETSIZE / 100));
		angle4 = Math.atan2((getY() + Game.ROCKETSIZE / 2) , (getX() + 15 * Game.ROCKETSIZE / 100));
	}
	
	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public int getX() {
		return (int) (radius * Math.cos(Math.toRadians(angle)));
	}
	
	public int getY() {
		return (int) (radius * Math.sin(Math.toRadians(angle)));
	}

	public void move() {
		angle -= direction * speed;
	}

	public double getAngle() {
		return angle;
	}
	
	public void changeDirection() {
		direction *= -1;
	}
	
	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public void takeDamage(int damage) {
		health -= damage;
	}
}
