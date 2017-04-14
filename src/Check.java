import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

public class Check {
	
	public static String[] typeMatch;  //typeMatch[0] = variable name; typeMatch[1] = type;
	
	public static boolean checkIP(String s) {

		if (s.length() < 4)
			return false;
		if (s.charAt(0) == '.')
			return false;
		if (s.substring(0, 3).matches("0*"))
			return false;
		if (s.replaceAll("[^\\.]", "").length() != 3)
			return false;
		if (s.substring(s.length() - 3, s.length()).matches("255"))
			return false;
		String[] array = s.split("\\.");
		// System.out.println("array = "+Arrays.toString(array));
		for (String t : array) {
			if (!t.trim().matches("[0-9]*")) {
				return false;
			}
			if (Integer.parseInt(t) > 255 || Integer.parseInt(t) < 0) {
				return false;
			}
		}
		return true;
	}

	public static boolean checkPort(String s) {
		if (!s.matches("[0-9]*$"))
			return false;
		if (Integer.parseInt(s) >= 0 && Integer.parseInt(s) <= 1023)
			return false;
		if (Integer.parseInt(s) > 65536) {
			return false;
		}
		return true;
	}

	

	public static boolean checkAddCmd(String cmd) {
		int count = 0;
		// System.out.println("cmd = "+cmd+" and its size is "+cmd.length());
		if (cmd == null || cmd.length() == 0 || !cmd.matches("\\(.*\\).*")) {
			System.out.println("Invalid command 2");
			return false;

		}

		List<String> strList = new ArrayList<>();

		int length = cmd.trim().length();
		for (int i = 0; i < length; i++) {
			// System.out.println("index = "+i+", character = "+cmd.charAt(i));
			if (i < length && cmd.charAt(i) != '(') {
				continue;
			} else {
				if (i >= length) {
					return false;
				}

				i++;
				int start = i;
				while (i < length && cmd.charAt(i) != ')') {
					i++;
				}
				if (i < length) {
					int end = i;
					strList.add(cmd.substring(start, end));
					count++;

				} else {
					System.out.println("Invalid command 6.");
					count++;
					continue;
				}

			}

		}

		if (count != strList.size()) {
			return false;
		}
		
		P1.addCmdList = new ArrayList<>();

		for (int i = 0; i < strList.size(); i++) {

			String str = strList.get(i);
			if (str.length() > 2) {
				String tmp = str.trim().substring(0, str.trim().length());
				// System.out.println("tmp = "+tmp);
				String[] tmpArray = tmp.split(",");
				// System.out.println("tmpArray = "+Arrays.toString(tmpArray));

				if (tmpArray == null || tmpArray.length != 3) {
					System.out.println("Invalid command 4");
					return false;
				}
				String destName = tmpArray[0].trim();
				// System.out.println("destName = "+destName);
				String destAddr = tmpArray[1].trim();
				if (!checkIP(destAddr)) {
					System.out.println("Invalid IP address");
					return false;
				}
				// System.out.println("destAddr = "+destAddr);
				String destPort = tmpArray[2].trim();
				if (!checkPort(destPort)) {
					System.out.println("Invalid port number");
					return false;
				}
				// System.out.println("destPort = "+destPort);

				P1.addCmdList.add(new String[] { destName, destAddr, destPort });

			} else {
				System.out.println("Invalid command 5");
				return false;
			}

		}
		return true;
	}

	public static void mergeNets(Hashtable<Integer, String[]> updateNets, Hashtable<Integer, String[]> recvdNets) {
		int num = updateNets.size();
		updateNets.put(num, recvdNets.get(0));

	}

