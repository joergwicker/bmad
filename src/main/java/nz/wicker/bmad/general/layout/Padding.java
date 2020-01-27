package nz.wicker.bmad.general.layout;

import java.util.*;

import static nz.wicker.bmad.general.StringUtils.*;

/**
 * Abstract class that represent a padding strategy. 
 * Contains methods for padding Strings and 
 * Strings that represent blocks with multiple lines.
 *
 */
public abstract class Padding {
	
	public abstract <X> List<X> pad(int length, X filler, List<X> seq);

	public String padLine(int length, char filler, String string) {
		return mkString(pad(length, filler, toList(string)), "", "", "");
	}
	
	public String padBlockHorizontally(int width, char filler, String block) {
		StringBuilder b = new StringBuilder();
		for (String line: block.split("\\n")) {
			b.append(padLine(width, filler, line) + "\n");
		}
		return b.toString();
	}
	
	public String padBlockVertically(int height, char filler, String block) {
		List<String> lines = Arrays.asList(block.split("\n"));
		String fillerLine = pow("" + filler, lines.get(0).length());
		return mkString(pad(height, fillerLine, lines), "", "\n", "");
	}
}
