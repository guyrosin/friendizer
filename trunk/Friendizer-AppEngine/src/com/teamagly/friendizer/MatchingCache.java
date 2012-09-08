package com.teamagly.friendizer;

import java.util.HashMap;

import net.sf.jsr107cache.*;

import com.google.appengine.api.memcache.jsr107cache.GCacheFactory;

public final class MatchingCache {
	private static final int ONE_DAY = 3600 * 24;
	private static Cache cache;
	
	private MatchingCache() {
	}
	
	static {
		HashMap<String, Integer> props = new HashMap<String, Integer>();
		props.put(GCacheFactory.EXPIRATION_DELTA, ONE_DAY);
		try {
			CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
			cache = cacheFactory.createCache(props);
		} catch (CacheException e) {
			System.err.println(e.getMessage());
		}
	}

	public static void put(long userID1, long userID2, int value) {
		String key = userID1 + "#" + userID2;
		cache.put(key, value);
	}

	public static int get(long userID1, long userID2) throws NullPointerException {
		String key = userID1 + "#" + userID2;
		return (Integer) cache.get(key);
	}

	public static String fetchCacheStatistics() {
		CacheStatistics stats = cache.getCacheStatistics();
		int hits = stats.getCacheHits();
		int misses = stats.getCacheMisses();
		return "Cache hits: " + hits + "\nCache misses:" + misses;
	}
}
