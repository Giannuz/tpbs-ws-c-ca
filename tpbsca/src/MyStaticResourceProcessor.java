import java.io.IOException;
public class MyStaticResourceProcessor {
	
	// accepts two arguments of type Request and Response, representing respectively the request object and response object associated with an HTTP request
	public void process(Request request, Response response) {
		
		try {
			//send static resources pages or files within the HTTP response
			response.sendStaticResource();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}
