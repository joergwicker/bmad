package nz.wicker.bmad.test.performance;

import java.io.*;
import java.util.*;

/**
 * Sorts the results of experiment by performance (helps to see, which 
 * algorithm performs better). 
 *
 */
public class SortFileByPerformance {

	public static void main(String[] args) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader("allAlgorithmsRealData.txt"));
		TreeSet<String> tree = new TreeSet<String>(new Comparator<String>() {

			public int compare(String o1, String o2) {
				String[] split1 = o1.split(" ");
				String[] split2 = o2.split(" ");
				String suffix1 = split1[split1.length-1];
				String suffix2 = split2[split2.length-1];
				int compSuff = suffix1.compareTo(suffix2);
				if (compSuff == 0) {
					return o1.compareTo(o2);	
				} else {
					return compSuff;
				}
			}
			
		});
		String line = "";
		while( (line = reader.readLine()) != null) {
			tree.add(line);
		}
		
		for (String s: tree) {
			System.out.println(s);
		}
	}
	
}