	public static boolean checkOutCmd(String cmd) {
		if (cmd == null || cmd.length() < 2  || !cmd.matches("\\(.*,*.*\\)")) {
			System.out.println("Invalid command");
			return false;
		}
		
		cmd = cmd.substring(1,cmd.length()-1).trim();
		//System.out.println("after removing() and now cmd = "+cmd+ " and its length = "+ cmd.length());
		if (cmd == null || cmd.length() == 0) {
			System.out.println("Invalid command");
			return false;
		}
		String[] strArray = cmd.split(",");
		
		
		int size = strArray.length;
		P1.tuple = new ArrayList<Object>();
		for(int i = 0; i < size; i++) {
			strArray[i] = strArray[i].trim();
			//System.out.println(strArray[i]);
			if (isString(strArray[i])) {
				strArray[i] = strArray[i].substring(1, strArray[i].length()-1);
				P1.tuple.add(strArray[i]);
				System.out.println(strArray[i]+" is added to tuple");
				continue;
			}
			else if (isInt(strArray[i])) {		
				P1.tuple.add(Integer.parseInt(strArray[i]));
				System.out.println(strArray[i]+" is added to tuple");
				continue;
			}
			else if (isFloat(strArray[i])) {	
					P1.tuple.add(Float.parseFloat(strArray[i]));
					System.out.println(strArray[i]+" is added to tuple");
					continue;

			}
			else {
				System.out.println("Invalid tuple format");
				P1.tuple = null;
				return false;
			}
			
		}
		
		return true;
	}
	
	
	public static boolean checkRdCmd(String cmd) {
		if (cmd == null || cmd.length() < 2  || !cmd.matches("\\(.*,*.*\\)")) {
			System.out.println("Invalid command");
			return false;
		}
		
		cmd = cmd.substring(1,cmd.length()-1).trim();
		//System.out.println("after removing() and now cmd = "+cmd+ " and its length = "+ cmd.length());
		if (cmd == null || cmd.length() == 0) {
			System.out.println("Invalid command");
			return false;
		}
		String[] strArray = cmd.split(",");
		
		
		int size = strArray.length;
		P1.tuple = new ArrayList<Object>();
		for(int i = 0; i < size; i++) {
			strArray[i] = strArray[i].trim();
			//System.out.println(strArray[i]);
			if (isString(strArray[i])) {
				strArray[i] = strArray[i].substring(1, strArray[i].length()-1);
				P1.tuple.add(strArray[i]);
				System.out.println(strArray[i]+" is added to tuple");
				continue;
			}
			else if (isInt(strArray[i])) {		
				P1.tuple.add(Integer.parseInt(strArray[i]));
				System.out.println(strArray[i]+" is added to tuple");
				continue;
			}
			else if (isFloat(strArray[i])) {	
					P1.tuple.add(Float.parseFloat(strArray[i]));
					System.out.println(strArray[i]+" is added to tuple");
					continue;

			}
			else if (isTypeMatch(strArray[i])) {
				P1.tuple.add(typeMatch);
				System.out.println(strArray[i]+" is a type match for "+typeMatch[1]+"added to tuple");
				continue;
			}
			else {
				System.out.println("Invalid tuple format");
				P1.tuple = null;
				return false;
			}
			
		}
		
		return true;
	}
	
