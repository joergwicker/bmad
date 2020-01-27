package nz.wicker.bmad.algorithms;

import nz.wicker.bmad.matrix.BooleanMatrix;

/**
 * Combinator (3rd part of the algorithm), that tries to use the basis rows with
 * maximal "improvement density".
 * 
 * "Environment-friendly" basis rows with fewer ones are preferred over basis
 * rows with many ones, if their "usefulness-density" is higher, even if the
 * absolute reconstruction error decrease is smaller.
 */
public class DensityGreedyCombinator extends GreedyCombinator {

	@Override
	protected double usefulness(Cover cover, BooleanMatrix row,
			BooleanMatrix basisRow) {

		return cover.coverChangeDensityOnInclusion(row, basisRow);
	}

	@Override
	public String toString() {
		return "DensGreed";
	}
}