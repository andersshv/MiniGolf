import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.Timer;


public class MiniGolf extends JPanel implements ActionListener, KeyListener, MouseListener {

	private Course course;
	private Ball ball;

	private Timer timer;

	private boolean courseDone = false;

	private Vector mouseClick;

	private Vector centerGoal;

	// Club Ball Params	
	private Vector ballCenterToClick;
	private Vector ballCenterToClickUnit;	
	private Vector startClub;
	private Vector endClub;
	private Vector ul;
	private Vector ur;
	private Vector ull;
	private Vector urr;
	
	// Testing
	private Vector hitLS1Moved;
	private Vector hitLS2Moved;	

	public MiniGolf() {
		addKeyListener(this);	
		addMouseListener(this);

		setBackground(Color.BLACK);
		setPreferredSize(new Dimension(500, 500));
		setFocusable(true);

		initCourse();	
		
		//Hej 
	}

	private void initCourse() {
		course = new Course1();
		ball = new Ball(course.ballStartPos.x, course.ballStartPos.y);	
		mouseClick = new Vector(ball.pos.x + ball.width / 2, ball.pos.y + 50); 
		centerGoal = new Vector(course.goalPos.x + ball.width / 2 + 3, course.goalPos.x + ball.width / 2 + 3);		
		calcClubBallParams();
		
		timer = new Timer(15, this);
		timer.start();
	}

	private void calcClubBallParams() {
		ballCenterToClick = mouseClick.minus(ball.center());
		ballCenterToClickUnit = ballCenterToClick.dividedBy(ballCenterToClick.length());

		startClub = ball.center().plus(ballCenterToClickUnit.times(ball.width));
		endClub = ball.center().plus(ballCenterToClickUnit.times(ball.width + 10 * ball.power));

		ul = new Vector(-ballCenterToClickUnit.y, ballCenterToClickUnit.x);
		ur = new Vector(ballCenterToClickUnit.y, -ballCenterToClickUnit.x);

		ull = startClub.plus(ul.times(10)).plus(ballCenterToClickUnit.times(10));
		urr = startClub.plus(ur.times(10)).plus(ballCenterToClickUnit.times(10));		
	}
	
	int xx = 0;
	int yy = 0;
	
	Vector oldPos;
	Vector newPos;

