import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class P1 {

	//static int hostCount;
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
		h.put(14, "Incoming in typeMatch delete ack");
		h.put(15, "Outcoming in typeMatch delete ack");

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
		
		if (new File("/tmp/pzhu/").setReadable(true, false) 
				&& new File("/tmp/pzhu/").setWritable(true, false) 
				&& new File("/tmp/pzhu/").setExecutable(true, false)) {
            System.out.println("P1: successfully changed the directory " + "/tmp/pzhu" + " to 777");
        } else {
            System.out.println("P1: failed to change the directory " + "/tmp/pzhu" + " to 777");
        }
		
		if (new File("/tmp/pzhu/linda/").setReadable(true, false) 
				&& new File("/tmp/pzhu/linda/").setWritable(true, false) 
				&& new File("/tmp/pzhu/linda/").setExecutable(true, false)) {
            System.out.println("P1: successfully changed the directory " + "/tmp/pzhu/linda/" + " to 777");
        } else {
            System.out.println("P1: failed to change the directory " + "/tmp/pzhu/linda/" + " to 777");
        }
		
		if (new File("/tmp/pzhu/linda/"+hostname+"/").setReadable(true, false) 
				&& new File("/tmp/pzhu/linda/"+hostname+"/").setWritable(true, false) 
				&& new File("/tmp/pzhu/linda/"+hostname+"/").setExecutable(true, false)) {
            System.out.println("P1: successfully changed the directory " + "/tmp/pzhu/linda/"+hostname+"/" + " to 777");
        } else {
            System.out.println("P1: failed to change the directory " + "/tmp/pzhu/linda/"+hostname+"/" + " to 777");
        }
		
		if (new File("/tmp/pzhu/linda/"+hostname+"/nets.txt").setReadable(true, false) 
				&& new File("/tmp/pzhu/linda/"+hostname+"/nets.txt").setWritable(true, false)) {
            System.out.println("P1: successfully changed the file " + "/tmp/pzhu/linda/"+hostname+"/nets.txt" + " to 666");
        } else {
            System.out.println("P1: failed change the file " + "/tmp/pzhu/linda/"+hostname+"/nets.txt" + " to 666");
        }
		
		if (new File("/tmp/pzhu/linda/"+hostname+"/tuples.txt").setReadable(true, false) 
				&& new File("/tmp/pzhu/linda/"+hostname+"/tuples.txt").setWritable(true, false)) {
            System.out.println("P1: successfully changed the file " + "/tmp/pzhu/linda/"+hostname+"/tuples.txt" + " to 666");
        } else {
            System.out.println("P1: failed change the file " + "/tmp/pzhu/linda/"+hostname+"/tuples.txt" + " to 666");
        }

		nets.put(0, new String[] { hostname, localAddr, port });
		//hostCount = 1;
		
		fileOut = new FileOutputStream(P1.path + "/tuples.txt");
		obOut = new ObjectOutputStream(fileOut);
		obOut.writeObject(P1.tupleSpace);
		obOut.close();

		while (true) {
			Thread.sleep(50);
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
							System.out.println("Invalid hostname of "+recp[0]+", correct hostname of " + pacsRecvd[i].nets.get(0)[0]
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
						//hostCount = nets.size();
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
					int host = Check.md5Sum(tuple) % nets.size();
					String destAddr = nets.get(host)[1];
					String destPort = nets.get(host)[2];
					Packet myPacket = new Packet(2, tuple);						//send out instruction
					System.out.println("Try to put tuple "+cmd+" on "+destAddr+":"+destPort);
					
					Client outClient = new Client();
					outClient.sendTo(destAddr, destPort, myPacket);
					pacsRecvd[0]=outClient.reply;
					if (pacsRecvd[0].type == 3) {								//receive out reply
						System.out.println(pacsRecvd[0].s);
						System.out.println("OUT instruction complete, put tuple "+cmd+" on "+destAddr+":"+destPort);
					}
					
				}else {
					System.out.println("Something went wrong, out instruction is not complete");
				}
			}
			
	
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
						int host = Check.md5Sum(tuple) % nets.size();
						String destAddr = nets.get(host)[1];
						String destPort = nets.get(host)[2];
						System.out.println("Get tuple "+cmd+" from "+ destAddr+":"+destPort);
						//System.out.println("sent out(rd) hash ="+Check.hashString(Check.tupleToString(tuple)));
						
						
						Packet myPacket = new Packet(6, tuple);						//send rd no typeMatch inst
						
						Client rdClient = new Client();
						rdClient.sendTo(destAddr, destPort, myPacket);
						
						while (pacsRecvd[0] == null) {
							pacsRecvd[0]=rdClient.reply;
							if (pacsRecvd[0] != null && pacsRecvd[0].type == 7) {	//receive rd no typeMatch reply
								System.out.println(pacsRecvd[0].s);
								System.out.println("Tuple = "+Check.displayTuple(pacsRecvd[0].tuple));
								break;
							}
						}
						
					}
					else {
						Client rdbroadClient = new Client();
						Packet outPacket = new Packet(10, nets, tuple);		//send rd typeMatch broadcast request
						rdbroadClient.broadcast(nets, outPacket);
						Thread.sleep(200);
						if ((pacsRecvd[0]=rdbroadClient.reply) != null) {
							System.out.println("Received matched tuple from "+pacsRecvd[0].s);
							System.out.println("tuple = " + Check.displayTuple(pacsRecvd[0].tuple));
						}
						else {
							while ((pacsRecvd[0]=rdbroadClient.reply) == null) {
								//System.out.println("in side while: receive rd type match reply");
								if (pacsRecvd[0] != null && pacsRecvd[0].type == 11) {	//receive rd typeMatch reply
									//System.out.println("in side if: receive rd type match reply");
									System.out.println("Received matched tuple from "+pacsRecvd[0].s);
									System.out.println("Tuple = " + Check.displayTuple(pacsRecvd[0].tuple));
									break;
								}
							}
						}
						
						
						System.out.println("rd typematch finished");
						
					}
					
					
				}else {
					System.out.println("Something went wrong, rd instruction is not complete");		
				}
			}
			
			else if (cmd.trim().length() > 2 && cmd.trim().substring(0, 2).toLowerCase().equals("in")) {	//Handle in
				cmd = cmd.trim().substring(2);
				tuple = null;
				if (Check.checkRdCmd(cmd) == false) {		//Use check parser to perform in instruction
					continue;
				}
				
				boolean broadcast = false;
				if (tuple != null || tuple.size() != 0) {	
					Packet[] pacsRecvd;
					
					for (Object obj : tuple) {
						if (obj instanceof String[]) {
							broadcast = true;
							break;

						}
					}
					if (!broadcast) {
						pacsRecvd = new Packet[2];				//1:1st communication. 2: 2nd communication
						int host = Check.md5Sum(tuple) % nets.size();
						String destAddr = nets.get(host)[1];
						String destPort = nets.get(host)[2];
						System.out.println("Try to get tuple "+cmd+" from "+ destAddr+":"+destPort);
						//System.out.println("sent out(in) hash ="+Check.hashString(Check.tupleToString(tuple)));
						
						
						Packet outPacket1 = new Packet(4, "1st", tuple);						//send in no typeMatch inst
						
						Client inClient1 = new Client();
						inClient1.sendTo(destAddr, destPort, outPacket1);
						
						pacsRecvd[0] = null;
						while (pacsRecvd[0] == null) {
							pacsRecvd[0]=inClient1.reply;
							if (pacsRecvd[0] != null && pacsRecvd[0].type == 5) {	//receive in w/o typeMatch reply
								System.out.println(pacsRecvd[0].s);
								break;
							}
						}
						List<Object> recTuple = pacsRecvd[0].tuple;
						
						Packet outPacket2 = new Packet(4, "2nd", recTuple);
						Client inClient2 = new Client();
						inClient2.sendTo(destAddr, destPort, outPacket2);
						
						pacsRecvd[1] = null;
						while (pacsRecvd[1] == null) {
							pacsRecvd[1]=inClient2.reply;
							if (pacsRecvd[1] != null && pacsRecvd[1].type == 5) {	//receive in w/o typeMatch reply
								System.out.println(pacsRecvd[1].s);
								System.out.println("IN instruction complete, get tuple from "+ destAddr +": "+ destPort);
								System.out.println("Tuple = "+ Check.displayTuple(recTuple));
								break;
							}
						}
						
					}
					else {
						pacsRecvd = new Packet[2];
						Client inbroadClient1 = new Client();
						Packet outPacket1 = new Packet(12, nets, tuple);		//send in typeMatch broadcast request
						inbroadClient1.broadcast(nets, outPacket1);
						
						while ((pacsRecvd[0]=inbroadClient1.reply) == null) {
							
							if (pacsRecvd[0] != null && pacsRecvd[0].type == 13) {	//receive in no typeMatch reply
								System.out.println("Received matched tuple from "+pacsRecvd[0].s);
								System.out.println("Tuple = " + Check.displayTuple(pacsRecvd[0].tuple));
								
								break;
								
							}
						}
						
						String destAddr = null;
						String destPort = null;
						
						for (int i = 0; i < P1.nets.size(); i++) {
							String[] array = P1.nets.get(i);
							if (array[1].equals(pacsRecvd[0].s)) {
								destAddr = array[1];
								destPort = array[2];
							}
						}
						
						if (destAddr != null && destPort != null) {
							Client inbroadClient2 = new Client();
							Packet outPacket2 = new Packet(14, pacsRecvd[0].tuple);
							inbroadClient2.sendTo(destAddr, destPort, outPacket2);
							while (pacsRecvd[1] == null) {
								pacsRecvd[1]=inbroadClient2.reply;
								if (pacsRecvd[1] != null && pacsRecvd[1].type == 15) {	//receive in w/o typeMatch reply
									System.out.println(pacsRecvd[1].s);
									break;
								}
							}
						}
						
						System.out.println("in typematch broadcast finished");
						System.out.println("linda> ");
						
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