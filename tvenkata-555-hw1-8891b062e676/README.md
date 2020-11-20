# CIS 455/555 Homework 1 - Build a Web Server / Microservices Framework

# Commands

| #       | Command           | 
| :------------- |:------------- |
| 1      | `mvn -f ${current.project.path} clean install exec:java` |

# App output

You need to create a "run" option as per the instructions, and click on the `Preview:` link
for the Web server.  Your server's logs should show up on the terminal.



-   Implemented Persitent Connection and Chuncked Encoding
-   Included Performance testing pdf used apache jmeter to get the stats.
-   Implemented Multiple simultaneous sockets / servers. Design allows to declare multiple servers. To do this,
    Server invocation has to happen by creating a httpWebService Object. and call the start function.
    Data is not stored as part of static class. Still maintained ServiceFactory implementations to adhere basic functionality.

-   Implemented General wildcards in routes. Multiple Wild cards can be put in the url similar to spark routes.

-   Webserver.java is the main class.