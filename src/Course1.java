public class Course1 extends Course {	
	
	Course1() {
		super();
		createCourseWalls();
		setBallStartPosition();
		setGoalPosition();
	}

	@Override
	void createCourseWalls() {
		lines.add(new LineSegment(10, 10, 490, 10));
		lines.add(new LineSegment(10, 10, 10, 250));
		lines.add(new LineSegment(10, 250, 200, 250));
		lines.add(new LineSegment(490, 10, 490, 490));
		lines.add(new LineSegment(200, 250, 200, 490));
		lines.add(new LineSegment(200, 490, 490, 490));
		
//		lines.add(new LineSegment(290, 100, 490, 300)); // Skew
//		lines.add(new LineSegment(290, 200, 490, 200)); // Horizontal
	}

	@Override
	void setBallStartPosition() {
		ballStartPos = new Vector(350,350);		
	}

	@Override
	void setGoalPosition() {
		goalPos = new Vector(100,100);		
	}

}
