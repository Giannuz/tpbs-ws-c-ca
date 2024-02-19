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
		
			if (!(f.exists() && f.isDirectory())) {
			
				System.out.println("Directory " + servletDir + " does not exists");
				return; 
			
			} else {

				try
				{ 
					String metadataFile = servletDir + "/" + "metadata.txt"; // location of the metadata.txt file
					BufferedReader reader = new BufferedReader(new FileReader(metadataFile));
					String command = reader.readLine();
					while (command != null) {
						
						if (command.contains("=")){
							int index = command.indexOf("=");
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
					urls[0] = new URL("file:" + servletDir + "/" + "class" + "/");
					loader = new URLClassLoader(urls);	
				}
				catch (IOException e) {
					System.out.println(e.toString() );
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
					servlet = (HttpServlet) myClass.getDeclaredConstructor().newInstance();
				}
				catch (Exception e) {
					System.out.println(e.toString());
				}
				ServletHashTable.put(servletInternalName, servlet);
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
	
	void executeLoadWithAnnotations(String servletInternalName)throws IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		
		if(ServletHashTable.contains(servletInternalName)){
			System.out.println("Servlet" + servletInternalName + " already in the servlet repository");
			return;
		}
		
	    String servletDir = MyHttpServer.DYNAMIC_WEB_ROOT + "/" + servletInternalName;
	    File f = new File(servletDir);
	    
		//check if exists a class folder
	    if (!(f.exists() && f.isDirectory())) {
	        System.out.println("Directory " + servletDir + " does not exists");
	        return;
	    
		} 
		

		try (URLClassLoader loader = new URLClassLoader(new URL[]{new URL("file:" + servletDir + "/" + "class" + "/")})) {
			Class<?> myClass = loader.loadClass(servletInternalName);
			addServletInstance(myClass);
		} catch (IOException | ClassNotFoundException e) {
			System.out.println(e.toString());
		}
		
	}

	void addServletInstance(Class<?> myClass) {
		try {
			MyAnnotation myAnnotation = myClass.getAnnotation(MyAnnotation.class);
			if (myAnnotation != null) {
				HttpServlet servlet = (HttpServlet) myClass.getDeclaredConstructor().newInstance();
				ServletHashTable.put(myAnnotation.value(), servlet);
				System.out.println("Servlet " + myAnnotation.value() + " added");
			} else {
				System.out.println("Failed to find Annotation for the class");
			}
		} catch (Exception e) {
			System.out.println(e.toString());
		}
	}

	
	void executeCommand(String command) throws IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		if (firstWord(command).equals("load")) {
			executeLoad(secondWord(command));
			return;
		}
		
		if(firstWord(command).equals("load-with-annotations")) {
			executeLoadWithAnnotations(secondWord(command));
			return;
		}
		
		if (firstWord(command).equals("remove")) {
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
			// TODO Auto-generated catch block
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
			} catch (IllegalArgumentException | InvocationTargetException | NoSuchMethodException
					| SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Shutdown.flag=true;	
	}

}

