package org.kramerlab.bmad.algorithms;

import org.kramerlab.bmad.matrix.BooleanMatrix;
import static org.kramerlab.bmad.matrix.BooleanMatrix.*;
import static java.lang.Math.*;


/**
 * Candidate generator that takes all pairwise intersections of rows of the
 * original matrix as candidates. All unknowns are replaced by zeros.
 * 
 * Notice that this algorithm generates a quadratic number of candidate 
 * vectors, which in turn usually leads to inacceptable runtimes in 
 * subsequent steps.
 */
public class IntersectionGenerator implements CandidateGenerator {

	public BooleanMatrix generateCandidates(BooleanMatrix a, int dimension) {
		int w = a.getWidth();
		int h = a.getHeight();
		BooleanMatrix result = new BooleanMatrix(h * h, w);
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < h; j++) {
				int r = i * h + j;
				for (int k = 0; k < w; k++) {
					result.update(r, k,
					    (a.apply(i, k) & a.apply(j, k)) == TRUE ? TRUE : FALSE
					);
				}
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return "Intersect";
	}
}
