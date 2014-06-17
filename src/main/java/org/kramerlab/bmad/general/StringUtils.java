package org.kramerlab.bmad.general;

import java.util.*;

/**
 * Some basic String manipulation stuff, 
 * that seems somehow infeasible in standard 
 * java without giant external libraries.
 */
public class StringUtils {

	/**
	 * Converts a string to a new, mutable list of Characters
	 * 
	 * @param string
	 * @return
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
	 * @param it
	 * @param prefix
	 * @param separator
	 * @param suffix
	 * @return
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
	 * Power-function in the free Monoid over Characters (AKA "Strings")
	 * 
	 * @param str
	 * @param n
	 * @return
	 */
	public static String pow(String str, int n) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < n; i++) {
			b.append(str);
		}
		return b.toString();
	}
}