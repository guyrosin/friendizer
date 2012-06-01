package com.teamagly.friendizer;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.android.c2dm.server.PMF;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.teamagly.friendizer.model.DeviceInfo;

@SuppressWarnings("serial")
public class RegisterServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(RegisterServlet.class.getName());
	private static final String OK_STATUS = "OK";
	private static final String ERROR_STATUS = "ERROR";

	private static int MAX_DEVICES = 10;

	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");
		log.info("RegisterServlet");
		System.out.println("RegisterServlet");

		RequestInfo reqInfo = RequestInfo.processRequest(req, resp, getServletContext());
		if (reqInfo == null) {
			return;
		}

		if (reqInfo.deviceRegistrationID == null) {
			resp.setStatus(400);
			resp.getWriter().println(ERROR_STATUS + "(Must specify devregid)");
			log.severe("Missing registration id ");
			return;
		}

		// Because the deviceRegistrationId isn't static, we use a static
		// identifier for the device. (Can be null in older clients)
		String deviceId = reqInfo.getParameter(Util.DEVICE_REGISTRATION_ID);

		PersistenceManager pm = PMF.get().getPersistenceManager();

		try {
			List<DeviceInfo> registrations = reqInfo.devices;

			if (registrations.size() > MAX_DEVICES) {
				// we could return an error - but user can't handle it yet.
				// we can't let it grow out of bounds.
				// TODO: we should also define a 'ping' message and
				// expire/remove
				// unused registrations
				DeviceInfo oldest = registrations.get(0);
				if (oldest.getRegistrationTimestamp() == null) {
					reqInfo.deleteRegistration(oldest.getDeviceRegistrationID());
				} else {
					long oldestTime = oldest.getRegistrationTimestamp().getTime();
					for (int i = 1; i < registrations.size(); i++) {
						if (registrations.get(i).getRegistrationTimestamp().getTime() < oldestTime) {
							oldest = registrations.get(i);
							oldestTime = oldest.getRegistrationTimestamp().getTime();
						}
					}
					reqInfo.deleteRegistration(oldest.getDeviceRegistrationID());
				}
			}

			// Get device if it already exists, else create
			String suffix = (deviceId != null ? "#" + Long.toHexString(Math.abs(deviceId.hashCode())) : "");
			Key key = KeyFactory.createKey(DeviceInfo.class.getSimpleName(), reqInfo.userName + suffix);

			DeviceInfo device = null;
			try {
				device = pm.getObjectById(DeviceInfo.class, key);
			} catch (JDOObjectNotFoundException e) {
			}
			if (device == null) {
				device = new DeviceInfo(key, reqInfo.deviceRegistrationID);
			} else {
				// update registration id
				device.setDeviceRegistrationID(reqInfo.deviceRegistrationID);
				device.setRegistrationTimestamp(new Date());
			}

			pm.makePersistent(device);
			log.log(Level.INFO, "Registered device " + reqInfo.userName);

			resp.getWriter().println(OK_STATUS);
		} catch (Exception e) {
			resp.setStatus(500);
			resp.getWriter().println(ERROR_STATUS + " (Error registering device)");
			log.log(Level.WARNING, "Error registering device.", e);
		} finally {
			pm.close();
		}
	}
}