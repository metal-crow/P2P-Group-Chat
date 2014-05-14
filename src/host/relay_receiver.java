package host;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

//this relay_listener thread is run on the host server
//it relays a sockets output to the other sockets, and listens for if it exits
public class relay_receiver implements Runnable{

	private Socket clientSocket;
	
	//make thread for new connected user
	public relay_receiver (Socket clientSocket) {
		this.clientSocket=clientSocket;
	}
	
	public void run() {
		try{
			BufferedReader get =
			        new BufferedReader(
			            new InputStreamReader(clientSocket.getInputStream()));
			
			while(true){
				String inputstring=get.readLine();
				
				//this prevents null reading, and blocks until such time
				if(inputstring!=null && inputstring.length()>0){

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
