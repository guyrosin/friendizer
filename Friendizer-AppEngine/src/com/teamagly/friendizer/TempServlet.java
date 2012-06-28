package com.teamagly.friendizer;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.teamagly.friendizer.model.Achievement;

@SuppressWarnings("serial")
public class TempServlet extends HttpServlet {
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PersistenceManager pm = PMF.get().getPersistenceManager();
		pm.makePersistent(new Achievement(29001, "", "", "", 0, 0));
		pm.makePersistent(new Achievement(30001, "", "", "", 0, 0));
		pm.makePersistent(new Achievement(66001, "", "", "", 0, 0));
		pm.close();
	}
}
