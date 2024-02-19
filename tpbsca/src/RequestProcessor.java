
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class RequestProcessor implements Runnable {
    private Socket socket;

    public RequestProcessor(Socket socket) {
        // we need this class to serve different type of request: servlet vs static
        this.socket = socket;
    }

    @Override
    public void run() {

                // we use try in order to have a cleaner implementation
                try (InputStream input = socket.getInputStream(); OutputStream output = socket.getOutputStream()) {
            
                    Request request = new Request(input);
                    request.parse();
        
                    Response response = new Response(output);
                    response.setRequest(request);
        
                    if (request.getUri() != null) {
        
        
        
                        if (request.getUri().startsWith("/servlet")) {
                            MyServletProcessor processor = new MyServletProcessor();
                            processor.process(request, response);
                        } else {
                            MyStaticResourceProcessor processor = new MyStaticResourceProcessor();
                            processor.process(request, response);
                        }
                    }
        
                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

    }
}

