package host;
import main.p2p_user;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

//this is the thread for the server that will listen for any user connections, and create a listener thread for each
public class connection_listener implements Runnable{
	
	private ServerSocket server;
	public static final ArrayList<Socket> connected_users=new ArrayList<Socket>(1);
	
	public connection_listener(ServerSocket server) {
		this.server=server;
	}
	
	public void run() {
		//TODO need a way to end when the hosting user disconnects
		while(true){
			try {
				Socket clientSocket = server.accept();
				
				//put this new accepted client in arraylist, which is read by each reciver so it can send each user's text to all clients
				connected_users.add(clientSocket);
				
				//server must listen to each connected socket and relay their text
				Thread reciver_thread = new Thread(new relay_receiver(clientSocket));
				reciver_thread.start();
				
				//tell the client their default name
				new PrintWriter(clientSocket.getOutputStream(), true).println("server-assigned-nick: ANON"+connected_users.size());
				
				p2p_user.gui.set_text("User ANON"+connected_users.size() + " connected to chat");
		        
			} catch (IOException e) {
				e.printStackTrace();
				p2p_user.gui.set_text("ERROR: Could not accept new user");
			}
		}	
	}

}
