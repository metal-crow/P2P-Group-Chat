import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

//this listener thread will output any text each connected socket writes
//it will also relay that texts to all other connected sockets
public class receiver implements Runnable{

	private Socket clientSocket;
	private boolean host;
	
	//make thread for new connected user
	public receiver (Socket clientSocket, boolean host) {
		this.clientSocket=clientSocket;
		this.host=host;
	}
	
	public void run() {
		try{
			BufferedReader get =
			        new BufferedReader(
			            new InputStreamReader(clientSocket.getInputStream()));
			
			while(true){
				String inputstring=get.readLine();
				
				//this prevents null reading, and blocks until such time
				while(inputstring!=null && inputstring.length()>0){
					//check to see if its the server saying something (only pay attention if not the server)
					if(!host){
						if(inputstring.contains("server-assigned-nick: ")){
							p2p_user.name=inputstring.substring(22);
							System.out.println("you are called "+p2p_user.name);
						}
						else{
							System.out.println(inputstring);
						}
					}
					
					if(host){
						//if the user exits, remove from list and close thread
						if(inputstring.equals("/exit")){
							System.out.println(inputstring);
							connection_listener.connected_users.remove(clientSocket);
							return;
						}
					
						//relay this text to all connected users
						for(Socket client:connection_listener.connected_users){
							//also, dont send the message from the user back to the user that sent it 
							if(!client.equals(clientSocket)){
								try{
									new PrintWriter(client.getOutputStream(), true).println(inputstring);
								}catch(IOException u){
									u.printStackTrace();
									System.out.println("Could not relay to client:" +client);
								}
							}
						}
					}
					
					//have to flush string
					inputstring=null;
				}
			}
		}catch(IOException e){
			System.out.println("Couldnt read input stream");
		}
	}
}
