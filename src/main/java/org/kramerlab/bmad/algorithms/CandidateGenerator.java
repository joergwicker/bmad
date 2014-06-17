package org.kramerlab.bmad.algorithms;

import org.kramerlab.bmad.matrix.*;

/**
 * First component of the generic boolean matrix decomposition algorithm.
 * 
 * It takes the original matrix as input, and generates a new matrix, with
 * candidate concepts in the rows.
 */
public interface CandidateGenerator {
	/**
	 * Generates candidates from the original matrix
	 * 
	 * @param a
	 *            original matrix
	 * @param dimension
	 *            number of basis rows (not used by most implementations)
	 * @return matrix with candidate concepts in the rows
	 */
	BooleanMatrix generateCandidates(BooleanMatrix a, int dimension);
}
