package com.teamagly.friendizer;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.*;

import com.teamagly.friendizer.model.UserDevice;

@SuppressWarnings("serial")
public class UnregisterServlet extends HttpServlet {
	private final Logger log = Logger.getLogger(getClass().getName());

	@SuppressWarnings("unused")
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String regID = request.getParameter("regID");
		String userID = request.getParameter("userID");

		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.deletePersistent(pm.getObjectById(UserDevice.class, regID)); // Delete the regID
		} catch (Exception e) {
			log.severe("Couldn't delete old reg ID: " + regID);
		}
		pm.close();
		response.setStatus(HttpServletResponse.SC_OK);
	}
}
