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
		while(p2p_user.connected){
			String inputstring = null;
			try {
				inputstring = get.readLine();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.out.println("Could not read input stream");
			}
			
			//this prevents null reading, and blocks until such time
			if(inputstring!=null && inputstring.length()>0){
				
				//System.out.println("getting " + inputstring);
				
				//check to see if its the server assiging name
				if(inputstring.startsWith("server-assigned-nick: ")){
					p2p_user.name=inputstring.substring(22);
					p2p_user.gui.set_text("you are called "+p2p_user.name);
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
					p2p_user.gui.set_text(inputstring.substring(inputstring.indexOf("<")+1,inputstring.indexOf(">")) + " left the chat");
				}
				
				else{
						//System.out.println("not catching " + inputstring);
					//check to make sure text can be parsed to check against blacklist, then check blacklist
					//TODO this is just wrong. ONLY TEMPORARY
					if(inputstring.matches("(.*)<(.*)>(.*)") && !p2p_user.blacklist.contains(inputstring.substring(inputstring.indexOf("<")+1,inputstring.indexOf(">")))){
						p2p_user.gui.set_text(inputstring);
					}
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
