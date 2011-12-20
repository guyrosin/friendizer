package com.teamagly.friendizer.server;


import com.google.android.c2dm.server.PMF;

import com.google.appengine.api.users.*;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.web.bindery.requestfactory.server.RequestFactoryServlet;


import java.util.List;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.ServletContext;


/**
 * Provide the connection between user's class and the database,
 * performs CRUD operations on the database,
 * @author Leon
 *
 */
public class DataStore {


  /**
   * Remove this object from the data store.
   */
  public void delete(Long id) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      User user = pm.getObjectById(User.class, id);
      pm.deletePersistent(user);
    } finally {
      pm.close();
    }
  }
  
  
  /**
   * Find a {@link User} by id.
   *
   * @param id the {@link User} id
   * @return the associated {@link User}, or null if not found
   */
  @SuppressWarnings("unchecked")
  public User find(Long id) {
    if (id == null) {
      return null;
    }

    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      Query query = pm.newQuery("select from " + User.class.getName()
          + " where id==" + id.toString());
      List<User> list = (List<User>) query.execute();
      return  (list.size() == 0 ? null : list.get(0));
    } catch (RuntimeException e) {
      System.out.println(e);
      throw e;
    } finally {
      pm.close();
    }
  }
  
  @SuppressWarnings("unchecked")
  public List<User> findAll() {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
        Query query = pm.newQuery("select from " + User.class.getName());
        List<User> list = (List<User>) query.execute();
        if (list.size() == 0) {
            list.size();
          }

      return list;
    } catch (RuntimeException e) {
      System.out.println(e);
      throw e;
    } finally {
      pm.close();
    }
  }
  
  /**
   * Persist this object in the datastore.
   */
  public User update(User user) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      pm.makePersistent(user);
      return user;
    } finally {
      pm.close();
    }
  }
  public static void sendC2DMUpdate(String message) {
	  
      UserService userService = UserServiceFactory.getUserService();
      com.google.appengine.api.users.User user = userService.getCurrentUser();
          ServletContext context = RequestFactoryServlet.getThreadLocalRequest().getSession().getServletContext();
          SendMessage.sendMessage(context, user.getEmail(), message);
  }


}