# tpbsc



Extend the Thread Pool Based Static Web Server to a Thread Pool Based Servlet Container
(tpbsc), able to process both static content requests and servlet activation requests. These latter requests are triggered by the “servlet” keyword in the URL request string (e.g.,http://localhost:7654/servlet/<servlet name>).


Servlets must be associated to subdirectories in an external servlet repository located in an external servlet repository, i.e., a folder called “servletrepository”, to be placed in the SP folder.


The name of the servlet folder inside the external servlet repository is used by clients as a servlet id in the URL request string. The servlet folder must include a file called “metadata.txt”, which contains the name of the servlet class in the form: `ServletClassName=<class name>`, a subfolder called “class”, which contains the executable class, a subfolder called “lib”, which includes the required libraries if needed, and a folder called “src”, which includes the servlet source files.


The Thread Pool Based Servlet Container must include a Management Console, running in a separate thread, in addition to the main thread listening on port 7654. Through such a console the container administrator populates and manages an internal servlet repository (e.g., a Hashtable). The Management Console must support the following commands: 

- “load <servletname>”, 

- “remove <servlet name>”, 

- “list”

where refers to the name of the servlet folder. 

The “load” command checks that the servlet requested 1. is in the external servlet
repository and 2. is not in the internal servlet repository. If both checks give positive results, then it loads the servlet class in the internal servlet repository, otherwise it issues a notification. 

The “remove” command checks that the servlet to be removed is in the internal servlet repository. If the check gives a positive result, then it removes the servlet, otherwise it issues a notification.
Finally, the “list” command lists the name of the servlets in the internal servlet repository.
Create an executable jar file called tpbsc.jar and place it in the SP folder. Create a servlet called myservlet and place it in the external repository (SP/servletrepository). The servlet source file, written in Java, must print the English Alphabet (A..Z) one letter at a time at a pace of one letter every 400 msecs, so that the whole print takes about 10 seconds.