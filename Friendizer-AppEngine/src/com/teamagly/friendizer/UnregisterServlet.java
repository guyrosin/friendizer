package com.teamagly.friendizer;
import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@SuppressWarnings("serial")
public class UnregisterServlet extends HttpServlet {
    private static final String OK_STATUS = "OK";
    private static final String ERROR_STATUS = "ERROR";

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	resp.setContentType("text/plain");

	RequestInfo reqInfo = RequestInfo.processRequest(req, resp, getServletContext());
	if (reqInfo == null) {
	    return;
	}

	if (reqInfo.deviceRegistrationID == null) {
	    resp.setStatus(400);
	    resp.getWriter().println(ERROR_STATUS + " (Must specify devregid)");
	    return;
	}

	reqInfo.deleteRegistration(reqInfo.deviceRegistrationID);
	resp.getWriter().println(OK_STATUS);
    }
}
