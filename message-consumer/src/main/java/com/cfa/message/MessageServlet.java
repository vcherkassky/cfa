package com.cfa.message;

import com.google.common.io.CharStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <br/><br/>Created by victor on 3/11/15.
 */
public class MessageServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(MessageServlet.class);

    private final MessageSink sink;

    public MessageServlet(MessageSink sink) {
        this.sink = sink;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String body = CharStreams.toString(req.getReader()).trim();
        //TODO: add schema validation here
        if (body.isEmpty()) {
            log.debug("ERROR: invalid body");
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Body should contain a valid JSON");
        } else {
            log.debug("Sending to Kafka");
            sink.send(body);

            log.debug("Finishing up with response");
            resp.setStatus(HttpServletResponse.SC_OK);
        }
    }
}
