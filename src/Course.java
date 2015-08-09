import java.util.ArrayList;


public abstract class Course {
	
	ArrayList<LineSegment> lines;
	
	Vector ballStartPos;
	
	Vector goalPos;
	
	Course() {
		lines = new ArrayList<LineSegment>();
	}
	
	abstract void createCourseWalls();
	abstract void setBallStartPosition();
	abstract void setGoalPosition();
	
}
