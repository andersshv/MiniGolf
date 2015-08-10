import java.util.ArrayList;

public class Course {
	
	ArrayList<LineSegment> lines = new ArrayList<LineSegment>();;	
	Vector ballStartPos;
	Vector goalPos;
	
	Course() {
		// Walls
		lines.add(new LineSegment(10, 10, 490, 10));
		lines.add(new LineSegment(10, 10, 10, 250));
		lines.add(new LineSegment(10, 250, 200, 250));
		lines.add(new LineSegment(490, 10, 490, 490));
		lines.add(new LineSegment(200, 250, 200, 490));
		lines.add(new LineSegment(200, 490, 490, 490));
		
		lines.add(new LineSegment(290, 100, 490, 300)); // Skew
		lines.add(new LineSegment(290, 200, 490, 200)); // Horizontal 
		lines.add(new LineSegment(385, 200, 490, 200)); // Horizontal corner hit 1
		lines.add(new LineSegment(285, 200, 355, 200)); // Horizontal corner hit 2
		lines.add(new LineSegment(375, 10, 375, 300)); // Horizontal corner hit 2
		
		lines.add(new LineSegment(280, 300, 282, 300)); // Horizontal corner hit 2
		
		
		// Ball start position
		ballStartPos = new Vector(350,350);
		
		// Goal position
		goalPos = new Vector(100,100);
	}
}
