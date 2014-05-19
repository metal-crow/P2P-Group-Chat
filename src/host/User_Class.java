package host;

import java.net.Socket;


//basic encapsulation class for a user's socket and name
public class User_Class {

	private Socket socket;
	private String name;
	
	public User_Class(Socket s,String n){
		socket=s;
		name=n;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String n){
		name=n;
	}
	
	public Socket getSocket(){
		return socket;
	}
}
