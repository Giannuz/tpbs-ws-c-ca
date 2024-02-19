
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServlet;

// we need a container to save all the association "servlet name - httpservlet object"
public class ServletHashTable {
		
	// we save the keys as strings
	static Hashtable<String, HttpServlet> ht;

	// constructor
	ServletHashTable() {
		ht = new Hashtable<String, HttpServlet>();
	}

	// save a nomeservlet and servlet pair
	static void put (String s, HttpServlet h){
		ht.put(s,  h);
	}

	// method to check if the HashTable contains the name of the servlet
	static boolean contains (String s){
		return ht.containsKey(s);
	}

	// method to return the servlet associated to given name
	static HttpServlet get(String s) {
		return ht.get(s);
	}

	// method to remove a servlet ginven his name
	static void remove(String s) {
		ht.remove(s);
	}


	// returns the list of currently active servlets
	static List<String> list() {

		List<String> Names = new ArrayList<>();
		

		Enumeration<String> servletNames = ht.keys();
		while(servletNames.hasMoreElements()){
			
			String servletName = servletNames.nextElement();
			HttpServlet servlet = ht.get(servletName);
			
			Names.add(servlet.getClass().getName());
		}

		return Names;


	}

}

