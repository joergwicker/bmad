package nz.wicker.bmad.algorithms;

import nz.wicker.bmad.matrix.BooleanMatrix;

/**
 * A row combinator, which is just a composition of multiple smaller row
 * combinators. It takes the initial approximation of the combination matrix,
 * that is created as by-product in the second step, and sequentially applies
 * all sub-combinators to it. Particularly useful to append strictly improving
 * algorithms (like Iter) to some other combinator, that might generate a better
 * initial guess.
 */
public class CombinatorPipeline extends Combinator {

	Combinator[] combinators;

	public CombinatorPipeline(Combinator... combinators) {
		this.combinators = combinators;
	}

	@Override
	protected BooleanMatrix combineRow(BooleanMatrix row,
			BooleanMatrix previousCombination, BooleanMatrix basis,
			double onesWeight) {

		for (Combinator combinator : combinators) {
			previousCombination = combinator.combineMatrix(row,
					previousCombination, basis, onesWeight);
		}
		return previousCombination;
	}

	@Override
	public String toString() {
		boolean first = true;
		String res = "";
		for (Combinator comb : combinators) {
			if (!first)
				res += "+";
			res += comb;
			first = false;
		}
		return res;
	}
}
