package nz.wicker.bmad.algorithms;


import java.util.HashSet;

import nz.wicker.bmad.matrix.BooleanMatrix;
import static nz.wicker.bmad.matrix.BooleanMatrix.*;

/**
 * Abstract greedy Combinator (third step of the algorithm). It greedily
 * includes basis rows of maximal "usefulness", until no improvement is
 * possible. Subclasses just have to override the usefulness method.
 */
public abstract class GreedyCombinator extends Combinator {

	protected abstract double usefulness(Cover cover, BooleanMatrix row,
			BooleanMatrix basisRow);

	@Override
	public BooleanMatrix combineRow(BooleanMatrix row, BooleanMatrix bm,
			BooleanMatrix basis, double onesWeight) {

		int dim = basis.getHeight();
		BooleanMatrix combination = new BooleanMatrix(1, dim);
		HashSet<Integer> remainingIndices = new HashSet<Integer>();
		for (int i = 0; i < dim; i++) {
			remainingIndices.add(i);
		}
		Cover cover = new Cover(row.getWidth(), onesWeight);

		while (true) {
			double maxUsefulness = Double.NEGATIVE_INFINITY;
			int bestIndex = -1;
			for (int i : remainingIndices) {
				BooleanMatrix basisRow = basis.getRow(i);
				double usefulness = usefulness(cover, row, basisRow);
				if (usefulness > maxUsefulness) {
					maxUsefulness = usefulness;
					bestIndex = i;
				}
			}
			if (maxUsefulness > 0) {
				remainingIndices.remove(bestIndex);
				cover.include(basis.getRow(bestIndex));
				combination.update(bestIndex, TRUE);
			} else {
				break;
			}
		}

		return combination;
	}
}
