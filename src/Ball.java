public class Ball { 

	float width = 40;
	Vector pos;
	Vector speed;	
	boolean roling = false;
	
	int power = 4;

	Ball(float x, float y) {
		pos = new Vector(x,y);
		speed = new Vector(0,0);
	}
	
	void powerPlus() {
		if(power < 7)
			power++;
	}
	
	void powerMinus() {
		if(power > 1)
			power--;
	}

	void setSpeed(Vector ballCenterToClickUnit) {
		speed = ballCenterToClickUnit.times(-1.0f).times(power);		
	}
	
	Vector center() {
		return new Vector(pos.x + width/2, pos.y + width/2);
	}	

}