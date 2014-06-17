package org.kramerlab.bmad.algorithms;

import java.util.Arrays;
import org.kramerlab.bmad.general.*;
import org.kramerlab.bmad.matrix.*;
import static org.kramerlab.bmad.matrix.BooleanMatrix.*;

import static org.kramerlab.bmad.general.Package.*;

/**
 * A structure that stores all information necessary to predict changes of a
 * weighted cover function for one single row.
 * 
 * This implementation properly handles unknown values, which turned out to be
 * not really necessary, since all unknown values are usually removed from the
 * basis vectors. Theoretically, this implementation would be more useful, if
 * one changed the reconstruction error function in a way that predicting an
 * unknown value is rewarded.
 * 
 * Works only for basis dimensions less than 2^15
 */
public class Cover {

	// weight of ones. weight of zero is set to 1 without loss of generality
	protected double onesWeight;
	protected int width;
	protected int[] entries;
	private static int trueMultiplicator = 1 << 15;

	/**
	 * Constructs a cover row
	 * 
	 * @param width
	 * @param onesWeight
	 */
	public Cover(int width, double onesWeight) {
		this.width = width;
		this.onesWeight = onesWeight;
		this.entries = new int[width];
	}

	/**
	 * returns tuple <code>(#TRUE, #UNKNOWN)</code>
	 * 
	 * @param c
	 *            index of the column
	 * @return
	 */
	public Tuple<Integer, Integer> apply(int c) {
		int i = entries[c];
		return tuple(i / trueMultiplicator, i % trueMultiplicator);
	}

	/**
	 * updates entry of this cover
	 * 
	 * @param c
	 *            index of a column
	 * @param ones
	 *            #TRUE
	 * @param unknowns
	 *            #UNKNOWN
	 */
	public void update(int c, int ones, int unknowns) {
		entries[c] = trueMultiplicator * ones + unknowns;
	}

	/**
	 * Includes a basis row into this cover
	 * 
	 * @param basisRow
	 */
	public void include(BooleanMatrix basisRow) {
		for (int c = 0; c < width; c++) {
			int b = basisRow.apply(c);
			if (b != 0) {
				entries[c] += (b == UNKNOWN ? 1 : trueMultiplicator);
			}
		}
	}

	/**
	 * Excludes a basis row from this cover
	 * 
	 * @param basisRow
	 */
	public void exclude(BooleanMatrix basisRow) {
		for (int c = 0; c < width; c++) {
			int b = basisRow.apply(c);
			if (b != 0) {
				entries[c] -= (b == UNKNOWN ? 1 : trueMultiplicator);
			}
		}
	}

	/**
	 * Predicts the number of covered ones and zeros, given the row that is
	 * being covered, and the basis row that could be included into this cover.
	 * Has no side effects.
	 * 
	 * @param coveredRow
	 *            boolean matrix row, this cover is associated with
	 * @param basisRow
	 *            boolean matrix row, that potentially could be included into
	 *            this cover
	 * @return <code>(#1, #0)</code> covered in case of inclusion
	 */
	public Tuple<Integer, Integer> coveredOnesAndZerosOnInclusion(
			BooleanMatrix coveredRow, BooleanMatrix basisRow) {
		int coveredOnes = 0;
		int coveredZeros = 0;

		for (int c = 0; c < width; c++) {
			byte aEntry = coveredRow.apply(c);
			if (aEntry == TRUE && entries[c] < trueMultiplicator
					&& basisRow.apply(c) == TRUE) {
				coveredOnes++;
			} else if (aEntry == FALSE && entries[c] == 0 && basisRow.apply(c) != FALSE) {
				coveredZeros++;
			}
		}

		return tuple(coveredOnes, coveredZeros);
	}

	/**
	 * analogous to coveredOnesAndZerosOnInclusion
	 * 
	 * @param coveredRow
	 * @param basisRow
	 * @return
	 */
	public Tuple<Integer, Integer> uncoveredOnesAndZerosOnExclusion(
			BooleanMatrix coveredRow, BooleanMatrix basisRow) {
		int uncoveredOnes = 0;
		int uncoveredZeros = 0;

		for (int c = 0; c < width; c++) {
			byte aEntry = coveredRow.apply(c);
			if (aEntry == 0) {
				int cEntry = entries[c];
				int bEntry = basisRow.apply(c);
				if ((cEntry == trueMultiplicator && bEntry == TRUE)
						|| (cEntry == 1 && bEntry == UNKNOWN)) {
					uncoveredZeros++;
				}
			} else if (aEntry == TRUE && entries[c] / trueMultiplicator == 1
					&& basisRow.apply(c) == TRUE) {
				uncoveredOnes++;
			}
		}

		return tuple(uncoveredOnes, uncoveredZeros);
	}

	/**
	 * weighted sum of the results returned by coveredOnesAndZerosOnInclusion
	 * 
	 * @param coveredRow
	 * @param basisRow
	 * @return <code>onesWeight * #1 + #0</code>
	 */
	public double coverChangeOnInclusion(BooleanMatrix coveredRow,
			BooleanMatrix basisRow) {
		Tuple<Integer, Integer> t = coveredOnesAndZerosOnInclusion(coveredRow,
				basisRow);
		return onesWeight * t._1 - t._2;
	}

	/**
	 * analogous to coverChangeOnInclusion
	 * 
	 * @param coveredRow
	 * @param basisRow
	 * @return
	 */
	public double coverChangeOnExclusion(BooleanMatrix coveredRow,
			BooleanMatrix basisRow) {
		Tuple<Integer, Integer> t = uncoveredOnesAndZerosOnExclusion(
				coveredRow, basisRow);
		return t._2 - onesWeight * t._1;
	}

	/**
	 * weighted cover change (indicating "direction" of the change), divided by
	 * the weighted number of changed entries (indicating "magnitude" of the
	 * change)
	 * 
	 * @param coveredRow
	 * @param basisRow
	 * @return
	 */
	public double coverChangeDensityOnInclusion(BooleanMatrix coveredRow,
			BooleanMatrix basisRow) {
		Tuple<Integer, Integer> t = coveredOnesAndZerosOnInclusion(coveredRow,
				basisRow);
		double denominator = onesWeight * t._1 + t._2;
		return (denominator == 0 ? -1
				: ((onesWeight * t._1 - t._2) / denominator));
	}

	/**
	 * transforms this cover to a boolean matrix
	 * 
	 */
	public BooleanMatrix toBooleanMatrix() {
		BooleanMatrix row = new BooleanMatrix(1, width);
		for (int i = 0; i < width; i++) {
			Tuple<Integer, Integer> t = this.apply(i);
			if (t._1 > 0) {
				row.update(i, TRUE);
			} else if (t._2 > 0) {
				row.update(i, UNKNOWN);
			}
		}
		return row;
	}

	/**
	 * Creates a new zero cover of specified width
	 * 
	 * @param width
	 * @return
	 */
	public Cover zero(int width) {
		return new Cover(width, onesWeight);
	}

	@Override
	public String toString() {
		String res = "[";
		for (int c = 0; c < width; c++) {
			res += apply(c);
		}
		res += "]";
		return res;
	}

	@Override
	public int hashCode() {
	    System.err.println("Do we ever use hash code of covers?...");
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(entries);
		long temp;
		temp = Double.doubleToLongBits(onesWeight);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + width;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Cover other = (Cover) obj;
		if (!Arrays.equals(entries, other.entries))
			return false;
		if (Double.doubleToLongBits(onesWeight) != Double
				.doubleToLongBits(other.onesWeight))
			return false;
		if (width != other.width)
			return false;
		return true;
	}
}
