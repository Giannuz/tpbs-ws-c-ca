package com.example;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

// starts a multithreaded server that listens for client connections on port 7654 and handles each client request in a separate thread.

public class ThreadPoolBasedStaticWebServer {
    private static final int THREAD_POOL_SIZE = 2;
    private static final File STATIC_CONTENT_REPOSITORY = new File("staticcontentrepository");

    public static void main(String[] args) throws IOException {
        // create an ExecutorService with size 2
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        
        // Start a ServerSocket on port 7654
        try (ServerSocket serverSocket = new ServerSocket(7654)) {
            // Each time a client connects, the ServerSocket provides a Socket for communication with the client
            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(() -> handleRequest(clientSocket));
            }
        }
    }

    // function that threads will execute
    private static void handleRequest(Socket clientSocket) {
        // creates a BufferedReader to read incoming data from the client
        // creates a PrintWriter to send output data to the client
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            // the request is split, the second element is to be the path to the requested file
            String request = in.readLine();
            String[] requestParts = request.split(" ");
            String filePath = requestParts[1];
            
            // search for the required file
            File requestedFile = new File(STATIC_CONTENT_REPOSITORY, filePath);
            if (requestedFile.exists() && !requestedFile.isDirectory()) {
                out.println("HTTP/1.0 200 OK"); // sends an HTTP 200 OK response to the client
                out.println("Content-Type: text/html");
                out.println("Content-Length: " + requestedFile.length());
                out.println();

                try (BufferedReader fileReader = new BufferedReader(new FileReader(requestedFile))) {
                    String line;
                    while ((line = fileReader.readLine()) != null) {
                        out.println(line);
                    }
                }
            } else { // if the file is not found, the response will be 404 followed by a simple 'not found' page
                out.println("HTTP/1.0 404 Not Found");
                out.println("Content-Type: text/html");
                out.println();
                out.println("<html><body><h1>404 Not Found</h1></body></html>");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