	@Override
	public void actionPerformed(ActionEvent e) {
		if(!courseDone) {			
			// Check For Goal Position			
			if(ball.center().minus(centerGoal).length() < 3) {
				courseDone = true;
			}

			// Move Ball
			oldPos = ball. pos;
			newPos = ball.pos.plus(ball.speed);
			ball.pos = newPos;

			// Check For Collision
			boolean collision = false;
			boolean cornerHit = false;
			boolean straightHit = false;
			LineSegment hitLS = null;
			for(LineSegment ls : course.lines) {
				Vector ls1 = new Vector(ls.x1, ls.y1);
				Vector ls2 = new Vector(ls.x2, ls.y2);

				Vector ls1ToBallCenter = ball.center().minus(ls1);
				Vector ls1Tols2 = ls2.minus(ls1);
				Vector ortLs1ToLs2 = new Vector(-ls1Tols2.y,ls1Tols2.x);
				float s1 = ls1ToBallCenter.cross(ortLs1ToLs2);
				float s2 = ls1Tols2.cross(ortLs1ToLs2);
				if(s1 * s2 <= 0) {
					// ball closest to endpoint ls1
					if(ls1ToBallCenter.length() < ball.width / 2) {
						collision = true;
						cornerHit = true;
						hitLS = ls;
						break;
					}
				} else {
					Vector ls2ToBallCenter = ball.center().minus(ls2);
					Vector ls2ToLs1 = ls1.minus(ls2);
					Vector ortLs2ToLs1 = new Vector(-ls2ToLs1.y, ls2ToLs1.x);
					float s3 = ls2ToBallCenter.cross(ortLs2ToLs1);
					float s4 = ls2ToLs1.cross(ortLs2ToLs1);
					if(s3 *s4 <= 0) {
						// Ball closest to endpoint ls2
						if(ls2ToBallCenter.length() < ball.width / 2) {
							collision = true;
							cornerHit = true;
							hitLS = ls;
							break;
						}
					} else {
						// Ball closest to some point on line segment
						Vector unitLs1ToLs2 = ls1Tols2.dividedBy(ls1Tols2.length());
						float s5 = ls1ToBallCenter.dot(unitLs1ToLs2);
						Vector ls1ToPointClosestToBallOnLine = unitLs1ToLs2.times(s5);
						Vector pointClosestToBallOnLine = ls1.plus(ls1ToPointClosestToBallOnLine);
						Vector ballToPointClosestToBallOnLine = ball.center().minus(pointClosestToBallOnLine);
						if(ballToPointClosestToBallOnLine.length() < ball.width / 2) {
							collision = true;
							straightHit = true;
							hitLS = ls;
							break;
						}						
					}
				}				
			}

			if(collision && straightHit) {
//				ball.pos = oldPos;
				
				Vector hitLS1 = new Vector(hitLS.x1, hitLS.y1);
				Vector hitLS2 = new Vector(hitLS.x2, hitLS.y2);
				Vector hitLS1ToLS2 = hitLS2.minus(hitLS1);
				Vector unitHitLS1ToLS2 = hitLS1ToLS2.dividedBy(hitLS1ToLS2.length());
				Vector hitLS1ToCenterBall = ball.center().minus(hitLS1);
				float f = unitHitLS1ToLS2.dot(hitLS1ToCenterBall);
				Vector projCenterBallOnHitLS = unitHitLS1ToLS2.times(f);
				Vector posProj = hitLS1.plus(projCenterBallOnHitLS);
				Vector centerBallToPosProj = posProj.minus(ball.center());
//				System.out.println(centerBallToPosProj.length());
				Vector posProjToCenterBall = centerBallToPosProj.times(-1.0f);
				Vector unitPosProjToCenterBall = posProjToCenterBall.dividedBy(posProjToCenterBall.length());
				Vector moveLineSegmentBy = unitPosProjToCenterBall.times(ball.width / 2);
				hitLS1Moved = hitLS1.plus(moveLineSegmentBy);
				hitLS2Moved = hitLS2.plus(moveLineSegmentBy);
				
				//a*x1 + b = y1
				//a*x2 + b = y2
				float x1 = oldPos.x;
				float y1 = oldPos.y;
				
				float x2 = newPos.x;
				float y2 = newPos.y;
				
				System.out.println(oldPos);
				System.out.println(newPos);
								
				//a = dy / dx = (y2 - y1) / (x2 - x1)
				float a = (y2 - y1) / (x2 - x1);
				
				//b = y1 - a*x1
				float b = y1 - a*x1;
				
				//Find intersection point				
				if(hitLS1Moved.x == hitLS2Moved.x) {
					//Wall is vertical
					float x = hitLS1Moved.x;
					float y = a*x + b;	
					xx = Math.round(x);
					yy = Math.round(y);
					
				} else if(hitLS1Moved.y == hitLS2Moved.y) {
					//Wall is horizontal
					float y = hitLS1Moved.y;
					float x = (y - b) / a;
					xx = Math.round(x);
					yy = Math.round(y);					
					
				} else {
					//Wall is skew					
				}				
				
				courseDone = true;
				timer.stop();				
			}

			if(!collision) {
				// Change Ball Speed
				if(ball.speed.length() > 0.1f) {
					ball.speed = ball.speed.times(0.98f);
				} else {
					ball.speed = new Vector(0,0);
					ball.roling = false; 
				}

				calcClubBallParams();
			}
			repaint();
		}
	}	

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);        
		g.setColor(Color.WHITE);  

		// Walls
		for(LineSegment l : course.lines) {
			g.drawLine(l.x1, l.y1, l.x2, l.y2);
		}

		// Goal
		g.drawOval(Math.round(course.goalPos.x), Math.round(course.goalPos.y), Math.round(ball.width + 6), Math.round(ball.width + 6));

		// Ball
		g.fillOval(Math.round(ball.pos.x), Math.round(ball.pos.y), Math.round(ball.width), Math.round(ball.width));

		// Club
		if(!ball.roling) {
			g.drawLine(Math.round(startClub.x), Math.round(startClub.y), Math.round(endClub.x), Math.round(endClub.y));
			g.drawLine(Math.round(startClub.x), Math.round(startClub.y), Math.round(ull.x), Math.round(ull.y));
			g.drawLine(Math.round(startClub.x), Math.round(startClub.y), Math.round(urr.x), Math.round(urr.y));			
		}

		//End Titles
		if(courseDone) {
			g.setColor(Color.YELLOW);
			Font font = new Font("Arial", Font.BOLD, 20);
			g.setFont(font);
			g.drawString("Done!", 220, 200);
			g.drawLine(Math.round(hitLS1Moved.x), Math.round(hitLS1Moved.y), Math.round(hitLS2Moved.x), Math.round(hitLS2Moved.y));
			g.setColor(Color.RED);
			g.fillOval(xx - 5, yy - 5, 10, 10);
			
			g.fillOval(Math.round(oldPos.x), Math.round(oldPos.y), 3, 3);
			g.fillOval(Math.round(newPos.x), Math.round(newPos.y), 3, 3);
		}

		Toolkit.getDefaultToolkit().sync();
	}

	@Override
	public void keyPressed(KeyEvent e) { 
		int kc = e.getKeyCode();
		if(kc == KeyEvent.VK_ESCAPE ) {
			System.exit(0);
		}
		if(kc == KeyEvent.VK_SPACE ) {
			courseDone = false;
			initCourse();
		}
		if(!ball.roling) {
			if(kc == KeyEvent.VK_UP)
				ball.powerPlus();
			if(kc == KeyEvent.VK_DOWN)
				ball.powerMinus();
			if(kc == KeyEvent.VK_ENTER) {
				ball.roling = true;
				ball.setSpeed(ballCenterToClickUnit);
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) { 
		mouseClick = new Vector(e.getX(), e.getY());		
	}

	@Override
	public void keyReleased(KeyEvent e) { }

	@Override
	public void keyTyped(KeyEvent e) { }

	@Override
	public void mousePressed(MouseEvent e) { }

	@Override
	public void mouseReleased(MouseEvent e) { }

	@Override
	public void mouseEntered(MouseEvent e) { }

	@Override
	public void mouseExited(MouseEvent e) { }

}
