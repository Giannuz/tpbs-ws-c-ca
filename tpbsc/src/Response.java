
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class Response implements HttpServletResponse {
	Request request;
	OutputStream output;
	PrintWriter writer;
	//constructor with the output stream in which the http response will be written
	public Response(OutputStream output) {
		this.output = output;
	}	
	//sets the Request object associated with this response. The Request object is used to extract information about the incoming request, such as the requested URI
	public void setRequest(Request request) {
		this.request = request;
	}
	/* This method is used to serve static pages */
	//This method is used to serve static resources, such as HTML pages or files, within an HTTP response. It reads the contents of a requested file, calculates its length and sends an HTTP response with the contents of the file. In the event of an error, such as when the requested file does not exist, a 'File Not Found' error message is sent as a response.
	public void sendStaticResource() throws IOException {
		String headerHttpBeforeLength = "HTTP/1.1 200 OK"+"\r\n"+"Content-Type: text/html"+"\r\n"+"Content-Length: ";
		String headerHttpAfterLength = "\r\n" + "\r\n";
		try {
			String fileName = MyHttpServer.STATIC_WEB_ROOT+request.getUri();
			FileReader fileReader = new FileReader(fileName.trim());
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			StringBuilder stringBuilder = new StringBuilder();
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line);
			}
			bufferedReader.close();
			int len=stringBuilder.toString().length();
			String outMsg=headerHttpBeforeLength+Integer.toString(len)+headerHttpAfterLength+stringBuilder.toString();
			
			output.write(outMsg.getBytes());
		}
		catch (FileNotFoundException e) {
			String errorMessage = "HTTP/1.1 404 File Not Found\r\n" +
			"Content-Type: text/html\r\n" +
			"Content-Length: 23\r\n" +
			"\r\n" +
			"<h1>File Not Found</h1>";
			output.write(errorMessage.getBytes());
		}
	}
	
	/** implementation of ServletResponse */
	public void flushBuffer() throws IOException { }
	public int getBufferSize() {
		return 0;
	}
	public String getCharacterEncoding() {
		return null;
	}
	public Locale getLocale() {
		return null;
	}
	public ServletOutputStream getOutputStream() throws IOException {
		return null;
	}
	public PrintWriter getWriter() throws IOException {
		// autoflush is true, println() will flush,
		// but print() will not.
		writer = new PrintWriter(output, true);
		return writer;
	}
	public boolean isCommitted() {
		return false;
	}
	public void reset() { }
	public void resetBuffer() { }
	public void setBufferSize(int size) { }
	public void setContentLength(int length) { }
	public void setContentType(String type) { }
	public void setLocale(Locale locale) { }
	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setCharacterEncoding(String arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setContentLengthLong(long arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void addCookie(Cookie cookie) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean containsHeader(String name) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public String encodeURL(String url) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String encodeRedirectURL(String url) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String encodeUrl(String url) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String encodeRedirectUrl(String url) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void sendError(int sc, String msg) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void sendError(int sc) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void sendRedirect(String location) throws IOException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setDateHeader(String name, long date) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void addDateHeader(String name, long date) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setHeader(String name, String value) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void addHeader(String name, String value) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setIntHeader(String name, int value) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void addIntHeader(String name, int value) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setStatus(int sc) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setStatus(int sc, String sm) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public int getStatus() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public String getHeader(String name) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Collection<String> getHeaders(String name) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public Collection<String> getHeaderNames() {
		// TODO Auto-generated method stub
		return null;
	}
}

