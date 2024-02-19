import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;


public class servlet1 extends HttpServlet {
 
   private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
   private String message;
   public void doGet(HttpServletRequest request, HttpServletResponse response)   //method that returns this string in HTML
      throws ServletException, IOException {
     	   message = "HTTP/1.1 200 OK\r\n"+
				"Content-Type: text/html\r\n"+
				"Content-Length: 580\r\n" +
				"\r\n"+
	   			"<h1>Miche - servlet1 </h1>";
      PrintWriter out = response.getWriter();
      out.println(message);
      for (char letter : ALPHABET.toCharArray()) {
         out.print("<a>" + letter + "</a>");
         out.flush();  // sends the character directly to the client
         try {
             Thread.sleep(400); // wait 0.4 seconds 
         } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
         }
      }
   }
}