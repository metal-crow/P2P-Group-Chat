package GUI;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import main.p2p_user;

@SuppressWarnings("serial")
public class GUI extends JPanel{
	private int width;
	private int height;
	private JTextPane chat_text = new JTextPane();
	private JTextField input = new JTextField();
	
	public GUI(int height, int width){
		this.width=width;
		this.height=height;
		
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		chat_text.setEditable(false);
		JScrollPane chat_text_sp = new JScrollPane(chat_text);
		chat_text_sp.setPreferredSize(new Dimension(width,height-25));
		
		JButton send = new JButton("Send");
		send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
            	p2p_user.users_input=input.getText();
            	input.setText("");
            }
		});
		
		add(chat_text_sp);
		add(input);
		add(send);
	}
	
	public Dimension getPreferredSize() {
        return new Dimension(width,height);
    }
	
	public void set_text(String txt){
		chat_text.setText(chat_text.getText()+txt+"\n");
	}
	
}
