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
	private Club club;			
	private boolean courseDone;
	private Timer timer;

	public MiniGolf() {		
		addKeyListener(this);	
		addMouseListener(this);
		setBackground(Color.BLACK);
		setPreferredSize(new Dimension(500, 500));
		setFocusable(true);
		initCourse();			
	}

	private void initCourse() {	
		course = new Course();		
		ball = new Ball(course.ballStartPos.x, course.ballStartPos.y);
		club = new Club(ball);
		courseDone = false;
		timer = new Timer(15, this);
		timer.start();
	}

	@Override
	public void keyPressed(KeyEvent e) { 
		int kc = e.getKeyCode();
		if(kc == KeyEvent.VK_ESCAPE ) {
			System.exit(0);
		}
		if(kc == KeyEvent.VK_SPACE ) {
			timer.stop();
			initCourse();
		}
		if(kc == KeyEvent.VK_UP) {
			if(!ball.roling) {	
				club.powerPlus();
			}
		}
		if(kc == KeyEvent.VK_DOWN) {
			if(!ball.roling) {	
				club.powerMinus();
			}
		}
		if(kc == KeyEvent.VK_ENTER) {
			if(!ball.roling) {				
				ball.speed = club.ballCenterToClickUnit.times(-1.0f).times(club.power);
				club.power = 4;
				ball.roling = true;
				club.isMouseClicked = false;
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(!ball.roling) {
			club.mouseClicked(new Vector(e.getX(), e.getY()));
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);        
		g.setColor(Color.WHITE);  

		// Walls
		for(LineSegment l : course.lines) {
			g.drawLine(Math.round(l.x1), Math.round(l.y1), Math.round(l.x2), Math.round(l.y2));
		}

		// Goal
		g.drawOval(Math.round(course.goalPos.x), Math.round(course.goalPos.y), Math.round(ball.width + 6), Math.round(ball.width + 6));

		// Ball
		g.fillOval(Math.round(ball.pos.x), Math.round(ball.pos.y), Math.round(ball.width), Math.round(ball.width));

		// Club
		if(!ball.roling && club.isMouseClicked) {
			g.drawLine(Math.round(club.startClub.x), Math.round(club.startClub.y), Math.round(club.endClub.x), Math.round(club.endClub.y));
			g.drawLine(Math.round(club.startClub.x), Math.round(club.startClub.y), Math.round(club.clubArrowLeft.x), Math.round(club.clubArrowLeft.y));
			g.drawLine(Math.round(club.startClub.x), Math.round(club.startClub.y), Math.round(club.clubArrowRight.x), Math.round(club.clubArrowRight.y));			
		}

		//End Titles
		if(courseDone) {
			g.setColor(Color.YELLOW);
			Font font = new Font("Arial", Font.BOLD, 20);
			g.setFont(font);
			g.drawString("Done!", 225, 220);
		}

		Toolkit.getDefaultToolkit().sync();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(!courseDone) {
			// Define current position of ball and next position
			Vector o_oldPos = ball.pos;
			Vector o_opc = o_oldPos.plus(new Vector(ball.width / 2, ball.width / 2)); // Origo To Old Pos Center
			Vector o_newPos = ball.pos.plus(ball.speed);
			Vector o_npc = o_newPos.plus(new Vector(ball.width / 2, ball.width / 2)); // Origo To New Pos Center

			// Check For Collision
			boolean collision = false;
			boolean cornerHit = false;
			boolean straightHit = false;			

			LineSegment hitLS = null;
			Vector o_hitCorner = null;
			for(LineSegment ls : course.lines) {
				if(!straightHit) {
					Vector o_ls1 = new Vector(ls.x1, ls.y1);
					Vector o_ls2 = new Vector(ls.x2, ls.y2);

					Vector ls1_npc = o_npc.minus(o_ls1);
					Vector ls1_ls2 = o_ls2.minus(o_ls1);
					Vector ls1_ls2_ORT = new Vector(-ls1_ls2.y,ls1_ls2.x);
					float s1 = ls1_npc.cross(ls1_ls2_ORT);
					float s2 = ls1_ls2.cross(ls1_ls2_ORT);
					if(s1 * s2 <= 0) {
						// ball closest to endpoint ls1
						if(!cornerHit) {
							if(ls1_npc.length() < ball.width / 2) {
								collision = true;
								cornerHit = true;
								o_hitCorner = new Vector(ls.x1, ls.y1);
							}
						}
					} else {
						Vector ls2_npc = o_npc.minus(o_ls2);
						Vector ls2_Ls1 = o_ls1.minus(o_ls2);
						Vector ls2_ls1_ORT = new Vector(-ls2_Ls1.y, ls2_Ls1.x);
						float s3 = ls2_npc.cross(ls2_ls1_ORT);
						float s4 = ls2_Ls1.cross(ls2_ls1_ORT);
						if(s3 *s4 <= 0) {
							// Ball closest to endpoint ls2
							if(!cornerHit) {
								if(ls2_npc.length() < ball.width / 2) {
									collision = true;
									cornerHit = true;
									o_hitCorner = new Vector(ls.x2, ls.y2);
								}
							}
						} else {
							// Ball closest to some point on line segment
							Vector ls1_ls2_UNIT = ls1_ls2.dividedBy(ls1_ls2.length());
							float s5 = ls1_npc.dot(ls1_ls2_UNIT);
							Vector ls1ToPointClosestToBallOnLine = ls1_ls2_UNIT.times(s5);
							Vector pointClosestToBallOnLine = o_ls1.plus(ls1ToPointClosestToBallOnLine);
							Vector ballToPointClosestToBallOnLine = o_npc.minus(pointClosestToBallOnLine);
							if(ballToPointClosestToBallOnLine.length() < ball.width / 2) {
								collision = true;
								straightHit = true;
								hitLS = ls;
							}						
						}
					}
				}				
			}

			if(collision) {
				if(straightHit) {
					LineSegmentBallCollision(hitLS, o_opc, o_npc);									} 
				if(cornerHit) {					
					/***Circle line intersection***/
					//Circle around hit corner should be considered to have center at (0,0)					
					//Therefore we move the line between opc and npc correspondingly
					Vector o_opc_MOVED = new Vector(o_opc.x - o_hitCorner.x, o_opc.y - o_hitCorner.y);
					Vector o_npc_MOVED = new Vector(o_npc.x - o_hitCorner.x, o_npc.y - o_hitCorner.y);

					Vector v1 = o_opc_MOVED;
					Vector v2 = o_npc_MOVED;

					double d_x = v2.x - v1.x;
					double d_y = v2.y - v1.y;
					double d_r = Math.sqrt(d_x*d_x + d_y*d_y);
					double D = v1.x*v2.y - v2.x*v1.y;
					double r = ball.width / 2;

					double xPlus = (D*d_y + Math.signum(d_y) * d_x * Math.sqrt(r*r*d_r*d_r - D*D)) / (d_r*d_r);
					double xMinus = (D*d_y - Math.signum(d_y) * d_x * Math.sqrt(r*r*d_r*d_r - D*D)) / (d_r*d_r);

					double yPlus = (-D*d_x + Math.abs(d_y) * Math.sqrt(r*r*d_r*d_r - D*D)) / (d_r*d_r);
					double yMinus = (-D*d_x - Math.abs(d_y) * Math.sqrt(r*r*d_r*d_r - D*D)) / (d_r*d_r);					

					Vector vPlus = new Vector((float)xPlus, (float)yPlus);
					Vector vMinus = new Vector((float)xMinus, (float)yMinus);

					//o_ip_MOVED is the point that is closest to o_opc_MOVED
					Vector o_opc_MOVED_vPlus = vPlus.minus(o_opc_MOVED);
					float o_opc_MOVED_vPlus_LENGTH = o_opc_MOVED_vPlus.length();
					Vector o_opc_MOVED_vMinus = vMinus.minus(o_opc_MOVED);
					float o_opc_MOVED_vMinus_LENGTH = o_opc_MOVED_vMinus.length();

					Vector o_ip_MOVED = null;
					if(o_opc_MOVED_vPlus_LENGTH < o_opc_MOVED_vMinus_LENGTH) {
						o_ip_MOVED = vPlus;
					} else {
						o_ip_MOVED = vMinus;
					}

					/*** Define abstract line segment that the ball hits ***/
					Vector av1_MOVED = new Vector(-o_ip_MOVED.y, o_ip_MOVED.x);
					Vector av2_MOVED = new Vector(o_ip_MOVED.y, -o_ip_MOVED.x);

					Vector av1 = new Vector(av1_MOVED.x + o_hitCorner.x, av1_MOVED.y + o_hitCorner.y);
					Vector av2 = new Vector(av2_MOVED.x + o_hitCorner.x, av2_MOVED.y + o_hitCorner.y);

					LineSegment absL = new LineSegment(av1.x, av1.y, av2.x, av2.y);
					LineSegmentBallCollision(absL, o_opc, o_npc);					
				}
			} else {
				ball.pos = o_newPos;
			}

			// Check For Goal Position
			Vector o_centerGoal = new Vector(course.goalPos.x + ball.width / 2 + 3, course.goalPos.y + ball.width / 2 + 3); 
			if(o_npc.minus(o_centerGoal).length() < ball.width / 2 + 3) {
				ball.pos = course.goalPos.plus(new Vector(3, 3));
				courseDone = true;
			}

			// Change Ball Speed
			if(ball.speed.length() > 0.1f) {
				ball.speed = ball.speed.times(0.985f);
			} else {
				ball.speed = new Vector(0,0);
				ball.roling = false; 
			}

			repaint();
		}		
	}

	void LineSegmentBallCollision(LineSegment ls, Vector o_opc, Vector o_npc) {
		/*** Find the ball's new position ***/
		// Vectors to the end points of the line segment that the ball has collided with
		Vector o_v1 = new Vector(ls.x1, ls.y1);
		Vector o_v2 = new Vector(ls.x2, ls.y2);

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
		// The new position is simply the reflection around the hit line segment
		float distNpcTov1v2Moved = ball.width / 2 - projNpcOnv1v2_npc.length();
		Vector npc_npcAfterCollision = projNpcOnv1v2_npc_UNIT.times(distNpcTov1v2Moved * 2);
		Vector o_npcAfterCollision = o_npc.plus(npc_npcAfterCollision);
		Vector cnpc = o_npcAfterCollision; 	// Correct new position center
		ball.pos = new Vector(cnpc.x - ball.width / 2, cnpc.y - ball.width / 2);

		/*** Find the ball's new speed vector ***/
		// The direction of the speed vector is the vector that goes from the crossing of the moved collision line segment
		// and the line between opc og npc, to o_npcAfterCollision.
		// The length of this vector should be the same as the length of the prior speed vector (air friction is added below in the code)
		Vector moveLineSegmentBy = projNpcOnv1v2_npc_UNIT.times(ball.width / 2);
		Vector hitLS1Moved = o_v1.plus(moveLineSegmentBy);
		Vector hitLS2Moved = o_v2.plus(moveLineSegmentBy);

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

		Vector o_ip = new Vector(x, y);
		Vector ip_cnpc = cnpc.minus(o_ip);
		Vector ip_cnpc_UNIT = ip_cnpc.dividedBy(ip_cnpc.length());
		Vector newSpeedVector = ip_cnpc_UNIT.times(ball.speed.length());
		ball.speed = newSpeedVector;
	}

	@Override
	public void mouseClicked(MouseEvent e) { }
	@Override
	public void keyReleased(KeyEvent e) { }
	@Override
	public void keyTyped(KeyEvent e) { }
	@Override
	public void mouseReleased(MouseEvent e) { }
	@Override
	public void mouseEntered(MouseEvent e) { }
	@Override
	public void mouseExited(MouseEvent e) { }

}
