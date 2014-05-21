package main;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;

//this listener thread listens to this socket's input stream
//print any text it receives, as well as handling commands
public class listener_receiver implements Runnable{
	
	public void run() {
		BufferedReader get = null;
		try{
			get = new BufferedReader(new InputStreamReader(p2p_user.clientsocket.getInputStream()));
		}catch(IOException e){
			e.printStackTrace();
			System.out.println("Could not open input stream");
		}
		
		while(p2p_user.connected){
			String inputstring = null;
			try {
				inputstring = get.readLine();
			} catch (IOException e1) {
				//user has properly closed the socket, exit thread
				if(!p2p_user.connected){
					return;
				}
				//the server side has been improperly closed
				else{
					p2p_user.gui.set_text("Server has been destroyed.");
					//reset main loop to beginning (try to create server)
					p2p_user.connected=false;
					//new host is the emergency host
					if(!p2p_user.BACKUP_HOST.equals("")){
						//connect, dont host
						p2p_user.hosting="c";
						p2p_user.HOST=p2p_user.BACKUP_HOST;
						p2p_user.gui.set_text("Connecting to backup server.");
					}
					//emergency catch if no one is assigned to be emergency host, this user hosts
					else{
						p2p_user.hosting="h";
						p2p_user.gui.set_text("No backup server found. Starting a new server.");
					}
					//close this thread (main will reopen it)
					return;
				}
			}
			
			//this prevents null reading, and blocks until such time
			if(inputstring!=null && inputstring.length()>0){
				
				//check to see if its the server assiging name
				if(inputstring.startsWith("server-assigned-nick: ")){
					p2p_user.name=inputstring.substring(22);
					p2p_user.gui.set_text("you are called "+p2p_user.name);
				}
				
				else if(inputstring.matches("[0-9]+ is the backup host")){
					p2p_user.BACKUP_HOST=inputstring.substring(0,inputstring.indexOf(" is the backup host"));
				}
				
				//if someone wants user's public key, broadcast it
				else if(inputstring.matches("\\[[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\] \\<(.*)\\> \\: \\/request " +p2p_user.name+ " key")){
					BigInteger[] publickey=p2p_user.Users_RSA.publickey();
					
					try{
						new PrintWriter(p2p_user.clientsocket.getOutputStream(), true).println(
								"Public Key for " + p2p_user.name + ":" +
								"n-"+publickey[0] +
								"e-"+publickey[1]);
						
						p2p_user.gui.set_text(inputstring.substring(0,inputstring.indexOf("> :")+1) +" requested your public key. It has been broadcast.");
					}catch(IOException u){
						u.printStackTrace();
						p2p_user.gui.set_text("ERROR: unable to broadcast public key");
					}
				}
				
				//if someone else is broadcasting their public key, store it
				else if(inputstring.matches("\\[[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\] Public Key for (.*)\\:n-[0-9]+e-[0-9]+")){
					BigInteger n=new BigInteger(inputstring.substring(inputstring.indexOf("n-")+2, inputstring.indexOf("e-")));
					BigInteger e=new BigInteger(inputstring.substring(inputstring.indexOf("e-")+2));
					String name=inputstring.substring(inputstring.indexOf("Public Key for ")+15,inputstring.indexOf(":n-"));
					String date=inputstring.substring(0,10);
					
					//if its not your public key and you dont already have it
					if(!name.equals(p2p_user.name) && !p2p_user.other_users_public_keys.contains(name)){
						p2p_user.other_users_public_keys.add(new RSA(n,e,name));
						p2p_user.gui.set_text(date+" Got "+name+" public key");
					}
				}
				
				//if someone is sending a dm, check if its directed to this user, and decrypt it
				else if(inputstring.matches("\\[[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\] \\<(.*)\\> \\: DM-"+p2p_user.name+" m-[0-9]+") && !p2p_user.blacklist.contains(inputstring.substring(inputstring.indexOf("<")+1,inputstring.indexOf(">")))){
					BigInteger encryptedmss= new BigInteger(inputstring.substring(inputstring.indexOf("m-")+2));
					p2p_user.gui.set_text(inputstring.substring(0,inputstring.indexOf("m-")+2)
										+p2p_user.Users_RSA.Decrypt(encryptedmss));
				}
				
				//if someone exits
				else if(inputstring.matches("\\[[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\] \\<(.*)\\> \\: \\/exit")){
					String name=inputstring.substring(inputstring.indexOf("<")+1,inputstring.indexOf(">"));
					p2p_user.gui.set_text(name + " left the chat");
					//remove from connected users
					p2p_user.gui.removeUser(name);
				}
				
				//if someone connects
				else if(inputstring.matches("\\[[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\] User \\<(.*)\\> connected to chat")){
					String name=inputstring.substring(inputstring.indexOf("<")+1,inputstring.indexOf(">"));
					p2p_user.gui.set_text(inputstring);
					//add connected user
					p2p_user.gui.addUser(name);
				}
				
				//if user connects and receives the list of people already connected
				else if(inputstring.matches("User \\<(.*)\\> is connected to chat")){
					String name=inputstring.substring(inputstring.indexOf("<")+1,inputstring.indexOf(">"));
					//add connected user
					p2p_user.gui.addUser(name);
				}
				
				//if someone changes their name
				else if(inputstring.matches("\\[[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\] (.*) is now called (.*)")){
					String oldname=inputstring.substring(inputstring.indexOf("]")+2,inputstring.indexOf(" is"));
					String newname=inputstring.substring(inputstring.indexOf("called ")+7);
					p2p_user.gui.set_text(inputstring);
					//replace connected users name
					p2p_user.gui.replaceUser(oldname,newname);
				}
				
				else{
					//This works, but feels wrong
					//check is this text is a user text message (not a user action alert), and then check against blacklist
					if(inputstring.matches("(.*)<(.*)>(.*)") && !p2p_user.blacklist.contains(inputstring.substring(inputstring.indexOf("<")+1,inputstring.indexOf(">")))){
						p2p_user.gui.set_text(inputstring);
					}
					//text only doesnt have <Name> if its an action alert. Allow it
					else if(!inputstring.matches("(.*)<(.*)>(.*)")){
						p2p_user.gui.set_text(inputstring);
					}
				}
				
				//have to flush string
				inputstring=null;
			}
		}
	}
}
