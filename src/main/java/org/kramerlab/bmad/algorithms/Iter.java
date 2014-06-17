package org.kramerlab.bmad.algorithms;

import org.kramerlab.bmad.matrix.BooleanMatrix;
import static org.kramerlab.bmad.matrix.BooleanMatrix.*;

/**
 * Combinator proposed as third part of Miettinen's BCX algorithm. It is a
 * strictly improving combinator, that means, that it maybe improves, but never
 * decreases the reconstruction accuracy.
 */
public class Iter extends Combinator {

	@Override
	protected BooleanMatrix combineRow(BooleanMatrix row,
			BooleanMatrix previousCombination, BooleanMatrix basis,
			double onesWeight) {

		int dim = basis.getHeight();

		// create cover matrix to keep track of all changes of the cover
		// function
		Cover cover = new Cover(row.getWidth(), onesWeight);
		for (int c = 0; c < dim; c++) {
			if (previousCombination.apply(c) == TRUE) {
				cover.include(basis.getRow(c));
			}
		}

		boolean approximationImproves = true;
		while (approximationImproves) {
			approximationImproves = false;

			for (int c = 0; c < dim; c++) {
				// try to improve by flipping the entry in the
				// previousCombination row
				// if improvement => approximation improves, keep iterating
				byte combinationEntry = previousCombination.apply(c);
				BooleanMatrix basisRow = basis.getRow(c);
				if (combinationEntry == FALSE) {
					// this row is currently excluded
					// check if it would increase the cover function if we
					// include it
					if (cover.coverChangeOnInclusion(row, basisRow) > 0) {
						cover.include(basisRow);
						previousCombination.update(c, TRUE);
						approximationImproves = true;
					}
				} else if (combinationEntry == TRUE) {
					// this row is currently included
					// check if we can do at least as good without it
					if (cover.coverChangeOnExclusion(row, basisRow) >= 0) {
						cover.exclude(basisRow);
						previousCombination.update(c, FALSE);
						approximationImproves = true;
					}
				}
			}
		}
		return previousCombination;
	}

	@Override
	public String toString() {
		return "Iter";
	}
}
