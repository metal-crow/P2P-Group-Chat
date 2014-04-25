import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;


public class p2p_user {

	private static final int PORT = 8888;
	public static String name="undefined";
	
	public static void main(String[] args) {
		boolean connecting=true;
		while(connecting){
			try {
				//create a connectable server
				System.out.println("setting up server...");
				
				//start the server, have it listen for incoming socket connections (clients)
				
				/*the problem is if i run this main file again i already have a server holding that port spot.
				 * if i have the next running user detect that there's a server, and connect to that server, the program will work,
				 * but it wont be a DECENTRALIZED p2p chat.
				 * TODO I can make it so that if the host user disconnects a different user is chosen to recreate the server and host.
				 * TODO have graceful closes on everything
				 * I think that a true decentralized p2p chat required a not-server based approach, which im not sure default java offers
				 * 
				 * 
				//method 1
				//i could have it so that the connected client sends the text to the server, which then relays it to all connected
				//clients, but that isnt really p2p
				
				//method 2
				//i think each client HAs to run a server, and everyone has to connect to each new clients server for this to work
				
				//if a group of peole are on lan, this group has to use method 1, and this group is a group.
				//Every new person or group on a different ip uses method 1, and the groups connect via method 2
				//i dont think multiple poeple on lan can have servers
				*/

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
		
				Socket clientsocket = new Socket("localhost", PORT);
		        
				//make sure to listen to your own socket, but this socket wont relay. Only server reciver does that
				Thread reciver_thread = new Thread(new receiver(clientsocket,false));
				reciver_thread.start();
				
		        //listen for input from this client
				Scanner from_client = new Scanner(System.in);
				
				boolean connected=true;
				System.out.println("You are connected! Type '/exit' to exit or /nick NEWNAME to change name.");
					
				while(connected){
					String users_input = from_client.nextLine();//get user input
					
					while(users_input!=null && users_input.length()>0){
						//for user to exit
						if(users_input.equals("/exit")){
							connected=false;
							connecting=false;
						}
						//for user to change name
						if(users_input.matches("/nick [a-zA-Z_0-9]+")){
							name=users_input.substring(6);
							System.out.println("You are now called "+name);
						}
						//write to the socket's output stream and the server picks it up
						try{
							new PrintWriter(clientsocket.getOutputStream(), true).println(name+":"+users_input);
						}catch(IOException u){
							u.printStackTrace();
							System.out.println("Could not write to output");
						}
						//flush input
						users_input=null;
					}
				}
					
				//exit
				System.out.println("Bye!");
				from_client.close();
				clientsocket.close();
				
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
