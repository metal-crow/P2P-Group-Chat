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

	//passing user_class and searching in array for it instead of passing its index, as if a user exits its index can change
	private User_Class user;
	private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	
	//make thread for new connected user
	public relay_receiver (User_Class c) {
		user=c;
	}
	
	public void run() {
		try{
			BufferedReader get =
			        new BufferedReader(
			            new InputStreamReader(user.getSocket().getInputStream()));
			
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
						String ip=inputstring.substring(inputstring.indexOf("=")+1);
						user.setIP(ip);
						connection_listener.connected_users.set(connection_listener.connected_users.indexOf(user),user);
						
						//if this user is the chosen emergency host, now that we know their ip, broadcast it
						if(connection_listener.backuphost==connection_listener.connected_users.indexOf(user)){
							for(User_Class client:connection_listener.connected_users){
								try{
									new PrintWriter(client.getSocket().getOutputStream(), true).println(user.getIP() + " is the emergency host");
								}catch(IOException u){
									u.printStackTrace();
									System.out.println("Could not inform " +client + " of backup host");
								}
							}
						}
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
							connection_listener.connected_users.remove(connection_listener.connected_users.indexOf(user));
							//since this user COULD have been the backup host, and if they exit and we dont have a new backup host were in trouble,
							//every time someone exits generate a new backup host
							connection_listener.generateBackupHost();
							return;
						}
						
						//if user changes name, store it
						else if(inputstring.matches("\\[[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\] (.*) is now called (.*)")){
							String newname=inputstring.substring(inputstring.indexOf("called ")+7);
							user.setName(newname);
							connection_listener.connected_users.set(connection_listener.connected_users.indexOf(user),user);
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
