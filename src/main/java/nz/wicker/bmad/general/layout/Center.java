package nz.wicker.bmad.general.layout;

import java.util.ArrayList;
import java.util.List;

public class Center extends Padding {

	@Override
	public <X> List<X> pad(int length, X filler, List<X> seq) {
		ArrayList<X> result = new ArrayList<X>(length);
		int toPad = length - seq.size();
		int head = toPad / 2;
		int tail = toPad - head;
		
		for (int i = 0; i < head; i++) {
			result.add(filler);
		}
		
		result.addAll(seq);
		
		for (int i = 0; i < tail; i++) {
			result.add(filler);
		}
		
		return result;
	}
	
}
