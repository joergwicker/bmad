package org.kramerlab.bmad.general.layout;

import java.util.List;

public class ZeroPadding extends Padding {

	@Override
	public <X> List<X> pad(int length, X filler, List<X> seq) {
		return seq;
	}
  
}
