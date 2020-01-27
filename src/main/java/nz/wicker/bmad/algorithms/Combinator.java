package nz.wicker.bmad.algorithms;

import nz.wicker.bmad.matrix.*;

/**
 * The last component of the generic boolean matrix decomposition algorithm. It
 * calculates the combination matrix (first factor), which describes how the
 * rows in the original matrix can be represented as a combinations of basis
 * rows (second factor).
 */
public abstract class Combinator {
	/**
	 * Describes a row of the original matrix as combination of basis rows from
	 * the basis matrix.
	 * 
	 * @param row
	 *            a row from the original matrix
	 * @param previousCombination
	 *            approximation, already obtained by previous steps
	 * @param basis
	 *            the basis matrix (with basis patterns as rows)
	 * @param onesWeight
	 *            relative weight of <code>1 -&gt; 0</code> errors
	 * @return single row, that has a 0 or 1 entry for each basis row
	 */
	protected abstract BooleanMatrix combineRow(BooleanMatrix row,
			BooleanMatrix previousCombination, BooleanMatrix basis,
			double onesWeight);

	/**
	 * Calculates the combination matrix C, that describes, how each row of the
	 * original matrix A can be expressed as boolean combination of basis
	 * patterns from basis matrix B.
	 * 
	 * @param a
	 *            original matrix
	 * @param previousCombination
	 *            an already calculated approximation of the solution (this
	 *            matrix will be modified and returned by the algorithm)
	 * @param basis
	 *            basis matrix
	 * @param onesWeight
	 *            relative weight of <code>1 -&gt; 0</code> errors
	 * @return combination matrix C (first factor)
	 */
	public BooleanMatrix combineMatrix(BooleanMatrix a,
			BooleanMatrix previousCombination, BooleanMatrix basis,
			double onesWeight) {

		for (int r = 0; r < previousCombination.getHeight(); r++) {
			BooleanMatrix modifiedRow = combineRow(a.getRow(r),
					previousCombination.getRow(r), basis, onesWeight);
			previousCombination.setRow(r, modifiedRow);
		}

		return previousCombination;
	}
}
