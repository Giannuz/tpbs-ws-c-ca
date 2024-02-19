import java.util.logging.Logger;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

public class MyServletProcessor {

	private static final Logger LOGGER = Logger.getLogger(MyServletProcessor.class.getName());
	
	public void process(Request request, Response response) {
		
		String uri = request.getUri(); //name of the requested servlet
		String servletName = uri.substring(uri.lastIndexOf("/") + 1);
		
		if (!ServletHashTable.contains(servletName)) { // checks that the servlet has actually been launched
		
			System.out.println("Error: " + servletName + " unknown");
		
		} else {
			
			HttpServlet servlet = ServletHashTable.get(servletName); // retrieves the servlet from the hash table
			try {
				
				//calls the service method of the HttpServlet object to handle an HTTP request. 
				//It takes the client's request and the server's response as arguments, both in the form of the generic ServletRequest 
				//and ServletResponse interfaces. The service method then determines the type of HTTP request (GET, POST, etc.) and calls 
				//the appropriate method (doGet, doPost, etc.) to handle the request.

				servlet.service((ServletRequest) request, (ServletResponse) response); // we handle the user's request

			}
			catch (Exception e) {

				LOGGER.severe(e.toString());

			}
			catch (Throwable e) {
				
				LOGGER.severe(e.toString());

			}

		}
	}
}
