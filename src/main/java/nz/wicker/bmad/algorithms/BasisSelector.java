package nz.wicker.bmad.algorithms;

import nz.wicker.bmad.general.*;
import nz.wicker.bmad.matrix.*;

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
	 *            relative weight of <code>1 -&gt; 0</code> errors, opposed to <code>0 -&gt; 1</code> errors
	 * @return (combination, basis)
	 */
	Tuple<BooleanMatrix, BooleanMatrix> selectBasis(BooleanMatrix candidates,
			BooleanMatrix a, int dimension, double onesWeight);
}
