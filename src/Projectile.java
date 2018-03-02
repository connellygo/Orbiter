
public class Projectile {
	private double speed;
	private int angle;
	private double radius;
	
	public Projectile(double d, int a) {
		speed = d;
		angle = a;
		radius = 0;
	}
	
	public void move() {
		radius += speed;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public int getAngle() {
		return angle;
	}

	public void setAngle(int angle) {
		this.angle = angle;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}
	
	public int getX() {
		return (int) (Math.cos(Math.toRadians(angle)) * radius);
	}
	
	public int getY() {
		return (int) (Math.sin(Math.toRadians(angle)) * radius);
	}
}
