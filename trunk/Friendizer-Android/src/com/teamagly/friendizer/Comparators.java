/**
 * 
 */
package com.teamagly.friendizer;

import java.util.Comparator;

/**
 * @author Guy
 * 
 */
public class Comparators {
    public class AlphabetComparator implements Comparator<FBUserInfo> {
	@Override
	public int compare(FBUserInfo u1, FBUserInfo u2) {
	    return u1.name.compareTo(u2.name);
	}
    }
    // public class MatchingComparator implements Comparator<UserInfo> {
    // @Override
    // public int compare(UserInfo u1, UserInfo u2) {
    // return u1.compareTo(u2.name);
    // }
    // }
}
