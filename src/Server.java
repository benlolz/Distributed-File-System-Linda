import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
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

	@SuppressWarnings({ })
	public void run() {
		// TODO Auto-generated method stub

		try {

			ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
			Packet pacRecvd = (Packet) in.readObject();

			ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());

			if (pacRecvd != null) {
				System.out.println(P1.hostname + ".Server received: " + P1.h.get(pacRecvd.type));

				if (pacRecvd.type == 0) { // Handle add request
					if (P1.hostname.equals(pacRecvd.s)) {
						Packet outPacket = new Packet(1, "yes", P1.nets);
						out.writeObject(outPacket);
						out.flush();
					} else {
						Packet outPacket = new Packet(1, "no", P1.nets); // send
																			// reply
																			// for
																			// add
						out.writeObject(outPacket);
						out.flush();
					}

				}

				else if (pacRecvd.type == 8 && pacRecvd.nets != null) { // Handle
																		// add
																		// ack
																		// request
					P1.nets = pacRecvd.nets;
					//P1.hostCount = P1.nets.size();

					/*
					 * System.out.println("The received nets = "); for (int i =
					 * 0; i < pacRecvd.nets.size(); i++) { StringBuilder sb =
					 * new StringBuilder(); sb.append(i + ": ");
					 * sb.append(pacRecvd.nets.get(i)[0]+", "+
					 * pacRecvd.nets.get(i)[1]+ ", "+pacRecvd.nets.get(i)[2]);
					 * System.out.println(sb.toString()); }
					 */
					FileOutputStream fileOut;
					ObjectOutputStream obOut;

					Packet outPacket = new Packet(9); // send reply for ack
					out.writeObject(outPacket);
					out.flush();
					fileOut = new FileOutputStream(P1.path + "/nets.txt");
					obOut = new ObjectOutputStream(fileOut);
					obOut.writeObject(P1.nets);
					obOut.close();
					System.out.println(P1.hostname + " replied ack");
					System.out.println(
							P1.hostname + " has finished add request, now the " + "size of nets is " + P1.nets.size());
					System.out.print("linda> ");

				}

				else if (pacRecvd.type == 2 && pacRecvd.tuple != null) { // Handle
																			// out
																			// instruction

					List<Object> tuple = pacRecvd.tuple;
					String hashcode = Check.hashString(Check.tupleToString(tuple));

					// System.out.println("Received tuple : ");
					// for (int i = 0; i < length; i++) {
					// System.out.println(tuple.get(i).toString());
					// }

					Hashtable<String, List<HashTableEntry>> tmpTuples = P1.tupleSpace;
					if (tmpTuples.containsKey(hashcode)) {
						List<HashTableEntry> htList = tmpTuples.get(hashcode);
						HashTableEntry putBack = new HashTableEntry();
						boolean found = false;
						for (int i = 0; i < htList.size(); i++) {
							if (Check.compareTuples(htList.get(i).tuple, tuple)) {
								putBack.tuple = htList.get(i).tuple;
								putBack.counts = htList.get(i).counts + 1;
								htList.set(i, putBack);
								tmpTuples.put(hashcode, htList);
								synchronized (P1.tupleSpace) {
									P1.tupleSpace = tmpTuples;
									P1.tupleSpace.notifyAll();
								}
								
								found = true;
								break;
							}
						}
						if (!found) {
							putBack.tuple = tuple;
							putBack.counts = 1;
							htList.add(putBack);
							tmpTuples.put(hashcode, htList);
							synchronized (P1.tupleSpace) {
								P1.tupleSpace = tmpTuples;
								P1.tupleSpace.notifyAll();
							}
							
						}
						

					} else {
						HashTableEntry putBack = new HashTableEntry();
						List<HashTableEntry> htList = new ArrayList<>();
						putBack.tuple = tuple;
						putBack.counts = 1;
						htList.add(putBack);
						tmpTuples.put(hashcode, htList);
						synchronized (P1.tupleSpace) {
							P1.tupleSpace = tmpTuples;
							P1.tupleSpace.notifyAll();
						}
						

					}

					FileOutputStream fileOut;
					ObjectOutputStream obOut;

					fileOut = new FileOutputStream(P1.path + "/tuples.txt");
					obOut = new ObjectOutputStream(fileOut);
					obOut.writeObject(P1.tupleSpace);
					System.out.println("Tuple added to the Tuple Space and wrote to " 
					+ P1.hostname + "'s local disk and the count = "+ Check.checkTupleCount(tuple, P1.tupleSpace));

					Packet outPacket = new Packet(3, "Tuple added to the destnation host's Tuple Space");

					fileOut.close();
					obOut.close();
					out.writeObject(outPacket); // send reply for out
					out.flush();
					System.out.print("linda> ");
				}

				else if (pacRecvd.type == 6 && pacRecvd.tuple != null) { // Handle
																			// rd
																			// w/o
																			// typeMatch
																			// instruction
					List<Object> tuple = pacRecvd.tuple;
					String hashcode = Check.hashString(Check.tupleToString(tuple));
					// System.out.println("Received hashcode = "+hashcode);
					Packet outPacket = null;

					while (outPacket == null) {
						

						Hashtable<String, List<HashTableEntry>> tmpTuples = P1.tupleSpace;

						if (tmpTuples.containsKey(hashcode)) {

							List<HashTableEntry> entryList = tmpTuples.get(hashcode);

							for (HashTableEntry entry : entryList) {
								if (Check.compareTuples(entry.tuple, tuple)) {
									outPacket = new Packet(7, "Tuple found and send back by " + P1.hostname,
											entry.tuple);
									// System.out.println("Reached here in
									// rd Server before break");

									break; // send rd no typeMatch reply
								}
							}
						}

					}
					System.out.println(P1.hostname + " finished rd request and tuple is sent back");

					out.writeObject(outPacket);
					out.flush();
					System.out.print("linda> ");

				}

				else if (pacRecvd.type == 10 && pacRecvd.tuple != null) { // Handle
																			// incoming
																			// rd
																			// typeMatch
																			// request
					List<Object> tuple = pacRecvd.tuple;
					//System.out.println("Checking matches in TupleSpace...");
					List<Object> returnTuple;
					Packet outPacket = null;
					while (outPacket == null) {
						synchronized(P1.tupleSpace) {
							Hashtable<String, List<HashTableEntry>> tmpTuples = P1.tupleSpace;
							P1.tupleSpace.notifyAll();
							returnTuple = Check.getTuple(tuple, tmpTuples);
						}
						
						
						if (returnTuple != null) {
							outPacket = new Packet(11, P1.hostname, returnTuple);
							//System.out.println(P1.hostname+".Server send out Packet(11)");
						}

					}
					System.out.println("Tuple found on " + P1.hostname + " and send back");
					out.writeObject(outPacket);
					out.flush();
					System.out.print("linda> ");
				}

				else if (pacRecvd.type == 4 && pacRecvd.tuple != null) { // Handle
																			// in
																			// instruction
																			// w/o
																			// typeMatch
					if (pacRecvd.s.equals("1st")) {
						List<Object> tuple = pacRecvd.tuple;
						String hashcode = Check.hashString(Check.tupleToString(tuple));
						// System.out.println("Received in hashcode =
						// "+hashcode);
						Packet outPacket = null;

						while (outPacket == null) {
							
							Hashtable<String, List<HashTableEntry>> tmpTuples = P1.tupleSpace;
							if (tmpTuples.containsKey(hashcode)) {

								List<HashTableEntry> entryList = tmpTuples.get(hashcode);

								for (HashTableEntry entry : entryList) {
									if (Check.compareTuples(entry.tuple, tuple)) {
										outPacket = new Packet(5, "Tuple found and send back by " 
									+ P1.hostname, entry.tuple);
									System.out.println(P1.hostname + " found the tuple and count = "
											+Check.checkTupleCount(tuple, tmpTuples)+". The tuple is sent back");
										break; // send in no typeMatch reply
									}
								}
								if (outPacket != null) {
									break;
								}
							}
							
						}
						
						out.writeObject(outPacket);
						out.flush();
					} else if (pacRecvd.s.equals("2nd")) {
						List<Object> tuple = pacRecvd.tuple;
						String hashcode = Check.hashString(Check.tupleToString(tuple));
						boolean deleted = false;

						while (!deleted) {

							FileOutputStream fileOut;
							ObjectOutputStream obOut;

							Hashtable<String, List<HashTableEntry>> tmpTuples = P1.tupleSpace;
							if (tmpTuples.containsKey(hashcode)) {

								List<HashTableEntry> entryList = tmpTuples.get(hashcode);

								for (int i = 0; i < entryList.size(); i++) {
									HashTableEntry entry = entryList.get(i);
									if (Check.compareTuples(entry.tuple, tuple)) {
										if (entry.counts > 1) {
											entry.counts -= 1;
											entryList.set(i, entry);
											tmpTuples.put(hashcode, entryList);
										} else if (entry.counts == 1) {
											entryList.remove(i);
											tmpTuples.put(hashcode, entryList);
										}
										deleted = true;
										break;
									}
								}

								if (deleted) {
									synchronized (P1.tupleSpace) {
										P1.tupleSpace = tmpTuples;
										P1.tupleSpace.notifyAll();
									}

									fileOut = new FileOutputStream(P1.path + "/tuples.txt");
									obOut = new ObjectOutputStream(fileOut);
									obOut.writeObject(P1.tupleSpace);
									fileOut.close();
									obOut.close();

								}

							}
							System.out.println("Tuple removed from " + P1.hostname + "'s Tuple Space and the count =" +
							Check.checkTupleCount(tuple, P1.tupleSpace));
							Packet outPacket = new Packet(5, "Tuple deleted confirmation on " + P1.hostname);
							out.writeObject(outPacket);
							out.flush();
						}

					} else {
						System.out.println("Unkown packet for in instruction w/o type match ");
					}

					System.out.print("linda> ");
				}

				else if (pacRecvd.type == 12 && pacRecvd.tuple != null) { // Handle
																			// incoming
																			// in
																			// typeMatch
																			// request
					List<Object> tuple = pacRecvd.tuple;
					//System.out.println("Checking matches in TupleSpace...");
					List<Object> returnTuple;
					Packet outPacket = null;
					while (outPacket == null) {
						returnTuple = Check.getTuple(tuple, P1.tupleSpace);
						if (returnTuple != null) {
							outPacket = new Packet(13, P1.hostname, returnTuple);
						}
					}
					System.out.println("Tuple found on " + P1.hostname + " and send back");
					out.writeObject(outPacket);
					out.flush();
					System.out.print("linda> ");
				}

				else if (pacRecvd.type == 14 && pacRecvd.tuple != null) {
					List<Object> tuple = pacRecvd.tuple;
					String hashcode = Check.hashString(Check.tupleToString(tuple));
					boolean deleted = false;

					while (!deleted) {

						FileOutputStream fileOut;
						ObjectOutputStream obOut;

						Hashtable<String, List<HashTableEntry>> tmpTuples = P1.tupleSpace;
						if (tmpTuples.containsKey(hashcode)) {
							List<HashTableEntry> entryList = tmpTuples.get(hashcode);

							for (int i = 0; i < entryList.size(); i++) {
								HashTableEntry entry = entryList.get(i);
								if (Check.compareTuples(entry.tuple, tuple)) {
									if (entry.counts > 1) {
										entry.counts -= 1;
										entryList.set(i, entry);
										tmpTuples.put(hashcode, entryList);
									} else if (entry.counts == 1) {
										entryList.remove(i);
										tmpTuples.put(hashcode, entryList);
									}
									deleted = true;
									break;
								}
							}

							if (deleted) {
								synchronized (P1.tupleSpace) {
									P1.tupleSpace = tmpTuples;
									P1.tupleSpace.notifyAll();
								}

								fileOut = new FileOutputStream(P1.path + "/tuples.txt");
								obOut = new ObjectOutputStream(fileOut);
								obOut.writeObject(P1.tupleSpace);
								obOut.close();

							}

						}
						System.out.println("Tuple removed from " + P1.hostname + "'s Tuple Space");
						Packet outPacket = new Packet(15, "Tuple deleted confirmation on " + P1.hostname);
						out.writeObject(outPacket);
						out.flush();

					}
					System.out.print("linda> ");
				}

				else {
					System.out.println(P1.hostname + ".Server received unknown command");
					System.out.print("linda> ");
				}

				in.close();
				out.close();

				// System.out.print("linda> ");

			}

			sock.close();
		} catch (ClassNotFoundException | IOException |

				NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
