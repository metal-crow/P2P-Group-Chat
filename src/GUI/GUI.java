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
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;

import main.p2p_user;

@SuppressWarnings("serial")
public class GUI extends JPanel{
	private int width;
	private int height;
	
	private JTextPane chat_text = new JTextPane();
	private JTextField input = new JTextField(25);
	private JTextPane connected_users = new JTextPane();
	
	//TODO color coding,fix layout
	
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
		
		//send and clear text
		JButton send = new JButton("Send");
		send.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
            	p2p_user.handle_GUI_input(input.getText());
            	input.setText("");
            }
		});
		
		//on enter press in input, field, do same as send does
		input.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),"send");
		input.getActionMap().put("send",new AbstractAction() {
		    public void actionPerformed(ActionEvent e) {
		        p2p_user.handle_GUI_input(input.getText());
            	input.setText("");
		    }
		});
		
		connected_users.setEditable(false);
		JScrollPane connected_users_sp = new JScrollPane(connected_users);
		
		JPanel chat_and_users= new JPanel();
		chat_text_sp.setPreferredSize(new Dimension(width-85,height-50));
		chat_and_users.add(chat_text_sp);
		chat_and_users.add(connected_users_sp);
		connected_users_sp.setPreferredSize(new Dimension(75,height-50));
		add(chat_and_users);
		
		JPanel user_input = new JPanel();
		user_input.add(input);
		user_input.add(send);
		add(user_input);
	}
	
	public Dimension getPreferredSize() {
        return new Dimension(width,height);
    }
	
	public void set_text(String txt){
		try {
		      Document doc = chat_text.getDocument();
		      doc.insertString(doc.getLength(), txt+"\n", null);
		} catch(BadLocationException exc) {
		      exc.printStackTrace();
		      System.out.println("Could not add to chat textbox");
		}
	}
	
	public void addUser(String user){
		try {
		      Document doc = connected_users.getDocument();
		      doc.insertString(doc.getLength(), user+"\n", null);
		} catch(BadLocationException exc) {
		      exc.printStackTrace();
		      System.out.println("Could not add user to connected user textbox");
		}
	}
	public void removeUser(String user){
		try {
		      Document doc = connected_users.getDocument();
		      int location_of_name=doc.getText(0, doc.getLength()).indexOf(user);
		      doc.remove(location_of_name, user.length()+1);
		} catch(BadLocationException exc) {
		      exc.printStackTrace();
		      System.out.println("Could not remove user from connected user textbox");
		}
	}
	public void replaceUser(String oldname,String newname){
		try {
		      Document doc = connected_users.getDocument();
		      int location_of_name=doc.getText(0, doc.getLength()).indexOf(oldname);
		      doc.remove(location_of_name, oldname.length());
		      doc.insertString(location_of_name, newname, null);
		} catch(BadLocationException exc) {
		      exc.printStackTrace();
		      System.out.println("Could not change users name in connected user textbox");
		}
	}
	//wipe list of currently connected users (used if a user takes over as host, we dont want old list sticking around)
	public void resetConnectedUsers(){
		connected_users.setText("");
	}
	//this seems wrong
	public void closeGUI(){
		System.exit(0);
	}
}
