package com.cfa.message;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * <br/><br/>Created by victor on 3/11/15.
 */
public class MessageConsumer {

    private final int port;
    private final MessageSink sink;

    public MessageConsumer(int port, String brokerList) {
        this.port = port;
        this.sink = new MessageSink(brokerList);
    }

    public void run() throws Exception {
        Server server = new Server(port);
        ServletContextHandler handler = new ServletContextHandler();
        handler.setContextPath("");
        handler.addServlet(new ServletHolder(new MessageServlet(sink)), "/messages");

        server.setHandler(handler);
        server.start();
        server.join();
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Please run with arguments <PORT> <BROKER_LIST>, BROKER_LIST should be host1:port1,host2:port2");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        String brokerList = args[1];

        new MessageConsumer(port, brokerList).run();
    }
}
