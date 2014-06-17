package org.kramerlab.bmad.algorithms;

import org.kramerlab.bmad.general.*;
import org.kramerlab.bmad.matrix.*;

/**
 * This is the second component of the BMD algorithm. It's purpose is to select
 * a basis out of candidates, generated in the first step. As a by-product, it
 * usually generates a coarse approximation of the combination matrix.
 */
public interface BasisSelector {
	/**
	 * Calculates a boolean basis matrix B (second factor), and (optionally) a
	 * coarse approximation of the combination matrix C (first factor).
	 * 
	 * @param candidates
	 *            matrix that contains candidate patterns in it's rows
	 * @param a
	 *            original matrix
	 * @param dimension
	 *            number of candidate rows to be included into the basis matrix
	 * @param onesWeight
	 *            relative weight of 1 -> 0 errors, opposed to 0 -> 1 errors
	 * @return (combination, basis)
	 */
	Tuple<BooleanMatrix, BooleanMatrix> selectBasis(BooleanMatrix candidates,
			BooleanMatrix a, int dimension, double onesWeight);
}
