package host;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

//this relay_listener thread is run on the host server
//it relays a sockets output to the other sockets, and listens for if it exits
public class relay_receiver implements Runnable{

	private Socket clientSocket;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	
	//make thread for new connected user
	public relay_receiver (Socket clientSocket) {
		this.clientSocket=clientSocket;
	}
	
	public void run() {
		try{
			BufferedReader get =
			        new BufferedReader(
			            new InputStreamReader(clientSocket.getInputStream()));
			
			//first thing, tell everyone this user connected
			for(Socket client:connection_listener.connected_users){
				try{
					new PrintWriter(client.getOutputStream(), true).println("["+sdf.format(new Date(System.currentTimeMillis()))+"] " + "User <ANON"+connection_listener.connected_users.size() + "> connected to chat");
				}catch(IOException u){
					u.printStackTrace();
					System.out.println("Could not inform " +client + " of new connection");
				}
			}

			while(true){
				String inputstring=get.readLine();
				
				//this prevents null reading, and blocks until such time
				if(inputstring!=null && inputstring.length()>0){
					
					//add timestamps
					inputstring="["+sdf.format(new Date(System.currentTimeMillis()))+"] "+ inputstring;

					//relay this text to all connected users
					for(Socket client:connection_listener.connected_users){
						try{
							new PrintWriter(client.getOutputStream(), true).println(inputstring);
						}catch(IOException u){
							u.printStackTrace();
							System.out.println("Could not relay to client:" +client);
						}
					}
					
					//if the user exits, remove from list and close thread
					if(inputstring.endsWith("/exit")){
						connection_listener.connected_users.remove(clientSocket);
						return;
					}
					
					//have to flush string
					inputstring=null;
				}
			}
		}catch(IOException e){
			e.printStackTrace();
			System.out.println("Couldnt read input stream");
		}
	}
}
