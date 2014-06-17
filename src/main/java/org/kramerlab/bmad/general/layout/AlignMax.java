package org.kramerlab.bmad.general.layout;

import java.util.*;

public class AlignMax extends Padding {

	@Override
	public <X> List<X> pad(int length, X filler, List<X> seq) {
		ArrayList<X> result = new ArrayList<X>(length);
		for (int i = seq.size(); i < length; i++) {
			result.add(filler);
		}
		result.addAll(seq);
		return result;
	}
	
}
