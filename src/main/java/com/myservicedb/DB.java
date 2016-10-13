/**
 *
 */
package com.myservicedb;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jreece
 *
 */
public enum DB {

	ALL("all"), CENTRE("centre"), CENTRE_DIRECTORY("centre_directory"), COLLECTION(
			"collection"), DEAL("deal"), EVENT("event"), MOVIE("movie"), NOTICE(
					"notice"), RETAILER("retailer"), STORE("store");

	private final String value;

	private DB(final String value) {
		this.value = value;
	}

	/**
	 * @return the String form of this enumerated value
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * @return a list of the String forms of all enumerated values
	 */
	public static List<String> getValues() {
		final List<String> values = new ArrayList<String>();
		for (final DB f : DB.values()) {
			if (!DB.ALL.getValue().equals(f.getValue())) {
				values.add(f.getValue());
			}
		}
		return values;
	}

	public static DB forValue(final String value) {
		for (final DB f : DB.values()) {
			if (value.equals(f.getValue())) {
				return f;
			}
		}
		return null;
	}
}
