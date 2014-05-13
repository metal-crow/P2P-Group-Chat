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
		
		//TODO this gets a socketclosed error when user exits
		while(!p2p_user.clientsocket.isClosed()){
			String inputstring = null;
			try {
				inputstring = get.readLine();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.out.println("Could not read input stream");
			}
			
			//this prevents null reading, and blocks until such time
			if(inputstring!=null && inputstring.length()>0){
				//check to see if its the server assiging name
				if(inputstring.startsWith("server-assigned-nick: ")){
					p2p_user.name=inputstring.substring(22);
					p2p_user.gui.set_text("you are called "+p2p_user.name);
				}
				
				//if someone wants user's public key, broadcast it
				else if(inputstring.contains("/request " + p2p_user.name + " key")){
					BigInteger[] publickey=p2p_user.Users_RSA.publickey();
					
					try{
						new PrintWriter(p2p_user.clientsocket.getOutputStream(), true).println(
								"Public Key for " + p2p_user.name + ":" +
								"n-"+publickey[0] +
								"e-"+publickey[1]);
						
						p2p_user.gui.set_text("Broadcast public key");
					}catch(IOException u){
						u.printStackTrace();
						System.out.println("Could not write to output");
					}
				}
				
				//if someone else is broadcasting their public key, store it
				else if(inputstring.startsWith("Public Key for ")){
					BigInteger n=new BigInteger(inputstring.substring(inputstring.indexOf("n-")+2, inputstring.indexOf("e-")));
					BigInteger e=new BigInteger(inputstring.substring(inputstring.indexOf("e-")+2));
					String name=inputstring.substring(inputstring.indexOf("Public Key for ")+15,inputstring.indexOf(":n-"));
					
					p2p_user.other_users_public_keys.add(new RSA(n,e,name));
					p2p_user.gui.set_text("Got "+name+" public key");
				}
				
				//if someone is sending a dm, check if its directed to this user, and decrypt it
				else if(inputstring.contains("DM-"+p2p_user.name) && !p2p_user.blacklist.contains(inputstring.substring(0,inputstring.indexOf(":")))){
					BigInteger encryptedmss= new BigInteger(inputstring.substring(inputstring.indexOf("m-")+2));
					p2p_user.gui.set_text(inputstring.substring(0,inputstring.indexOf("m-")+2)
										+new String(p2p_user.Users_RSA.Decrypt(encryptedmss).toByteArray()));
				}
				
				else{
					//check to make sure text can be parsed to check against blacklist, then check blacklist
					if(!inputstring.contains(":") || !p2p_user.blacklist.contains(inputstring.substring(0,inputstring.indexOf(":")))){
						p2p_user.gui.set_text(inputstring);
					}
				}
				
				//have to flush string
				inputstring=null;
			}
		}
	}
}
