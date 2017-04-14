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
			Socket sock = new Socket(addr, Integer.parseInt(port));

			ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
			out.writeObject(packet);
			out.flush();
			ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
			Packet recPack;
			recPack = (Packet) in.readObject();
			if (recPack != null) {
				synchronized (msgQ) {
					msgQ.offer(recPack);
					msgQ.notifyAll();
				}

			}
			if (msgQ.size() != 0) {
				reply = msgQ.poll();
			}
			
			sock.close();

		} catch (NumberFormatException | IOException | ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void broadcast(Hashtable<Integer, String[]> nets, Packet outPacket) {
		

		
		try {
			int num = nets.size();
			for (int i = 0; i < num; i++) {

				String destAddr = nets.get(i)[1];
				String destPort = nets.get(i)[2];
				System.out.println("Sending to NO." + i + " destination = " + destAddr + " at port " + destPort);
				Socket sock = new Socket(destAddr, Integer.parseInt(destPort));
				ClientWorker worker = new ClientWorker(outPacket, sock);
				Thread clientWorkerT = new Thread(worker);
				clientWorkerT.start();

			}
			while (msgQ != null || msgQ.size() != 0) {
				reply = msgQ.poll();
				break;
			}

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
				ObjectOutputStream out = new ObjectOutputStream(sock.getOutputStream());
				out.writeObject(packet);
				out.flush();

				ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
				Packet recPack;
				recPack = (Packet) in.readObject();

				if (recPack != null) {
					synchronized (msgQ) {
						msgQ.offer(recPack);
						msgQ.notifyAll();
					}
					// System.out.println("Client sock close");
				}
				out.close();
				in.close();
				sock.close();
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


}

