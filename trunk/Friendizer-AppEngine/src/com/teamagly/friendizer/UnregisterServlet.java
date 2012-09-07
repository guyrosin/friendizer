package com.teamagly.friendizer;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.teamagly.friendizer.model.UserDevice;

@SuppressWarnings("serial")
public class UnregisterServlet extends HttpServlet {
	private final Logger log = Logger.getLogger(getClass().getName());

	@SuppressWarnings("unused")
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String regID = req.getParameter(Util.REG_ID);
		String userID = req.getParameter(Util.USER_ID);

		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			pm.deletePersistent(pm.getObjectById(UserDevice.class, regID)); // Delete the regID
		} catch (Exception e) {
			log.severe("Couldn't delete old reg ID: " + regID);
		}
		pm.close();
		resp.setStatus(HttpServletResponse.SC_OK);
	}
}
