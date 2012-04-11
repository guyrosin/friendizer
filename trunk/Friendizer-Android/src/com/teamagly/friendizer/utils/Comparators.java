/**
 * 
 */
package com.teamagly.friendizer.utils;

import java.util.Comparator;

import com.teamagly.friendizer.model.UserInfo;

public class Comparators {

    public class AlphabetComparator implements Comparator<UserInfo> {
	@Override
	public int compare(UserInfo u1, UserInfo u2) {
	    return u1.name.compareTo(u2.name);
	}
    }

    public class ValueComparator implements Comparator<UserInfo> {
	@Override
	public int compare(UserInfo u1, UserInfo u2) {
	    return -((Long) u1.value).compareTo(u2.value); // Descending order
	}
    }

    public class AgeComparator implements Comparator<UserInfo> {
	@Override
	public int compare(UserInfo u1, UserInfo u2) {
	    return u1.age.compareTo(u2.age);
	}
    }

    public class MatchingComparator implements Comparator<UserInfo> {
	@Override
	public int compare(UserInfo u1, UserInfo u2) {
	    return -((Integer) u1.matching).compareTo(u2.matching); // Descending order
	}
    }

    public class DistanceComparator implements Comparator<UserInfo> {
	@Override
	public int compare(UserInfo u1, UserInfo u2) {
	    return ((Long) u1.distance).compareTo(u2.distance);
	}
    }
}
