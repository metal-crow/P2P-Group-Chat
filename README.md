Rudimentary P2P (only technically) group chat I've made.
==============
  
INFO:  
---------------------  
  
Currently in beta.  
<strike>It can communicate with other connected users over LAN.</strike>  
Using RSA, it allows people to DM each other securely and privately.  
It allows users to selectively block and unblock other users based on name.  
Has timestamps based on relay time.  
Uses Regex to make use user commands are properly caught (i.e. it won't do a command for "just type in /nick NAME to change your name")  
Stores and displays the list of connected usernames (only plaintext)  
Allows other connected users to take over as host if host disconnects  
  
Next steps are (probably in order of implementation)  
	-Learn a TON more about network communication  
	-Fix RSA generation bug  
	-Allow user to input program commands (connection failure) over gui  
	-More GUI work (colors and color coding, make like traditional IRC look and feel)  
	-Allow internet connections and have multiple hosts managing their LAN groups able to connect (ideally this will be more like true P2P)  
  
  
DOCUMENTATION:  
---------------------  
  
I am coding a group chat that will allow users connected over LAN to communicate, as well as a Direct Message system that uses RSA encryption to allow users to send messages to each other that can only be read by the recipient.  
  
The first stage, the group chat, will be implemented by having the first user to run the program create a server, which runs on a separate thread and listens for sockets to connect to that address. The first user, besides running this thread, is treated in the same way as every other user. Each user creates a socket, connects to the server, and creates a receiver thread that listens for any input on that sockets input stream. The user can write text to their sockets output stream, which includes their name, and the server has a thread listening to each connected socket attached to it. When it receives the text from that sockets output stream, it relays this text to all the other sockets (except the socket it received it from), by writing it to their input stream. Each socket then reads their output stream in the listener thread, and prints it out.  
  
More details for group chat:  
	When a user connects the server writes a default name to their socket stream based on the number of people connected (i.e. for 5 people connected a new user is ANON6). The socket receives this text in the listener thread, and recognizes that is comes from the server, not another user imitating it, and changes the user name to match this name. Their name is auto-attached to every output they write. The user can also change their name by typing in a command to the chat which changes their name to the text after the command (i.e. /nick NEWNAME).  
	There are multiple commands possible in the chat. When the user enters a command that is listened for when the user sends their text, the code for that respective command is executed (this is just if listeners). This is the same for the socket listener each user runs that listens to their socket’s input. If they receives input that matches pre-coded commands specific code for each is executed (again, if statements). Some example of user side commands are  
•	/nick (change name)  
•	/help (list commands)  
•	/request USERNAME key (get a user’s public key)  
•	/dm USERNAME m:MESSAGE (send a dm to a user)  
•	/block USERNAME (blocks this user's DM's and messages)  
•	/unblock USERNAME (unblocks this user)  
•	More may be added  
There are also commands that the socket listener listens for. These are  
•	server-assigned-nick: (change user’s name)  
•	/request USERNAME key (broadcasts the user’s public key)  
•	Public Key for (indicates that another user has broadcast their public key, and adds it to this user’s public key array)  
•	DM- USERNAME (that someone has send a dm to this user, and it must be decrypted)  
•	More may be added  


The Second stage, the encrypted DM’s, is done by generating a RSA class that has two constructors. One is the normal RSA construction, which generates 2 primes, computes their product (n) and totient, generates a number (e) co-prime to the totient, and gets the modular multiplicative inverse of that number (d) and the totient, generates the private key and public key. The second constructor takes in the public key as input, and stores it. These classes have an encrypt method that encrypts text by converting it to a byte array, then to a BigInteger, then raising it to e modulo n. There is also a decrypt function which takes in a BigInteger, raises it to d modulo n, converts it to a byte array, and converts that to a string. When the RSA class is constructed with the second constructor, with only the public key, this second method cannot be done. When a user runs the program, they generate an RSA class with the first constructor, and an array of RSA classes. When they ask another user for their public key (using an automated command build into the chat), their public key is used to create another RSA class (via second constructor), which is added to the RSA array. When the user in possession of the public key wants to DM the user with the private key, they encrypt their text with the public key generated RSA class, post it to the group chat, and the user it is addressed to automatically read and decrypted using their private key enabled RSA class. This allows private communication.  
  
The Third stage, the decentralization, is when multiple users are connected to the chat, and the host user terminates. If this occurs, another user must be chosen to be the host, and run the connection listener and relay receiver threads. In the main file, whenever the user starts the program it checks if the server exists and either creates it or connects to it, depending on the result. If the host exits, then the listener receiver gets an error, which we can tell it it’s unexpected by checking a Boolean in the main file that keeps track of the users connection state. (This error is generated whenever the socket closes, but if we purposefully close the socket we change the Boolean value and know that this error is acceptable. I know this is weird, but it works.) If this error is thrown and the Boolean is unchanged, we can conclude that the connection was terminated by the host’s disconnection, and need to recreate the host. The main file has been running in a loop while the program is running, but doesn’t do anything because once the user connects a Boolean “connected” is set to true, and not more code is executed in the wile loop. When the host disconnects, we detect this and set the “connected” variable back to false, causing the main file’s loop to re-run the code in the if statement blocked off by the if statement !connected. This code is the same as when the user starts the program, or it checks if the server exists and either creates it or connects to it, depending on the result. This effectively starts the program off on a blank slate (except info stored in variables), and we can restart with a new host and all users auto-reconnect.  