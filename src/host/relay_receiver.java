package host;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

//this relay_listener thread is run on the host server
//it relays a sockets output to the other sockets, and listens for if it exits
public class relay_receiver implements Runnable{

	private int index;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	
	//make thread for new connected user
	public relay_receiver (int index) {
		this.index=index;
	}
	
	public void run() {
		try{
			BufferedReader get =
			        new BufferedReader(
			            new InputStreamReader(connection_listener.connected_users.get(index).getSocket().getInputStream()));
			
			//first thing, tell everyone this user connected
			for(User_Class client:connection_listener.connected_users){
				try{
					new PrintWriter(client.getSocket().getOutputStream(), true).println("["+sdf.format(new Date(System.currentTimeMillis()))+"] " + "User <ANON"+connection_listener.connected_users.size() + "> connected to chat");
				}catch(IOException u){
					u.printStackTrace();
					System.out.println("Could not inform " +client + " of new connection");
				}
			}

			while(true){
				String inputstring=get.readLine();
				
				//this prevents null reading, and blocks until such time
				if(inputstring!=null && inputstring.length()>0){
					
					//user is inform us of their ip. Add to file, and dont relay to other users
					if(inputstring.matches("Local ip=[0-9]+")){
						String ip=inputstring.substring(inputstring.indexOf("="));
						connection_listener.connected_users.get(index).setIP(ip);
					}
					
					else{
						//add timestamps
						inputstring="["+sdf.format(new Date(System.currentTimeMillis()))+"] "+ inputstring;
	
						//relay this text to all connected users
						for(User_Class client:connection_listener.connected_users){
							try{
								new PrintWriter(client.getSocket().getOutputStream(), true).println(inputstring);
							}catch(IOException u){
								u.printStackTrace();
								System.out.println("Could not relay to client:" +client);
							}
						}
						
						//if the user exits, remove from list and close thread
						if(inputstring.matches("\\[[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\] \\<(.*)\\> \\: \\/exit")){
							connection_listener.connected_users.remove(index);
							return;
						}
						
						//if user changes name, store it
						else if(inputstring.matches("\\[[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\] (.*) is now called (.*)")){
							String newname=inputstring.substring(inputstring.indexOf("called ")+7);
							connection_listener.connected_users.get(index).setName(newname);
						}
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
