public class Vector {
	
	float x;
	float y;
	
	Vector(float x, float y) {
		this.x = x;
		this.y = y;
	}
	
	Vector plus(Vector v) {
		return new Vector(x + v.x, y + v.y);
	}
	
	Vector minus(Vector v) {
		return new Vector(x - v.x, y - v.y);
	}
	
	Vector times(float f) {
		return new Vector(x * f, y * f);
	}
	
	Vector dividedBy(float f) {
		return new Vector(x / f, y / f);
	}
	
	float length() {
		return (float) Math.sqrt(x*x + y*y);
	}	
	
	float cross(Vector v) {
		return x*v.y - y*v.x;
	}
	
	float dot(Vector v) {
		return x*v.x + y*v.y;
	}
	
	double dot(Vector v, Vector w) {
		return v.x*w.x + v.y*w.y;
	}
	
	double smallAngle(Vector v, Vector w) {
		double input = dot(v,w) / (w.length()*v.length());
		if (input > 1.0 || input < -1.0)
			return Math.PI;
		return Math.acos(input);
	}
	
	@Override
	public String toString() {
		return "x: " + x + ", y: " + y;
	}

}
