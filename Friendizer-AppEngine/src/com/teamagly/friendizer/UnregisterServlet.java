package com.teamagly.friendizer;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.teamagly.friendizer.model.UserDevice;

@SuppressWarnings("serial")
public class UnregisterServlet extends HttpServlet {
	private final Logger log = Logger.getLogger(getClass().getName());

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String regIDParam = req.getParameter(Util.REG_ID);
		String userIDParam = req.getParameter(Util.USER_ID);

		PersistenceManager pm = PMF.get().getPersistenceManager();
		try {
			Query query = pm.newQuery(UserDevice.class);

			query.setFilter(Util.REG_ID + " == regIDParam && " + Util.USER_ID + " == userIDParam");
			query.declareParameters("String regIDParam, String userIDParam");
			query.deletePersistentAll();
		} catch (Exception e) {
			log.warning("Error unregistering device: " + e.getMessage());
		} finally {
			pm.close();
		}
		resp.setStatus(HttpServletResponse.SC_OK);
	}
}
