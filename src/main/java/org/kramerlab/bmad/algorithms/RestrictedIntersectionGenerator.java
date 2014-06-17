package org.kramerlab.bmad.algorithms;

import java.util.*;

import org.kramerlab.bmad.matrix.BooleanMatrix;
import static org.kramerlab.bmad.matrix.BooleanMatrix.*;

/**
 * Similar to IntersectionGenerator, this candidate generator creates candidates
 * by intersecting rows of the original matrix. However, the number of
 * candidates is the same as the number of rows in the original matrix, which
 * yields a much smaller number of candidates.
 */
public class RestrictedIntersectionGenerator implements CandidateGenerator {

	public BooleanMatrix generateCandidates(BooleanMatrix a, int dimension) {
		Set<Integer> availableIndices = new HashSet<Integer>();
		for (int i = 0; i < a.getHeight(); i++) {
			availableIndices.add(i);
		}
		int w = a.getWidth();
		int h = a.getHeight();
		BooleanMatrix result = new BooleanMatrix(h, w);
		for (int i = 0; i < h; i++) {
			int maxImplications = 0;
			int bestIndex = i;
			for (int j : availableIndices) {
				if (i != j) {
					int countImplications = 0;
					for (int c = 0; c < w; c++) {
						if (a.apply(i, c) == 2 && a.apply(j, c) == 2) {
							countImplications += 1;
						}
					}
					if (countImplications >= maxImplications) {
						maxImplications = countImplications;
						bestIndex = j;
					}
				}
			}
			// if (i != bestIndex) System.out.println("intersecting " + i +
			// " with " + bestIndex);
			for (int c = 0; c < w; c++) {
				result.update(i, c, 
				    ((a.apply(i, c) & a.apply(bestIndex, c)) == TRUE) ? TRUE : FALSE
				);
			}
			availableIndices.remove(i);
			availableIndices.remove(bestIndex);
		}
		return result;
	}

	@Override
	public String toString() {
		return "RestrictedIntersect";
	}
}
