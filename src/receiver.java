import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
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
					if(!host){
						//check to see if its the server assiging name
						if(inputstring.contains("server-assigned-nick: ")){
							p2p_user.name=inputstring.substring(22);
							System.out.println("you are called "+p2p_user.name);
						}
						
						//if someone wants user's public key, broadcast it
						else if(inputstring.contains("/request " + p2p_user.name + " key")){
							BigInteger[] publickey=p2p_user.Users_RSA.publickey();
							
							try{
								new PrintWriter(p2p_user.clientsocket.getOutputStream(), true).println(
										"Public Key for " + p2p_user.name + ":" +
										"n-"+publickey[0] +
										"e-"+publickey[1]);
							}catch(IOException u){
								u.printStackTrace();
								System.out.println("Could not write to output");
							}
							
							System.out.println("Broadcast public key");
						}
						
						//if someone else is broadcasting their public key, store it
						else if(inputstring.contains("Public Key for ")){
							BigInteger n=new BigInteger(inputstring.substring(inputstring.indexOf("n-")+2, inputstring.indexOf("e-")));
							BigInteger e=new BigInteger(inputstring.substring(inputstring.indexOf("e-")+2));
							String name=inputstring.substring(inputstring.indexOf("Public Key for ")+15,inputstring.indexOf(":n-"));
							
							p2p_user.other_users_public_keys.add(new RSA(n,e,name));
							System.out.println("Got "+name+" public key");
						}
						
						//if someone is sending a dm, check if its directed to this user, and decrypt it
						else if(inputstring.contains("DM-"+p2p_user.name)){
							BigInteger encryptedmss= new BigInteger(inputstring.substring(inputstring.indexOf("m-")+2));
							System.out.println(inputstring.substring(0,inputstring.indexOf("m-")+2)
												+new String(p2p_user.Users_RSA.Decrypt(encryptedmss).toByteArray()));
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
			e.printStackTrace();
			System.out.println("Couldnt read input stream");
		}
	}
}
