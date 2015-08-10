public class Ball { 

	float width = 40;
	Vector pos;
	Vector speed;	
	boolean roling = false;

	Ball(float x, float y) {
		pos = new Vector(x,y);
		speed = new Vector(0,0);
	}	

}