import host.connection_listener;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;



public class p2p_user {

	private static final int PORT = 8888;
	private static final String ADDRESS = "localhost";
	
	public static Socket clientsocket;
	public static String name="undefined";
	
	public static RSA Users_RSA= new RSA(1024);
	public static ArrayList<RSA> other_users_public_keys=new ArrayList<RSA>();
	
	public static void main(String[] args) {
		boolean connecting=true;
		while(connecting){
			try {
				System.out.println("setting up server...");
				
				//if hosting the server, make a connection listener
				try {
					//start server
					ServerSocket server=new ServerSocket(PORT);
					//start the listener for connecting clients
			        Thread connection_listener_thread = new Thread(new connection_listener(server));
			        connection_listener_thread.start();
				} catch (IOException e) {
					System.out.println("Server already exists. Connecting...");
				}
		
				clientsocket = new Socket(ADDRESS, PORT);
		        
				//make sure to listen to your own socket.
				Thread reciver_thread = new Thread(new listener_receiver());
				reciver_thread.start();
				
		        //listen for input from this client
				Scanner from_client = new Scanner(System.in);
				
				boolean connected=true;
				System.out.println("You are connected! Type /help for a list of commands.");
					
				while(connected){
					//TODO if the host server disconnects, recreate it
					if(!clientsocket.isConnected()){
						System.out.println("Recreating server");
						try{
							//start server
							ServerSocket server=new ServerSocket(PORT);
							//start the listener for connecting clients
					        Thread connection_listener_thread = new Thread(new connection_listener(server));
					        connection_listener_thread.start();
						}catch(IOException e){
							e.printStackTrace();
						}
					}
					
					String users_input = from_client.nextLine();//get user input
					
					while(users_input!=null && users_input.length()>0){
						//for user to exit (others can see)
						if(users_input.equals("/exit")){
							connected=false;
							connecting=false;
							try{
								new PrintWriter(clientsocket.getOutputStream(), true).println(name+" left the chat");
							}catch(IOException u){
								u.printStackTrace();
								System.out.println("Could not write to output (exiting)");
							}
							System.out.println("Bye!");
							from_client.close();
							clientsocket.close();
						}
						
						//see commands (others can't see)
						else if(users_input.equals("/help")){
							System.out.println("Type '/exit' to exit");
							System.out.println("Type '/nick NEWNAME' to change name.");
							System.out.println("Type '/request USERSNAME key' to be able a private message to a user.");
							System.out.println("Type '/dm USERSNAME m:MESSAGETEXT' to send a private message to a user you have a key from.");
						}
						
						//for user to change name (others can see)
						else if(users_input.contains("/nick")){
							try{
								new PrintWriter(clientsocket.getOutputStream(), true).println(name+" is now called " + users_input.substring(6));
							}catch(IOException u){
								u.printStackTrace();
								System.out.println("Could not write to output");
							}
							name=users_input.substring(6);
							System.out.println("You are now called "+name);
						}
						
						//for user to send a dm to a user using their public key (others can only see encrypted)
						else if(users_input.toLowerCase().contains("/dm")){
							//since a dm is supposed to be private, try to be forgiving if user fudges command
							String dm_message=users_input.toLowerCase().substring(users_input.indexOf("m:")+2);
							String username=users_input.toLowerCase().substring(7,users_input.indexOf(" m:"));
							boolean founduser=false;
							
							for(RSA user:other_users_public_keys){
								if(user.name().equals(username)){
									founduser=true;
									try{
										new PrintWriter(clientsocket.getOutputStream(), true).println(name+":" +
												"DM-"+username+
												" m-"+user.Encrypt(dm_message));
										System.out.println("Sucessfully send dm to "+username);
									}catch(IOException u){
										u.printStackTrace();
										System.out.println("Could not write to output");
									}
								}
							}
							
							if(!founduser){
								System.out.println("You do not have the key for user " + username + ". Request it and retry your message.");
							}
						}
						
						//anything not specifically caught by commands
						else{
							//write to the socket's output stream and the server picks it up
							try{
								new PrintWriter(clientsocket.getOutputStream(), true).println(name+":"+users_input);
							}catch(IOException u){
								u.printStackTrace();
								System.out.println("Could not write to output");
							}
						}
						
						//flush input
						users_input=null;
					}
				}
				
			} catch (IOException e) {
				System.out.println("Could not connect. Do you want to retry? Y/N");
				Scanner in = new Scanner(System.in);
				if(in.next().toLowerCase().equals("n")){
					connecting=false;
					in.close();
				}
			}
		}
	}
}
