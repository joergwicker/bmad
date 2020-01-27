package nz.wicker.bmad.algorithms;

import nz.wicker.bmad.matrix.BooleanMatrix;
import static nz.wicker.bmad.matrix.BooleanMatrix.*;

/**
 * Implementation of the Association candidate generation method, which was used
 * as first part of Miettinen's DBP algorithm.
 * 
 * This algorithm has an additional parameter: confidence threshold. The results
 * of the reconstruction strongly depend on this threshold.
 */
public class AssociationGenerator implements CandidateGenerator {

	protected double confidenceThreshold;

	public AssociationGenerator(double confidenceThreshold) {
		this.confidenceThreshold = confidenceThreshold;
	}

	public BooleanMatrix generateCandidates(BooleanMatrix a, int dimension) {
		int w = a.getWidth();
		BooleanMatrix result = new BooleanMatrix(w, w);
		for (int r = 0; r < w; r++) {
			for (int c = 0; c < w; c++) {
				int ones = 0;
				int implications = 0;
				for (int k = 0; k < a.getHeight(); k++) {
					if (a.apply(k, r) == TRUE) {
						ones++;
						if (a.apply(k, c) == TRUE) {
							implications++;
						}
					}
				}
				if (implications > confidenceThreshold * ones) {
					result.update(r, c, TRUE);
				}
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return "Assoc[" + confidenceThreshold + "]";
	}
}
