package com.teamagly.friendizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;
import net.sf.jsr107cache.CacheStatistics;

import com.google.appengine.api.memcache.jsr107cache.GCacheFactory;
import com.teamagly.friendizer.model.User;

public final class NearbyUsersCache {
	private static final int EXPIRES_TIME = 15; // In seconds
	private static Cache cache;

	private NearbyUsersCache() {
	}

	static {
		HashMap<String, Integer> props = new HashMap<String, Integer>();
		props.put(GCacheFactory.EXPIRATION_DELTA, EXPIRES_TIME);
		try {
			CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
			cache = cacheFactory.createCache(props);
		} catch (CacheException e) {
			System.err.println(e.getMessage());
		}
	}

	public static void put(long userID, List<User> value) {
		cache.put(userID, new ArrayList<User>(value));
	}

	@SuppressWarnings("unchecked")
	public static List<User> get(long userID) throws NullPointerException {
		return (List<User>) cache.get(userID);
	}

	public static String fetchCacheStatistics() {
		CacheStatistics stats = cache.getCacheStatistics();
		int hits = stats.getCacheHits();
		int misses = stats.getCacheMisses();
		return "Cache hits: " + hits + "\nCache misses:" + misses;
	}
}
