package org.kramerlab.bmad.general.layout;

import java.util.*;

public class AlignMin extends Padding {

	@Override
	public <X> List<X> pad(int length, X filler, List<X> seq) {
		ArrayList<X> result = new ArrayList<X>(length);
		result.addAll(seq);
		for (int i = seq.size(); i < length; i++) {
			result.add(filler);
		}
		return result;
	}
	
}