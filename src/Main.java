import java.awt.EventQueue;

import javax.swing.JFrame;

public class Main extends JFrame {

	public static void main(String[] args) {		
		EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
            	Main m = new Main();
                m.setVisible(true);
            }
        });
	}
	
	public Main() {
		add(new MiniGolf());        
        setResizable(false);
        pack();        
        setTitle("Mini Golf");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
