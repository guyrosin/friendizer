package com.teamagly.friendizer;

import java.util.HashMap;
import java.util.List;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;
import net.sf.jsr107cache.CacheStatistics;

import com.google.appengine.api.memcache.jsr107cache.GCacheFactory;
import com.teamagly.friendizer.model.User;

public final class LeaderboardCache {
	private static final int EXPIRES_TIME = 20; // In seconds
	private static Cache cache;

	private LeaderboardCache() {
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

	public static void put(String type, List<User> value) {
		String key = "leaderboard#" + type;
		cache.put(key, value);
	}

	@SuppressWarnings("unchecked")
	public static List<User> get(String type) throws NullPointerException {
		String key = "leaderboard#" + type;
		return (List<User>) cache.get(key);
	}

	public static String fetchCacheStatistics() {
		CacheStatistics stats = cache.getCacheStatistics();
		int hits = stats.getCacheHits();
		int misses = stats.getCacheMisses();
		return "Cache hits: " + hits + "\nCache misses:" + misses;
	}
}
