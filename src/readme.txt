Name: Pengyu Zhu
Student ID:	1118641
Email: pzhu@scu.edu
Directory: /tmp/pzhu/linda/<hostname>/


Running instructions:
1. 	Run "java P1 hostname" on each machine you want to connect to the network.
	Please note that hostname cannot be empty and cannot start with numbers.

2.	On one machine, using add command to connect different machines. 
	Syntax: add (host2, 129.210.16.80, 23456) (host3, 129.210.16.81, 34567)
	Please note the destination hostname here can be different from the one you used to start P1 on it.
	The program will check the if hostname of destination matches the one in add command. 
	If not, the destination machine will send the correct hostname to the master and asks the server to correct it.
	For add command, if any one of the machine fails to connect, the program will be forced to quit. You need to 
	re-launch the P1 on each machine and do the add again.

3. 	After step 2, the machines are connected together. You can use any of rd, in, out command on any machine.

4. 	Example for out command: out ("ABC", 123, 4.5)
	Example for rd command: rd ("ABC", 123, 4.5) or rd ("ABC", ?i:int, 4.5) for type match broadcasting
	Example for in command: in ("ABC", 123, 4.5) or in ("ABC", ?i:int, 4.5) for type match broadcasting

5.	The program can check the syntax of commands to see if they are correct. Notification will be given if any
	command is invalid. You need to retype the correct syntax for the command.
	


Modular design:
	The application basically contains 5 java file. 
P1: P1 is the main entrance and serves as the terminal for linda application. P1 identifies different commands 
	and parse the command. 
Client: Client.java is separated out from P1.java and servers as the message sender and receiver. It only serves 
	these two functions. 
Server:	Server.java serves as the back end server for linda application. It receives messages from CLient side and 
	act accordingly for different command and sends back the result. 
Check: Check.java contains all utility functions.
Packet: Packet.java serves as the message packet between Server and Client. All the messages passed in linda are in
	the format of packet. Also, the id of the packet tells the Server what type of command it is.


File/Directory organization:
	The nets.txt and tuples.txt are under /tmp/pzhu/linda/<hostname>/ directory. The nets.txt contains all the connected
machines' hostnames, IP addresses and ports. The tuples.txt serves as the Tuple Space. Every machine manages its own 
tuples.