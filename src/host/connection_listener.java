package host;
import main.p2p_user;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

//this is the thread for the server that will listen for any user connections, and create a listener thread for each
public class connection_listener implements Runnable{
	
	private ServerSocket server;
	public static final ArrayList<User_Class> connected_users=new ArrayList<User_Class>(1);
	public static int backuphost=-1;
	
	public connection_listener(ServerSocket server) {
		this.server=server;
	}
	
	public void run() {
		//TODO need a way to end when the hosting user disconnects
		while(true){
			try {
				Socket clientSocket = server.accept();
				
				//put this new accepted client in arraylist, which is read by each reciver so it can send each user's text to all clients
				User_Class user = new User_Class(clientSocket,"ANON"+(connected_users.size()+1));
				connected_users.add(user);
				
				//server must listen to each connected socket and relay their text
				Thread reciver_thread = new Thread(new relay_receiver(user));
				reciver_thread.start();
				
				PrintWriter clientout=new PrintWriter(clientSocket.getOutputStream(), true);
				
				//tell the client their default name
				clientout.println("server-assigned-nick: ANON"+connected_users.size());
				
		        //tell client all currently connected users (excluding self)
				for(int i=0;i<connected_users.size()-1;i++){
					clientout.println("User <"+connected_users.get(i).getName()+ "> is connected to chat");
				}
				
				//tell clients who has been randomly picked to be the next host
				//everytime a new client connects, a new backup host is randomly picked
				generateBackupHost();
				
			} catch (IOException e) {
				e.printStackTrace();
				p2p_user.gui.set_text("ERROR: Could not accept new user");
			}
		}	
	}
	
	public static void generateBackupHost(){
		if(connected_users.size()>1){
			backuphost=new Random().nextInt((connected_users.size() - 1) + 1);
			
			//if the chosen user isn't the one who just connected, we have their ip and can broadcast it
			if(backuphost<(connected_users.size()-1)){
				for(User_Class client:connected_users){
					try{
						new PrintWriter(client.getSocket().getOutputStream(), true).println(connected_users.get(backuphost).getIP() + " is the emergency host");
					}catch(IOException u){
						u.printStackTrace();
						System.out.println("Could not inform " +client + " of backup host");
					}
				}
			}
		}
	}

}