	public static boolean isInt(String s) {
		if (s.matches("[+-]{0,1}[0-9]*")) {
			if (Integer.parseInt(s)<=Integer.MAX_VALUE && Integer.parseInt(s)>=Integer.MIN_VALUE) {
				return true;
			}
			else {
				//System.out.println("Exceed the Max or Min value of integer");
				return false;
			}
		}
		return false;
	}
	public static boolean isFloat(String s) {
		if (s.matches("[+-]{0,1}[0-9]*[.][0-9]*[fF]{0,1}") || s.matches("[+-]{0,1}[0-9]*[.]{0,1}[0-9]*[Ee][+-]{0,1}[0-9]+[fF]{0,1}")) {
			if (Float.parseFloat(s)<=Float.MAX_VALUE && Float.parseFloat(s)>=-Float.MAX_VALUE) {
				return true;
			}
			else {
				//System.out.println("Exceed the Max or Min value of float");
				return false;
			}
		}
		return false;
	}
	public static boolean isString(String s) {
		if (s.charAt(0)=='"' && s.charAt(s.length()-1)=='"' && s.length() > 1) {
			return true;
		}
		return false;
	}
	public static boolean isTypeMatch(String s) {
		if (s.matches("?.*:.+")) {
			s.substring(1);
			String variable;
			String type;
			for (int i = s.length() - 1; i > 0; i--) {
				if (s.charAt(i) == ':') {
					variable = s.substring(0, i);
					if (variable == null || variable.length() == 0 && !variable.matches("^[a-zA-Z_$][a-zA-Z_$0-9]*$")) {
						return false;
					}
					typeMatch[0] = variable;
					type = s.substring(i+1).toLowerCase();
					if (type.equals("int") || type.equals("integer")) {
						typeMatch[1] = "int";
						return true;
					}
					else if (type.equals("float")) {
						typeMatch[1] = "float";
						return true;
					}
					else if (type.equals("string")) {
						typeMatch[1] = "string";
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static String tupleToString(List<Object> tuple) {
		
		if (tuple == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (Object i:tuple) {
			if (i instanceof Integer) {
				sb.append("Integer"+i.toString());
			}
			else if (i instanceof String) {
				sb.append("String"+i.toString());
			}
			else if (i instanceof Float) {
				sb.append("Float"+i.toString());
			}
		}
		return sb.toString();
		
	}
	
	public static String hashString(String s) throws NoSuchAlgorithmException {

		MessageDigest m;
		m = MessageDigest.getInstance("MD5");
		m.reset();
		m.update(s.getBytes());
		byte[] digest = m.digest();
		BigInteger bigInt = new BigInteger(1,digest);
		String hashtext = bigInt.toString(16);
		// Now we need to zero pad it if you actually want the full 32 chars.
		while(hashtext.length() < 32 ){
			hashtext = "0"+hashtext;
		}
		
		return hashtext;
		
	}
	
	public static int md5Sum(List<Object> t) throws NoSuchAlgorithmException {
		
		int sum = 0;
		String s = hashString(tupleToString(t));
		for (int i = 0; i < s.length(); i++) {
			String tmp = Character.toString(s.charAt(i));
			sum += Integer.parseInt(tmp,16);
		}
		
		return sum;
	}
	
	public static boolean compareTuples(List<Object> t1, List<Object> t2) {
		
		if (t1.size() != t2.size()) {
			return false;
		}
		for (int i = 0; i < t1.size(); i++) {
			if (t1.get(i) instanceof String && t2.get(i) instanceof String) {
				if (!t1.get(i).equals(t2.get(i))) {
					return false;
				}
			}
			else if (t1.get(i) instanceof Integer && t2.get(i) instanceof Integer) {
				if (t1.get(i) != t2.get(i)) {
					return false;
				}
			}
			else if (t1.get(i) instanceof Float && t2.get(i) instanceof Float) {
				if (t1.get(i) != t2.get(i)) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	public static int checkTupleCount(List<Object> tuple, Hashtable<String, List<HashTableEntry>> tupleSpace) throws NoSuchAlgorithmException {
		
		int count = 0;
		
		String hashcode = hashString(tupleToString(tuple));
		if (tupleSpace.contains(hashcode)) {
			List<HashTableEntry> list = tupleSpace.get(hashcode);
			for (HashTableEntry entry : list) {
				if (compareTuples(entry.tuple, tuple)) {
					count = entry.counts;
				}
			}
		}
		
		
		return count;
		
	}
	
	public static List<Object> getTuple (List<Object> t, Hashtable<String, List<HashTableEntry>> ts) {
		
		boolean bl = false;
		
		for (String key:ts.keySet()) {
			List<HashTableEntry> entryList = ts.get(key);
			Searchtuple:
			for (int i = 0; i < entryList.size(); i++) {
				List<Object> tuple = entryList.get(i).tuple;
				if (tuple.size() != t.size()) {
					continue;
				}
				for (int j = 0; j < t.size(); j++) {
					if (t.get(j) instanceof String[]) {
						String tmp = ((String[]) t.get(j))[1];
						if (tmp.equals("string")){
							if (tuple.get(j) instanceof String) {
								continue Searchtuple;
							}
						}
						else if (tmp.equals("int")) {
							if (tuple.get(j) instanceof Integer) {
								continue;
							}
						} 
						else if (tmp.equals("float")) {
							if (tuple.get(j) instanceof Float) {
								continue;
							}
						}
					}
					else if (t.get(j) instanceof String) {
						if (tuple.get(j) instanceof String) {
							continue;
						}
					}
					else if (t.get(j) instanceof Integer) {
						if (tuple.get(j) instanceof Integer) {
							continue;
						}
					}
					else if (t.get(j) instanceof Float) {
						if (tuple.get(j) instanceof Float) {
							continue;
						}
					}
				}
				return tuple;
				
				
			}
			
		}
		
		
		return null;
	}
	
}



