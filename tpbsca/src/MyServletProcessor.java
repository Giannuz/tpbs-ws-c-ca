
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

public class MyServletProcessor {
	public void process(Request request, Response response) {
		
		String uri = request.getUri(); //name of the requested servlet
		String servletName = uri.substring(uri.lastIndexOf("/") + 1); 
		
		if (!ServletHashTable.contains(servletName)) { // checks that the servlet has actually been launched
			System.out.println("Error: " + servletName + " unknown");
		} else {
			
			HttpServlet servlet = ServletHashTable.get(servletName); // retrieves the servlet from the hash table
			try {
			
				servlet.service((ServletRequest) request, (ServletResponse) response); // we handle the user's request
			
			}
			catch (Exception e) {

				System.out.println(e.toString());
			
			}
			catch (Throwable e) {

				System.out.println(e.toString());
			
			}
		
		}
	}
}
