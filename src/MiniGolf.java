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
	boolean testing = false;
	private Vector hitLS1Moved;
	private Vector hitLS2Moved;
	private Vector o_ip;
	private Vector o_opc;
	private Vector o_npc;

	public MiniGolf() {
		addKeyListener(this);	
		addMouseListener(this);

		setBackground(Color.BLACK);
		setPreferredSize(new Dimension(500, 500));
		setFocusable(true);

		initCourse();	
	}

	private void initCourse() {
		course = new Course1();
		ball = new Ball(course.ballStartPos.x, course.ballStartPos.y);	
		mouseClick = new Vector(ball.pos.x + ball.width / 2, ball.pos.y + 50); // Vertical start
		//mouseClick = new Vector(ball.pos.x - 50, ball.pos.y + ball.width / 2); // Horizontal start
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

	@Override
	public void actionPerformed(ActionEvent e) {
		if(!courseDone) {				

			// Define current position of ball and next position
			Vector o_oldPos = ball.pos;
			o_opc = o_oldPos.plus(new Vector(ball.width / 2, ball.width / 2)); // Origo To Old Pos Center
			Vector o_newPos = ball.pos.plus(ball.speed);
			o_npc = o_newPos.plus(new Vector(ball.width / 2, ball.width / 2)); // Origo To New Pos Center

			// Check For Collision
			boolean collision = false;
			boolean cornerHit = false;
			boolean straightHit = false;			

			LineSegment hitLS = null;
			for(LineSegment ls : course.lines) {
				Vector ls1 = new Vector(ls.x1, ls.y1);
				Vector ls2 = new Vector(ls.x2, ls.y2);

				Vector ls1ToBallCenter = o_npc.minus(ls1);
				Vector ls1Tols2 = ls2.minus(ls1);
				Vector ortLs1ToLs2 = new Vector(-ls1Tols2.y,ls1Tols2.x);
				float s1 = ls1ToBallCenter.cross(ortLs1ToLs2);
				float s2 = ls1Tols2.cross(ortLs1ToLs2);
				if(s1 * s2 <= 0) {
					// ball closest to endpoint ls1
					if(ls1ToBallCenter.length() < ball.width / 2) {
						if(!straightHit) {
							collision = true;
							cornerHit = true;
							hitLS = ls;
						}
					}
				} else {
					Vector ls2ToBallCenter = o_npc.minus(ls2);
					Vector ls2ToLs1 = ls1.minus(ls2);
					Vector ortLs2ToLs1 = new Vector(-ls2ToLs1.y, ls2ToLs1.x);
					float s3 = ls2ToBallCenter.cross(ortLs2ToLs1);
					float s4 = ls2ToLs1.cross(ortLs2ToLs1);
					if(s3 *s4 <= 0) {
						// Ball closest to endpoint ls2
						if(ls2ToBallCenter.length() < ball.width / 2) {
							if(!straightHit) {
								collision = true;
								cornerHit = true;
								hitLS = ls;
							}
						}
					} else {
						// Ball closest to some point on line segment
						Vector unitLs1ToLs2 = ls1Tols2.dividedBy(ls1Tols2.length());
						float s5 = ls1ToBallCenter.dot(unitLs1ToLs2);
						Vector ls1ToPointClosestToBallOnLine = unitLs1ToLs2.times(s5);
						Vector pointClosestToBallOnLine = ls1.plus(ls1ToPointClosestToBallOnLine);
						Vector ballToPointClosestToBallOnLine = o_npc.minus(pointClosestToBallOnLine);
						if(ballToPointClosestToBallOnLine.length() < ball.width / 2) {
							collision = true;
							straightHit = true;
							hitLS = ls;
							break;
						}						
					}
				}				
			}

			if(collision) {
				if(straightHit) {
					testing = true;
					/*** Find the ball's new position ***/
					// Vectors to the end points of the line segment that the ball has collided with
					Vector o_v1 = new Vector(hitLS.x1, hitLS.y1);
					Vector o_v2 = new Vector(hitLS.x2, hitLS.y2);

					// Projection of center of ball on colliding line segment after collision detection 
					// Here it is assumed that the center of the ball has not passed over the line segment
					// https://en.wikipedia.org/wiki/Vector_projection
					Vector v1_v2 = o_v2.minus(o_v1);						// b
					Vector v1_v2_UNIT = v1_v2.dividedBy(v1_v2.length());	// b/|b|
					Vector v1_npc = o_npc.minus(o_v1);						// a
					float f = v1_v2_UNIT.dot(v1_npc);						// a__1
					Vector v1_projNpcOnv1v2 = v1_v2_UNIT.times(f);			// a_1

					Vector o_projNpcOnv1v2 = o_v1.plus(v1_projNpcOnv1v2);

					// Vector from projection point to center of ball after collision
					Vector projNpcOnv1v2_npc = o_npc.minus(o_projNpcOnv1v2);	
					Vector projNpcOnv1v2_npc_UNIT = projNpcOnv1v2_npc.dividedBy(projNpcOnv1v2_npc.length());

					// Finding the correct new ball position, as if the collision has caused the ball to bounce of the wall
					float distNpcTov1v2Moved = ball.width / 2 - projNpcOnv1v2_npc.length();
					Vector npc_npcAfterCollision = projNpcOnv1v2_npc_UNIT.times(distNpcTov1v2Moved * 2);
					Vector o_npcAfterCollision = o_npc.plus(npc_npcAfterCollision);
					Vector cnpc = o_npcAfterCollision; 	// Correct new position center

					/*** Find the ball's new speed vector ***/
					// The direction of the speed vector is the vector that goes from the crossing of the moved collision line segment
					// and the line between opc og npc, to o_npcAfterCollision.
					// The length of this vector should be the same as the length of the prior speed vector (air friction is added below in the code)
					Vector moveLineSegmentBy = projNpcOnv1v2_npc_UNIT.times(ball.width / 2);
					hitLS1Moved = o_v1.plus(moveLineSegmentBy);
					hitLS2Moved = o_v2.plus(moveLineSegmentBy);

					//Calculating line between opc and npc
					//a*x1 + b = y1
					//a*x2 + b = y2
					float x1 = o_opc.x;
					float y1 = o_opc.y;

					float x2 = o_npc.x;
					float y2 = o_npc.y;

					float a = 0;
					float b = 0;
					String ballDir = "skew";
					if(x1 != x2 && y1 != y2) { 
						//Ball is moving skew
						//a = dy / dx = (y2 - y1) / (x2 - x1)
						a = (y2 - y1) / (x2 - x1);

						//b = y1 - a*x1
						b = y1 - a*x1;

						//Formula: y = a*x + b
					} 
					if(x1 != x2 && y1 == y2) {	
						//Ball is moving horizontal
						//Formula: y = y1
						ballDir = "hori";
					}
					if(x1 == x2 && y1 != y2){	
						//Ball is moving vertical
						//Formula: x = x1
						ballDir = "vert";
					}

					//Find intersection point, ip
					float x = 0;
					float y = 0;					
					if(hitLS1Moved.x == hitLS2Moved.x) {
						//Wall is vertical
						//Calculating intersection point
						if(ballDir.equals("skew")) {
							x = hitLS1Moved.x;
							y = a*x + b;	
						}
						if(ballDir.equals("hori")) {
							x = hitLS1Moved.x;
							y = y1;
						}
					} else if(hitLS1Moved.y == hitLS2Moved.y) {
						//Wall is horizontal
						//Calculating intersection point
						if(ballDir.equals("skew")) {
							y = hitLS1Moved.y;
							x = (y - b) / a;
						}
						if(ballDir.equals("vert")) {
							x = x1;
							y = hitLS1Moved.y;
						}
					} else {
						//Wall is skew
						//Calculating line for the hit line segment
						float x1l = hitLS1Moved.x;
						float y1l = hitLS1Moved.y;

						float x2l = hitLS2Moved.x;
						float y2l = hitLS2Moved.y;

						//al = dyl / dxl = (y2l - y1l) / (x2l - x1l)
						float al = (y2l - y1l) / (x2l - x1l);

						//bl = y1l - al*x1l
						float bl = y1l - al*x1l;

						//Formula: y = al*x + bl
						
						//Calculating intersection point, ip
						if(ballDir.equals("skew")) {							
							x = (bl - b) / (a - al);
							y = a*x + b;
						}
						if(ballDir.equals("vert")) {
							x = x1;
							y = al*x + bl;
						}

						if(ballDir.equals("hori")) {
							y = y1;
							x = (y - bl) / al;
						}
					}

					o_ip = new Vector(x, y);
					Vector ip_cnpc = cnpc.minus(o_ip);
//					System.out.println("opc: " + o_opc + ", npc: " + o_npc);
//					System.out.println("o_ip: " + o_ip + ", cnpc: " + cnpc);
					Vector ip_cnpc_UNIT = ip_cnpc.dividedBy(ip_cnpc.length());
					Vector newSpeedVector = ip_cnpc_UNIT.times(ball.speed.length());
					ball.speed = newSpeedVector;

					//courseDone = true;
					
				} else {
					// Hit corner with ball
				}
			} else {
				ball.pos = o_newPos;
			}

			// Check For Goal Position			
			if(o_npc.minus(centerGoal).length() < ball.width / 2 + 3) {
				ball.pos = course.goalPos.plus(new Vector(3, 3));
				courseDone = true;
				return;
			}

			// Change Ball Speed
			if(ball.speed.length() > 0.1f) {
				ball.speed = ball.speed.times(0.985f);
			} else {
				ball.speed = new Vector(0,0);
				ball.roling = false; 
			}

			calcClubBallParams();

		}

		repaint();
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
			g.drawString("Done!", 225, 220);
		}

		// Testing
//		if(testing) {
//			g.setColor(Color.RED);
//			g.drawLine(Math.round(hitLS1Moved.x), Math.round(hitLS1Moved.y), Math.round(hitLS2Moved.x), Math.round(hitLS2Moved.y));					
//			g.fillOval(Math.round(o_opc.x), Math.round(o_opc.y), 4,4);
//			g.fillOval(Math.round(o_npc.x), Math.round(o_npc.y), 4,4);
//			g.setColor(Color.GREEN);
//			g.fillOval(Math.round(o_ip.x), Math.round(o_ip.y), 4,4);
//		}

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
			timer.stop();
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
				System.out.println(ball.speed);
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) { }

	@Override
	public void keyReleased(KeyEvent e) { }

	@Override
	public void keyTyped(KeyEvent e) { }

	@Override
	public void mousePressed(MouseEvent e) {
		mouseClick = new Vector(e.getX(), e.getY());
	}

	@Override
	public void mouseReleased(MouseEvent e) { }

	@Override
	public void mouseEntered(MouseEvent e) { }

	@Override
	public void mouseExited(MouseEvent e) { }

}
