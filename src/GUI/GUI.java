package GUI;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultCaret;

import main.p2p_user;

@SuppressWarnings("serial")
public class GUI extends JPanel{
	private int width;
	private int height;
	private JTextPane chat_text = new JTextPane();
	//TODO f it, I'll fix this layout later
	private JTextField input = new JTextField(25);
	
	public GUI(int height, int width){
		this.width=width;
		this.height=height;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		chat_text.setEditable(false);
		DefaultCaret caret = (DefaultCaret)chat_text.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		JScrollPane chat_text_sp = new JScrollPane(chat_text);
		
		/*input.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
            	if(input.getText().startsWith("//")){
            		input.setC
            	}
            }
		});*/
		
		JButton send = new JButton("Send");
		send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
            	p2p_user.handle_GUI_input(input.getText());
            	input.setText("");
            }
		});
		
		input.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),"send");
		input.getActionMap().put("send",new AbstractAction() {
		    public void actionPerformed(ActionEvent e) {
		        p2p_user.handle_GUI_input(input.getText());
            	input.setText("");
		    }
		});
		
		add(chat_text_sp);
		JPanel user_input = new JPanel();
		user_input.add(input);
		user_input.add(send);
		add(user_input);
	}
	
	public Dimension getPreferredSize() {
        return new Dimension(width,height);
    }
	
	public void set_text(String txt){
		chat_text.setText(chat_text.getText()+txt+"\n");
	}
}
