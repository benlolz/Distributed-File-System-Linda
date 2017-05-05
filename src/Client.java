import java.io.*;
import java.net.*;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;

public class Client {

	public Packet reply;
	public Queue<Packet> msgQ;
	
	
	public Client() {
		msgQ = new LinkedList<>();
	}

	public void sendTo(String addr, String port, Packet packet) {

		try {
			Socket sock;
			
			ObjectOutputStream out;
			ObjectInputStream in;
			try {
				sock = new Socket(addr, Integer.parseInt(port));
				out = new ObjectOutputStream(sock.getOutputStream());
				out.writeObject(packet);
				out.flush();
				in = new ObjectInputStream(sock.getInputStream());
				
				Packet recPack = null;
				//recPack = (Packet) in.readObject();
				while (recPack == null) {
					recPack = (Packet) in.readObject();
					synchronized (msgQ) {
						if(recPack != null) {
							msgQ.offer(recPack);
							msgQ.notifyAll();
							break;
						}
						
					}

				}
				if (!msgQ.isEmpty()) {
					reply = msgQ.peek();
					
				}
				out.close();
				in.close();
				sock.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println("Connection failed, please check the IP and port number and restart the program P1");
				System.exit(0);
			}
			
			

		} catch (NumberFormatException/* | IOException | ClassNotFoundException */e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void broadcast(Hashtable<Integer, String[]> nets, Packet outPacket) {
		

		
		try {
			int num = nets.size();
			Thread[] workers= new Thread[num];
			//Socket[] socks = new Socket[num];
			
			
			for (int i = 0; i < num; i++) {

				String destAddr = nets.get(i)[1];	//destination IP
				String destPort = nets.get(i)[2];	//destination port
				System.out.println("Sending to destination = " + destAddr + " at port " + destPort);
				Socket sock = new Socket(destAddr, Integer.parseInt(destPort));
				ClientWorker worker = new ClientWorker(outPacket, sock);
				Thread clientWorkerT = new Thread(worker);
				workers[i] = clientWorkerT;
				clientWorkerT.start();

			}
			//Thread.sleep(500);
			
			while (msgQ.isEmpty()) {
				System.out.print("");
				//Thread.sleep(1000);
			}
			
			//System.out.println("Received "+msgQ.size()+ " numbers of replies");
			System.out.println("Type match found and close other blocking connections ");
			for(int i = 0; i < num; i++) {
				workers[i].interrupt();
				//if (socks[i] != null) {
				//	socks[i].close();
				//}
				
			}
			reply = msgQ.peek();
			System.out.println(P1.hostname+"'s Client worker has finished");
			
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	class ClientWorker implements Runnable {

		Packet packet;
		Socket sock;

		public ClientWorker(Packet packet, Socket sock) {

			this.packet = packet;
			this.sock = sock;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub

			try {
				
				Thread.sleep(200);
				ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
				out.writeObject(packet);
				out.flush();

				ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
				//System.out.println("Client worker got reply");
				Packet recPack = null;
				//recPack = (Packet) in.readObject();
				//System.out.println("replied pack = "+recPack);
				
					//while (recPack  == null) {
					if (!msgQ.isEmpty()) {
						//reply = msgQ.peek();
						//System.out.println(Thread.currentThread().getId()+" put message in msgQ"+" and msgQ size = "+msgQ.size());
						
					}
					
					while(msgQ.isEmpty()) {	
						recPack = (Packet) in.readObject();
						msgQ.offer(recPack);
						//System.out.println(Thread.currentThread().getId()+" put message in msgQ"+" and msgQ size = "+msgQ.size());
						
						
					}
					//reply = msgQ.peek();
					
					
					
				
				
				out.close();
				in.close();
				sock.close();
			} catch (ClassNotFoundException | IOException | InterruptedException e)  {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


}


