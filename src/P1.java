import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class P1 {

	static int hostCount;
	static String hostname;
	static String port;
	static Hashtable<Integer, String[]> nets;		//[hostname, host IP, host port]
	static Hashtable<String, List<HashTableEntry>> tupleSpace;
	static String path;
	static Packet rpMsg;
	static FileOutputStream fileOut;
	static ObjectOutputStream obOut;
	static FileInputStream fileIn;
	static ObjectInputStream obIn;
	static List<String[]> addCmdList;
	static List<Object> tuple; 
	static Hashtable<Integer, String> h;

	Server myServer;

	public P1() throws Exception {
		Server myServer = new Server();
		Thread serverT = new Thread(myServer);
		serverT.start();
		nets = new Hashtable<>();
		tupleSpace = new Hashtable<>();
		h = new Hashtable<>();
		h.put(0, "Incoming add request for hostname validation");
		h.put(1, "Outgoing reply message for add request");
		h.put(2, "Incoming out request");
		h.put(3, "Outgoing reply message for out request");
		h.put(4, "Incoming in request");
		h.put(5, "Outgoing reply message for in request");
		h.put(6, "Incoming rd request");
		h.put(7, "Outgoing reply message for rd request");
		h.put(8, "Ack request");
		h.put(9, "Ack sent");
		h.put(10, "Incoming rd boardcast instruction");
		h.put(11, "Outgoing rd reply message of boardcast");
		h.put(12, "Incoming in boardcast instruction");
		h.put(13, "Outgoing in reply message of boardcast");

	}

	public static void main(String[] args) throws Exception {

		if (args == null || args.length != 1 || args[0].matches("\\s") || !args[0].matches("^[a-zA-Z][a-zA-Z0-9]*$")) {
			System.out.println("Invalid command, check the hostname, hostname cannot start with a number and cannot use any special characters");
			return;
		}

		P1 myP1 = new P1();
		hostname = args[0];
		path = "/tmp/pzhu/linda/" + hostname;

		new File(path).mkdirs();
		new File(path + "/nets.txt").createNewFile();
		new File(path + "/tuples.txt").createNewFile();
		String localAddr = InetAddress.getLocalHost().getHostAddress().toString();
		System.out.println(localAddr + " at port number: " + port);

		nets.put(0, new String[] { hostname, localAddr, port });
		hostCount = 1;
		
		

		while (true) {

			System.out.print("linda> ");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			String cmd = br.readLine().trim();
			addCmdList = new ArrayList<>();
			tuple = new ArrayList<>();

			if (cmd == null || cmd.length() < 3) {
				System.out.println("Invalid command 1");

			}

			else if (cmd.length() > 3 && cmd.substring(0, 3).toLowerCase().equals("add")) {			//Handle add
				cmd = cmd.substring(3).trim();

				if (Check.checkAddCmd(cmd) == false) {
					continue;
				}
				if (addCmdList != null || addCmdList.size() != 0) {
					Packet[] pacsRecvd = new Packet[addCmdList.size()];

					Hashtable<Integer, String[]> updateNets = nets;
					for (int i = 0; i < addCmdList.size(); i++) {
						String[] recp = addCmdList.get(i);
						// System.out.println("recp = "+Arrays.toString(recp));
						Packet myPacket = new Packet(0, recp[0]);					//send add instruction
						Client addClient = new Client();
						addClient.sendTo(recp[1], recp[2], myPacket);
						pacsRecvd[i] = addClient.reply;
						if (pacsRecvd[i] == null) {
							System.out.println("No reply received");
							continue;
						}
						if (pacsRecvd[i].type == 1 && pacsRecvd[i].s.equals("yes")) {		//receive add replies
							Hashtable<Integer, String[]> recvdNets = pacsRecvd[i].nets;
							Check.mergeNets(updateNets, recvdNets);
						} else if (pacsRecvd[i].s.equals("no")) {
							System.out.println("Invalid hostname, correct hostname " + pacsRecvd[i].nets.get(0)[0]
									+ " is fetched and updated!");
							addCmdList.set(i, new String[] { pacsRecvd[i].nets.get(0)[0], recp[1], recp[2] });

							Hashtable<Integer, String[]> recvdNets = pacsRecvd[i].nets;
							Check.mergeNets(updateNets, recvdNets);
						} else {
						}

					}
					System.out.println("All peers informed for nets update, prepare to commit changes");
					for (int i = 0; i < addCmdList.size(); i++) {
						String[] recp = addCmdList.get(i);
						Packet myPacket = new Packet(8, "ack", updateNets);
						Client addClient = new Client();
						addClient.sendTo(recp[1], recp[2], myPacket);		//send ack request
						pacsRecvd[i]=addClient.reply;
						
					}
					int ackCount = 0;
					for (int i = 0; i < pacsRecvd.length; i++) {
						if (pacsRecvd[i].type == 9) {						//receive acks reply
							ackCount++;
						}
					}
					if (ackCount == addCmdList.size()) {
						nets = updateNets;
						hostCount = nets.size();
						fileOut = new FileOutputStream(path + "/nets.txt");
						obOut = new ObjectOutputStream(fileOut);
						obOut.writeObject(nets);
						obOut.close();
						System.out.println("Add instruction complete! nets file wrote to "+hostname+"'s local disk");
						System.out.println(P1.hostname + "'s nets size = " + nets.size());
					} else {
						System.out.println("Something went wrong, add instruction is not complete");
					}
				}

			}

			else if (cmd.length() > 3 && cmd.substring(0, 3).toLowerCase().equals("out")) {			//Handle out
				
				cmd = cmd.substring(3).trim();

				if (Check.checkOutCmd(cmd) == false) {
					continue;
				}
				
				
				if (tuple != null || tuple.size() != 0) {
					
					Packet[] pacsRecvd = new Packet[1];
					int host = Check.md5Sum(tuple) % hostCount;
					String destAddr = nets.get(host)[1];
					String destPort = nets.get(host)[2];
					Packet myPacket = new Packet(2, tuple);						//send out instruction
					System.out.println("Put tuple "+cmd+" on "+destAddr+":"+destPort);
					
					Client outClient = new Client();
					outClient.sendTo(destAddr, destPort, myPacket);
					pacsRecvd[0]=outClient.reply;
					if (pacsRecvd[0].type == 3) {								//receive out reply
						System.out.println(pacsRecvd[0].s);
					}
					
				}else {
					System.out.println("Something went wrong, out instruction is not complete");
				}
			}
			
/*			else if (cmd.length() > 4 && cmd.substring(0,5).toLowerCase().equals("broad")) {			//Handle broad
				List<Object> t = new ArrayList<>();
				t.add("this");
				t.add(1200);
				t.add(3.5);
				Client broadClient = new Client();
				broadClient.broadcast(nets, t);
				Packet[] pacsRecvd = new Packet[nets.size()];
				
				int replyNum = 0;
				while (replyNum != nets.size()) {
					
					pacsRecvd[0]=broadClient.msgQ.poll();
					
					if (pacsRecvd[0]!=null && pacsRecvd[0].type == 11) {
						
						System.out.println(pacsRecvd[0].s);
						replyNum++;
					}
				}
				
			}
*/			
			else if (cmd.trim().length() > 2 && cmd.trim().substring(0, 2).toLowerCase().equals("rd")) { 	//Handle rd
				cmd = cmd.trim().substring(2);
				if (Check.checkRdCmd(cmd) == false) {
					continue;
				}
				
				boolean broadcast = false;
				if (tuple != null || tuple.size() != 0) {	
					Packet[] pacsRecvd = new Packet[1];
					
					for (Object obj : tuple) {
						if (obj instanceof String[]) {
							broadcast = true;
							break;
							
							
						}
					}
					if (!broadcast) {
						int host = Check.md5Sum(tuple) % hostCount;
						String destAddr = nets.get(host)[1];
						String destPort = nets.get(host)[2];
						System.out.println("Get tuple "+cmd+" from "+ destAddr+":"+destPort);
						
						
						
						Packet myPacket = new Packet(6, tuple);						//send rd no typeMatch inst
						
						Client rdClient = new Client();
						rdClient.sendTo(destAddr, destPort, myPacket);
						
						
						//pacsRecvd[0]=Client.reply;
						while (pacsRecvd[0] == null) {
							pacsRecvd[0]=rdClient.reply;
							if (pacsRecvd[0] != null && pacsRecvd[0].type == 7) {	//receive rd no typeMatch reply
								System.out.println(pacsRecvd[0].s);
								break;
							}
						}
						
					}
					else {
						Client rdbroadClient = new Client();
						Packet outPacket = new Packet(10, nets, tuple);		//send rd typeMatch broadcast request
						rdbroadClient.broadcast(nets, outPacket);
						
						while (pacsRecvd[0] == null) {
							pacsRecvd[0]=rdbroadClient.reply;
							if (pacsRecvd[0] != null && pacsRecvd[0].type == 11) {	//receive rd no typeMatch reply
								System.out.println("Received matched tuple from "+pacsRecvd[0].s);
								System.out.println("tuple = " + Check.displayTuple(pacsRecvd[0].tuple));
								break;
							}
						}
					}
					
					
				}else {
					System.out.println("Something went wrong, rd instruction is not complete");
				}
			}

			else if (cmd.trim().length() > 4 && cmd.substring(0, 4).toLowerCase().equals("exit")) {
				System.out.println("Exit Linda. Goodbye!");
				System.exit(0);
			}

			else {
				System.out.println("Invalid command");

			}

		}

	}

}	