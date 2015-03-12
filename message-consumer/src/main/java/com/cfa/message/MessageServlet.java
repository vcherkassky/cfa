package com.cfa.message;

import com.google.common.io.CharStreams;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <br/><br/>Created by victor on 3/11/15.
 */
public class MessageServlet extends HttpServlet {
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
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Body should contain a valid JSON");
        } else {
            sink.send(body);
            resp.setStatus(HttpServletResponse.SC_OK);
        }
    }
}
