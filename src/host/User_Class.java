package host;

import java.net.Socket;


//basic encapsulation class for a user's socket and name and local ip
public class User_Class {

	private Socket socket;
	private String name;
	private String IP;
	
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
	
	public String getIP(){
		return IP;
	}
	
	public void setIP(String ip){
		IP=ip;
	}
}
