import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import javax.servlet.http.HttpServlet;

public class ManagementConsole extends Thread {
	ManagementConsole(){
	}

	// Separate the command from the rest of the string
	String firstWord(String command) {
		if (command.contains(" ")){
			int index = command.indexOf(" ");
			return command.substring(0, index);
		} else return command;
		
	}

	// Get the argument of the command 
	String secondWord(String command) {
		if (command.contains(" ")) {
			int index = command.indexOf(" ");
			return command.substring(index+1, command.length());
			} else {
				return command;
			}
	}

	void executeUnload(String servletInternalName){

		// Verify if the servlet that the user want to remove exist, then it remove it form the hashtable
		if (!ServletHashTable.contains(servletInternalName)) {
			System.out.println("Servlet " + servletInternalName + " not in the servlet repository");
		} else {
			ServletHashTable.remove(servletInternalName);
			System.out.println("Servlet " + servletInternalName + " removed");
		}
	}

	
	void executeLoad(String servletInternalName){
		if (ServletHashTable.contains(servletInternalName)) {

			System.out.println("Servlet " + servletInternalName + " already in the servlet repository");
		
		} else {
			
			String servletClassName = null;
			String servletRepository = new String(MyHttpServer.DYNAMIC_WEB_ROOT).trim(); //take and save the name of the DYNAMIC_WEB_ROOT
			String servletDir = new String(servletRepository + "/" + servletInternalName).trim(); //take and save the servletDir name by merging servletRepository and servletInternalName
			
			File f = new File(servletDir);
			if (!(f.exists() && f.isDirectory())) { // checks whether files and directories exist

				System.out.println("The specified directory: " + servletDir + " does not exists");
				return; 
			
			} else {
				try
				{ 
					String metadataFile = servletDir + "/" + "metadata.txt"; // location of the metadata.txt file, contain the name of the servlet class.
					BufferedReader reader = new BufferedReader(new FileReader(metadataFile));
					String command = reader.readLine();
					
					while (command != null) { // legge il file riga per riga, 
						
						if (command.contains("=")){
							int index = command.indexOf("="); // if a line contains an '= takes the part of the string after the equals and assigns it to servletClassName
							servletClassName = command.substring(index+1, command.length()); // value after "="
						}
						command = reader.readLine(); //next line
					
					}
					
					reader.close();
				}
				
				catch (FileNotFoundException fe) 
				{ 
					System.out.println("File not found"); 
				} catch (IOException e) {
					e.printStackTrace();
				}

				URLClassLoader loader = null;
				try {
					URL[] urls = new URL[1];
					urls[0] = new URL("file:" + servletDir + "/" + "class" + "/"); // Creates a URLClassLoader pointing to the class directory
					loader = new URLClassLoader(urls);	// Use the class loader to load the servlet class
				}
				catch (IOException e) {
					System.out.println(e.toString() ); // If the instance cannot be created, it prints an error message
				}

				Class<?> myClass = null;
				try {
					myClass = loader.loadClass(servletClassName);
				}
				catch (ClassNotFoundException e) {
					System.out.println(e.toString());
				}
				HttpServlet servlet = null;
				try {
					servlet = (HttpServlet) myClass.getDeclaredConstructor().newInstance(); // creates a new instance of the servlet
				}
				catch (Exception e) {
					System.out.println(e.toString());
				}
				ServletHashTable.put(servletInternalName, servlet); // adds the servlet to the ServletHashTable hash table and prints a confirmation message.
				System.out.println("Servlet " + servletInternalName + " added"); 
			}
		}
	}

	void executeList() {
		
		List<String> servletNames = ServletHashTable.list();

		if(!servletNames.isEmpty()){

			for(String i : servletNames){

				System.out.println(i);

			}

		} else {

			System.out.println("No servlet to show");

		}

	}
	

	void executeCommand(String command) throws IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (firstWord(command).equals("load")) {
			executeLoad(secondWord(command));
			return;
		}
		
		if (firstWord(command).equals("unload")) {
			executeUnload(secondWord(command));
			return;
		}
		if(firstWord(command).equals( "list")){
			executeList();
			return;
		}
		if (firstWord(command).equals("quit")) {
			return;
		}
		System.out.print("Command unknown: ");
		System.out.println("Commands supported: load <servlet> unload <servlet> quit list <servlet> load-with-annotations");
	}
	
	public void run() {
		String command= null;
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		System.out.print ("Command: ");
		try {
			command  = bufferedReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			executeCommand(command);
		} catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			System.out.print ("Something went wrong with the command reading process.");
			e.printStackTrace();
		}
		while (!command.equals("quit")){
			System.out.print ("Command: ");
			try {
				command  = bufferedReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				executeCommand(command);
			} catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
				
				System.out.print ("Something went wrong with the exit process");					
						
				e.printStackTrace();
			}
		}
		ShutdownFlag.flag=true;	
	}

}

