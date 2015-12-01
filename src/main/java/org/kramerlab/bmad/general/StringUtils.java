package org.kramerlab.bmad.general;

import java.util.*;

/**
 * Some basic String manipulation stuff, 
 * that seems somehow infeasible in standard 
 * java without giant external libraries.
 */
public class StringUtils {

	/**
	 * Converts a string to a new, mutable list of Characters.
	 * 
	 * @param string The string to be converted.
	 * @return the list of characters.
	 */
	public static ArrayList<Character> toList(String string) {
		ArrayList<Character> list = new ArrayList<Character>(string.length());
		for (char c: string.toCharArray()) {
			list.add(c);
		}
		return list;
	}
	
	/**
	 * Composes a String from the prefix, elements of an Iterable 
	 * separated by the separator, and the suffix.
	 * 
	 * @param it the Iterable to compose the String of.
	 * @param prefix the prefix to start the String with.
	 * @param separator the sepearator for composing.
	 * @param suffix the suffix to end the string with.
	 * @return the resulting String.
	 */
	public static String mkString(Iterable<?> it, String prefix, String separator, String suffix) {
		StringBuilder builder = new StringBuilder(prefix);
		boolean first = true;
		for (Object obj: it) {
			if (!first) {
				builder.append(separator);
			} else {
				first = false;
			}
			builder.append(obj.toString());
		}
		builder.append(suffix);
		return builder.toString();
	}
	
	/**
	 * Power-function in the free Monoid over Characters (AKA "Strings").
	 * 
	 * @param str the String to repeat.
	 * @param n how often the string is repeated.
	 * @return tge resulting string.
	 */
	public static String pow(String str, int n) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < n; i++) {
			b.append(str);
		}
		return b.toString();
	}
}
