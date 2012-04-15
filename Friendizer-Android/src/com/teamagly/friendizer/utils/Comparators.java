/**
 * 
 */
package com.teamagly.friendizer.utils;

import java.util.Comparator;

import com.teamagly.friendizer.model.User;

public class Comparators {

    public class AlphabetComparator implements Comparator<User> {
	@Override
	public int compare(User u1, User u2) {
	    return u1.getName().compareTo(u2.getName());
	}
    }

    public class ValueComparator implements Comparator<User> {
	@Override
	public int compare(User u1, User u2) {
	    return -((Long) u1.getValue()).compareTo(u2.getValue()); // Descending order
	}
    }

    public class AgeComparator implements Comparator<User> {
	@Override
	public int compare(User u1, User u2) {
	    return u1.getAge().compareTo(u2.getAge());
	}
    }

    public class MatchingComparator implements Comparator<User> {
	@Override
	public int compare(User u1, User u2) {
	    return -((Integer) u1.getMatching()).compareTo(u2.getMatching()); // Descending order
	}
    }

    public class DistanceComparator implements Comparator<User> {
	@Override
	public int compare(User u1, User u2) {
	    return ((Long) u1.getDistance()).compareTo(u2.getDistance());
	}
    }
}
