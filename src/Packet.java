import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;

public class Packet implements Serializable {
	/*
	 * type: 
	 * 0: incoming add instruction 				Packet(0, hostname sent from master); 
	 * 1: outgoing add instruction 				Packet(1, "yes/no", nets); 
	 * 2: incoming out instruction 				Packet(2, tuple);
	 * 3: outgoing out instruction 				Packet(3, "Tuple added to the Tuple Space");
	 * 4: incoming in w/o typeMatch inst 		Packet(4, "1st/2nd", tuple)
	 * 5: outgoing in w/o typeMatch inst 		Packet(5, "Tuple found on P1.hostname, tuple")
	 * 6: incoming rd w/o typeMatch inst		Packet(6, tuple);
	 * 7: outgoing rd w/o typeMatch inst 		Packet(7, "Tuple found and send back by"+P1.hostname, tuple);
	 * 8: ack request 							Packet(8, "ack", nets); 
	 * 9: ack sent 								Packet(9);
	 * 10: incoming rd typeMatch instruction	Packet(10, nets, tuple);
	 * 11: outgoing rd typeMatch inst reply		Packet(11, s, tuple);
	 * 12: incoming in typeMatch instruction	Packet(12, nets, tuple);
	 * 13: outgoing in typeMatch inst reply		Packet(13, s, tuple);
	 * 14: incoming in typeMatch delete ack		Packet(14, tuple)
	 * 15: outgoing in typeMatch delete ack		Packet(15, s)

	 */

	/**
	 * 
	 */
	private static final long serialVersionUID = 912922114076434640L;
	/**
	 * 
	 */
	
	int type;
	Hashtable<Integer, String[]> nets;
	List<Object> tuple;
	String s;
	Hashtable<Integer, String> h;

	public Packet() {

	}

	public Packet(int type) {
		this.type = type;

	}

	public Packet(int type, String s) {
		this.type = type;
		this.s = s;

	}

	public Packet(int type, String s, Hashtable<Integer, String[]> nets) {
		this.type = type;
		this.s = s;
		this.nets = nets;
	}

	public Packet(int type, List<Object> tuple) {
		this.type = type;
		this.tuple = tuple;
	}
	
	public Packet(int type, String s, List<Object> tuple) {
		this.type = type;
		this.s = s;
		this.tuple = tuple;
	}
	
	public Packet(int type, Hashtable<Integer, String[]> nets, List<Object> tuple) {
		this.type = type;
		this.nets = nets;
		this.tuple = tuple;
	}
	
	public Packet(int type, String s, Hashtable<Integer, String[]> nets, List<Object> tuple) {
		this.type = type;
		this.s = s;
		this.nets = nets;
		this.tuple = tuple;
	}
}
