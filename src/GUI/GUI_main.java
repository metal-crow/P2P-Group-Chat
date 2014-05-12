package GUI;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

public class GUI_main{
	public static void main(String[] args) {
		createAndShowGUI(500,500);
	}
		
	private static void createAndShowGUI(int height, int width) {
        JFrame f = new JFrame("Chat Room");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(height,width);
		f.add(new GUI(height,width));
        f.pack();
        f.setVisible(true);
    }
}

@SuppressWarnings("serial")
class GUI extends JPanel{
	private int width;
	private int height;
	public JTextPane chat_text = new JTextPane();
	
	public GUI(int height, int width){
		this.width=width;
		this.height=height;
		
		chat_text.setEditable(false);
		JScrollPane chat_text_sp = new JScrollPane(chat_text);
		
		JTextField input = new JTextField();
		
		JButton send = new JButton("Send");
		send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
            	//input.
            	//send text
            }
		});
	}
	
	public Dimension getPreferredSize() {
        return new Dimension(width,height);
    }
	
}
