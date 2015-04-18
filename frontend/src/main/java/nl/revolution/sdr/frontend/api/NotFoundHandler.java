package nl.revolution.sdr.frontend.api;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class NotFoundHandler extends AbstractHandler {

    private static final Logger LOG = LoggerFactory.getLogger(NotFoundHandler.class);

    public void handle(String target, Request baseRequest, HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException {
        LOG.warn("404 for {}", baseRequest.getRequestURI());
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().write("404 - Not found.");
        baseRequest.setHandled(true);
    }

}