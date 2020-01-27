package nz.wicker.bmad.algorithms;

import static nz.wicker.bmad.general.Package.*;
import static java.lang.Math.*;
import java.util.*;

import nz.wicker.bmad.general.Tuple;
import nz.wicker.bmad.matrix.BooleanMatrix;
import static nz.wicker.bmad.matrix.BooleanMatrix.*;

/**
 * Basis Selector algorithm, that greedily selects the basis rows, estimating
 * the reconstruction error by keeping an approximation of the combination
 * matrix, which is also updated greedily in each step.
 * 
 * Similar to what was proposed as second step of Miettinen's DBP algorithm.
 */
public class GreedySelector implements BasisSelector {

	public Tuple<BooleanMatrix, BooleanMatrix> selectBasis(
			BooleanMatrix candidates, BooleanMatrix a, int dimension,
			double onesWeight) {

		if (dimension > candidates.getHeight()) {
			throw new IllegalArgumentException(
					"dimension too high: cannot choose " + dimension
							+ " basis rows out of " + candidates.getHeight()
							+ " candidates!");
		}

		int w = a.getWidth();
		int h = a.getHeight();
		int numCandidates = candidates.getHeight();

		// initialize cover
		ArrayList<Cover> cover = new ArrayList<Cover>(numCandidates);
		for (int r = 0; r < h; r++) {
			cover.add(new Cover(w, onesWeight));
		}

		// initialize the permutation
		ArrayList<Integer> permutation = new ArrayList<Integer>();
		for (int i = 0; i < numCandidates; i++) {
			permutation.add(i);
		}

		Collections.shuffle(permutation);

		// use same optimization as for FastLoc
		// calculate cover densities between candidate rows and rows of A
		double threshold = 0;
		int[][] potentiallyCoverableRowsOfA = new int[numCandidates][];
		Cover zeroCover = new Cover(w, onesWeight);
		for (int cand = 0; cand < numCandidates; cand++) {
			BooleanMatrix basisRow = candidates.getRow(cand);
			List<Integer> list = new LinkedList<Integer>();
			for (int aRow = 0; aRow < h; aRow++) {
				if (zeroCover.coverChangeDensityOnInclusion(a.getRow(aRow),
						basisRow) > threshold) {
					list.add(aRow);
				}
			}
			potentiallyCoverableRowsOfA[cand] = new int[list.size()];
			int i = 0;
			for (Integer e : list) {
				potentiallyCoverableRowsOfA[cand][i++] = e;
			}
		}

		// initialize combination matrix
		BooleanMatrix combination = new BooleanMatrix(h, dimension);

		// iterate once
		// go through all basis rows, and try to replace each one by something
		// else
		for (int b = 0; b < dimension; b++) {

			// current permutation index is vacuously the best seen so far
			int bestPermutationIndex = b;
			// how much one looses, if one removes this row from cover
			double bestCoverChange = 0;

			// remove the b'th basis row from the cover
			BooleanMatrix actualCandidate = candidates.getRow(permutation
					.get(b));
			for (int aRow = 0; aRow < h; aRow++) {
				Cover currentCover = cover.get(aRow);
				if (combination.apply(aRow, b) == TRUE) {
					combination.update(aRow, b, FALSE);
					bestCoverChange -= currentCover.coverChangeOnExclusion(
							a.getRow(aRow), actualCandidate);
					currentCover.exclude(actualCandidate);
				}
			}

			// find the best replacement
			for (int alternativePermutationIndex = dimension; 
					alternativePermutationIndex < permutation.size(); 
					alternativePermutationIndex++) {
				BooleanMatrix alternativeCandidate = candidates
						.getRow(permutation.get(alternativePermutationIndex));
				double alternativeCoverChange = 0;

				// this loop is slightly optimized, but avoids some of
				// unnecessary checks
				for (int aRow : potentiallyCoverableRowsOfA[permutation
						.get(alternativePermutationIndex)]) {
					alternativeCoverChange += max(
							0,
							cover.get(aRow).coverChangeOnInclusion(
									a.getRow(aRow), alternativeCandidate));
				}
				if (alternativeCoverChange > bestCoverChange) {
					bestCoverChange = alternativeCoverChange;
					bestPermutationIndex = alternativePermutationIndex;
				}
			}

			// check if there is some change
			if (b != bestPermutationIndex) {
				// swap entries at bestPermutationIndex and b
				int temp = permutation.get(b);
				permutation.set(b, permutation.get(bestPermutationIndex));
				permutation.set(bestPermutationIndex, temp);
			}

			// add the new basis column back to the cover
			BooleanMatrix newBasisRow = candidates.getRow(permutation.get(b));
			for (int aRow : potentiallyCoverableRowsOfA[permutation.get(b)]) {
				Cover currentCover = cover.get(aRow);
				if (currentCover.coverChangeOnInclusion(a.getRow(aRow),
						newBasisRow) > 0) {
					currentCover.include(newBasisRow);
					combination.update(aRow, b, TRUE);
				}
			}
		}

		// create basis matrix
		BooleanMatrix basis = new BooleanMatrix(dimension, w);
		for (int b = 0; b < dimension; b++) {
			basis.setRow(b, candidates.getRow(permutation.get(b)));
		}

		return tuple(combination, basis);
	}

	@Override
	public String toString() {
		return "Greedy";
	}
}
