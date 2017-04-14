import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;


public class Server implements Runnable {

	String port;
	ServerSocket srvSock;

	@Override
	public void run() {
		// TODO Auto-generated method stub

		try {
			
			srvSock = new ServerSocket(0);
			port = Integer.toString(srvSock.getLocalPort());
			P1.port = port;
			System.out.println("Server is starting up...");

			while (true) {
				Socket sock = srvSock.accept();
				ServerWorker worker = new ServerWorker(sock);
				Thread serverWorkerT = new Thread(worker);
				serverWorkerT.start();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}

class ServerWorker implements Runnable {

	Socket sock;
	

	public ServerWorker(Socket sock) {
		this.sock = sock;
	}

	public void run() {
		// TODO Auto-generated method stub

		try {
			
			ObjectInputStream in=new ObjectInputStream(sock.getInputStream());  
			Packet pacRecvd = (Packet) in.readObject();
			
			ObjectOutputStream out=new ObjectOutputStream(sock.getOutputStream());
			
			if (pacRecvd != null) {
				System.out.println(P1.hostname+".Server received: "+P1.h.get(pacRecvd.type));	
				
				if (pacRecvd.type == 0) {									//Handle add request
					if (P1.hostname.equals(pacRecvd.s)) {
						Packet outPacket = new Packet(1, "yes", P1.nets);
						out.writeObject(outPacket);
						out.flush();
					}
					else {
						Packet outPacket = new Packet(1, "no", P1.nets);		//send reply for add
						out.writeObject(outPacket);
						out.flush();
					}
					
				}
				
				else if (pacRecvd.type == 8 && pacRecvd.nets != null) {		//Handle add ack request
					P1.nets = pacRecvd.nets;
					P1.hostCount = P1.nets.size();
					
				/*	System.out.println("The received nets = ");
					for (int i = 0; i < pacRecvd.nets.size(); i++) {
						StringBuilder sb = new StringBuilder();
						sb.append(i + ": ");
						sb.append(pacRecvd.nets.get(i)[0]+", "+ pacRecvd.nets.get(i)[1]+ ", "+pacRecvd.nets.get(i)[2]);
						System.out.println(sb.toString());
					}
				*/
					
					Packet outPacket = new Packet(9);						//send reply for ack
					out.writeObject(outPacket);
					out.flush();
					P1.fileOut = new FileOutputStream(P1.path + "/nets.txt");
					P1.obOut = new ObjectOutputStream(P1.fileOut);
					P1.obOut.writeObject(P1.nets);
					P1.obOut.close();
					System.out.println(P1.hostname+ " replied ack");
					System.out.println(P1.hostname+" has finished add request, now the "
							+ "size of nets is "+P1.hostCount);
					
				}
				
				else if (pacRecvd.type == 2 && pacRecvd.tuple != null) {		// Handle out instruction
					
					List<Object> tuple = pacRecvd.tuple;
					String hashcode = Check.hashString(Check.tupleToString(tuple));
					
					
				//	System.out.println("Received tuple : ");
				//	for (int i = 0; i < length; i++) {
				//		System.out.println(tuple.get(i).toString());
				//	}
					if (P1.tupleSpace.contains(hashcode)) {
						List<HashTableEntry> htList = P1.tupleSpace.get(hashcode);
						HashTableEntry putBack = new HashTableEntry();
						for (int i = 0; i < htList.size(); i++) {
							if (Check.compareTuples(htList.get(i).tuple, tuple)) {
								putBack.tuple = tuple;
								putBack.counts = htList.get(i).counts + 1;
								htList.set(i, putBack);
								P1.tupleSpace.put(hashcode, htList);
								break;
							}
						}
						putBack.tuple = tuple;
						putBack.counts = 1;
						htList.add(putBack);
						P1.tupleSpace.put(hashcode, htList);
					}
					else {
						HashTableEntry putBack = new HashTableEntry();
						List<HashTableEntry> htList = new ArrayList<>();
						putBack.tuple = tuple;
						putBack.counts = 1;
						htList.add(putBack);
						P1.tupleSpace.put(hashcode, htList);
					}
					
					
					
					P1.fileOut = new FileOutputStream(P1.path + "/tuples.txt");
					P1.obOut = new ObjectOutputStream(P1.fileOut);
					P1.obOut.writeObject(P1.tupleSpace);
					System.out.println("Tuple added to the Tuple Space and wrote to "+P1.hostname+"'s local disk");
					
					P1.obOut.close();
					Packet outPacket = new Packet(3, "Tuple added to the destnation host's Tuple Space");	
					out.writeObject(outPacket);								//send reply for out
					out.flush();
				}
				
/*				else if (pacRecvd.type == 10 && pacRecvd.tuple != null) {			//Handle broadcast
					List<Object> tuple = pacRecvd.tuple;
					int length = tuple.size();
					if (P1.tupleSpace.containsKey(length)) {
						Hashtable<List<Object>, Integer> innerHT = P1.tupleSpace.get(length);
						if (innerHT.contains(tuple)) {
							int tmpNum = innerHT.get(tuple) + 1;
							innerHT.put(tuple, tmpNum);
						}
						P1.tupleSpace.put(length, innerHT);
					}
					else {
						Hashtable<List<Object>, Integer> innerHT = new Hashtable<>();
						innerHT.put(tuple, 1);
						P1.tupleSpace.put(length, innerHT);
					}
					P1.fileOut = new FileOutputStream(P1.path + "/tuples.txt");
					P1.obOut = new ObjectOutputStream(P1.fileOut);
					P1.obOut.writeObject(P1.tupleSpace);
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < tuple.size(); i++) {
						sb.append(tuple.get(i).toString()+" ");
					}
					System.out.println("Broadcast instruction received and the tuple: "+sb.toString()+" is wrote to "+P1.hostname+"'s local disk");
					P1.obOut.close();
					
					Packet outPacket = new Packet(11, P1.hostname+" replied the broadcast");
					out.writeObject(outPacket);
					out.flush();
				}
*/				
				else if (pacRecvd.type == 6 && pacRecvd.tuple != null) {				// Handle rd no typeMatch instruction
					List<Object> tuple = pacRecvd.tuple;
					String hashcode = Check.hashString(Check.tupleToString(tuple));
					Packet outPacket = null;
					
					while (true) {
						P1.fileIn = new FileInputStream(P1.path + "/tuples.txt");
						P1.obIn = new ObjectInputStream(P1.fileIn);
						@SuppressWarnings("unchecked")
						Hashtable<String, List<HashTableEntry>> tmpTuples = 
						(Hashtable<String, List<HashTableEntry>>) P1.obIn.readObject();
						
						if (tmpTuples.containsKey(hashcode)) {
							List<HashTableEntry> innerHT = tmpTuples.get(hashcode);
							
							for(HashTableEntry entry: innerHT) {
								if (Check.compareTuples(entry.tuple, tuple)) {
									outPacket = new Packet(7,"Tuple found and send back by"+P1.hostname,entry.tuple);
									break;				//send rd no typeMatch reply
								}
							}
							break;
							
						}
						continue;
					}
					out.writeObject(outPacket);
					out.flush();
					P1.obIn.close();

				}
				
				else if (pacRecvd.type == 10 && pacRecvd.tuple != null) {
					List<Object> tuple = pacRecvd.tuple;
				}
				
				
				
				System.out.print("linda> ");

			}
			
			sock.close();
		} catch (ClassNotFoundException | IOException | NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
