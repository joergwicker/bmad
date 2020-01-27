package nz.wicker.bmad.algorithms;

import nz.wicker.bmad.matrix.BooleanMatrix;

/**
 * Column combinator (3rd part of the algorithm), that tries to minimize the
 * absolute reconstruction error in every step. Originally proposed as 3rd step
 * of Miettinen's DBP algorithm.
 */
public class CoverGreedyCombinator extends GreedyCombinator {

	@Override
	protected double usefulness(Cover cover, BooleanMatrix row,
			BooleanMatrix basisRow) {

		return cover.coverChangeOnInclusion(row, basisRow);
	}

	@Override
	public String toString() {
		return "CovGreed";
	}
}
