public class Club {	
	
	Ball ball;
	Vector mouseClick;
	
	int power = 4;
	
	Vector startClub;
	Vector endClub;
	Vector clubArrowLeft;
	Vector clubArrowRight;

	Vector ballCenterToClickUnit;
	
	boolean isMouseClicked;
	
	Club(Ball ball) {
		this.ball = ball;
		mouseClick = new Vector(ball.pos.x + ball.width / 2, ball.pos.y + ball.width * 2);
		calcParams();
		isMouseClicked = true;
	}
	
	void mouseClicked(Vector clickPos) {
		mouseClick = clickPos;
		calcParams();
		isMouseClicked = true;
	}
	
	void powerPlus() {
		if(power < 10) {
			power++;
			calcParams();
		}
	}
	
	void powerMinus() {
		if(power > 1) {
			power--;
			calcParams();
		}
	}
	
	void calcParams() {
		Vector ballCenter = new Vector(ball.pos.x + ball.width/2, ball.pos.y + ball.width/2);
		Vector ballCenterToClick = mouseClick.minus(ballCenter);
		ballCenterToClickUnit = ballCenterToClick.dividedBy(ballCenterToClick.length());

		startClub = ballCenter.plus(ballCenterToClickUnit.times(ball.width));
		endClub = ballCenter.plus(ballCenterToClickUnit.times(ball.width + 10 * power));

		Vector aUnitLeftFromStartClub = new Vector(-ballCenterToClickUnit.y, ballCenterToClickUnit.x);
		Vector aUnitRightFromStartClub = new Vector(ballCenterToClickUnit.y, -ballCenterToClickUnit.x);

		clubArrowLeft = startClub.plus(aUnitLeftFromStartClub.times(10)).plus(ballCenterToClickUnit.times(10));
		clubArrowRight = startClub.plus(aUnitRightFromStartClub.times(10)).plus(ballCenterToClickUnit.times(10));		
	}
	
	
	
}
