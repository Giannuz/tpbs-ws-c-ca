
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyHttpServer {

	public static final String STATIC_WEB_ROOT = "staticcontentrepository";
	public static final String DYNAMIC_WEB_ROOT = "servletrepository";

	public static void  main(String[] args) {
		ServletHashTable servletHashTable = new ServletHashTable();

		ManagementConsole managementConsole = new ManagementConsole();
		managementConsole.start();

		MyHttpServer myHttpServer = new MyHttpServer();
		myHttpServer.await();
		System.out.println("Quitting...");
		System.exit(0);
	}

	MyHttpServer() {
	}

	public void await() {
		ServerSocket serverSocket = null; // declared serverSocket
		int port = 7654; // port on which we will be listening
		try {
			serverSocket = new ServerSocket(port, 1, InetAddress.getByName("127.0.0.1"));
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		
		while (!Shutdown.flag) {
		    try {
		        try { 
		        	serverSocket.setSoTimeout(2000);
		            Socket socket = serverSocket.accept();
		            socket.setSoTimeout(2000);

		            // ask the threadpool to provide us with a processor
		            executorService.execute(new RequestProcessor(socket)); // here we execute all the code that were in MyhttpServer
		            
		        } catch (SocketTimeoutException se) {
		            if (Shutdown.flag)  return;
		        }
		    } catch (Exception e) {
		        e.printStackTrace();
		        continue;
		    }
		}
	}
}


