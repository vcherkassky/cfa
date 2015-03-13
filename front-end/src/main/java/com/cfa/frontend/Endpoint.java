package com.cfa.frontend;

import com.cfa.commons.cassandra.CassandraClient;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * <br/><br/>Created by victor on 3/13/15.
 */
public class Endpoint {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Please run with arguments <PORT> <CASSANDRA_ADDRESS>");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);
        String cassandraAddress = args[1];

        CassandraClient.getInstance(cassandraAddress);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        Server jettyServer = new Server(port);
        jettyServer.setHandler(context);

        ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/*");
        jerseyServlet.setInitOrder(0);

        jerseyServlet.setInitParameter("jersey.config.server.provider.classnames",
                TotalAmountResource.class.getCanonicalName() + ", " + TransactionsAverageResource.class.getCanonicalName());
        try {
            jettyServer.start();
            jettyServer.join();
        } finally {
            jettyServer.destroy();
        }
    }
}
