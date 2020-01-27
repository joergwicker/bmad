package nz.wicker.bmad.algorithms;

import static nz.wicker.bmad.general.Package.*;
import static java.lang.Math.*;
import java.util.*;
import java.math.BigDecimal;

import nz.wicker.bmad.general.Tuple;
import nz.wicker.bmad.matrix.BooleanMatrix;
import static nz.wicker.bmad.matrix.BooleanMatrix.*;

/**
 * A minor modification of Local search heuristic,
 * proposed by Miettinen as second part of his BCX algorithm.
 */
public class FastLoc implements BasisSelector {
	
    public Tuple<BooleanMatrix, BooleanMatrix> selectBasis(
							   BooleanMatrix candidates, 
							   BooleanMatrix a, 
							   int dimension,
							   double onesWeight) {
		
	if (dimension > candidates.getHeight()) {
	    throw new IllegalArgumentException(
					       "dimension too high: cannot choose " + 
					       dimension + " basis rows out of " + 
					       candidates.getHeight() + " candidates!");
	}
		
	int w = a.getWidth();
	int h = a.getHeight();
	int numCandidates = candidates.getHeight();
		
	// initialize cover
	ArrayList<Cover> cover = new ArrayList<Cover>(numCandidates);
	for (int r = 0; r < h; r++) {
	    cover.add(new Cover(w, onesWeight));
	}
		
	// initialize permutation
	ArrayList<Integer> permutation = new ArrayList<Integer>();
	for (int i = 0; i < numCandidates; i++) {
	    permutation.add(i);
	}
		
	Collections.shuffle(permutation);
		
	// calculate cover densities between candidate rows and rows of A
	double threshold = 0;
	int[][] potentiallyCoverableRowsOfA = new int[numCandidates][];
	Cover zeroCover = new Cover(w, onesWeight);
	for (int cand = 0; cand < numCandidates; cand++) {
	    BooleanMatrix basisRow = candidates.getRow(cand);
	    List<Integer> list = new LinkedList<Integer>();
	    for (int aRow = 0; aRow < h; aRow++) {
		if (zeroCover.coverChangeDensityOnInclusion(a.getRow(aRow), basisRow) > threshold) {
		    list.add(aRow);
		}
	    }
	    potentiallyCoverableRowsOfA[cand] = new int[list.size()];
	    int i = 0;
	    for (Integer e: list) {
		potentiallyCoverableRowsOfA[cand][i++] = e;
	    }
	}
		
	//initialize C
	BooleanMatrix combination = new BooleanMatrix(h, dimension);
		
	// iterate while there is some improvement
	boolean basisImproves = true;
		
	while (basisImproves) {
	    basisImproves = false;
	    // go through all basis rows, and try to replace each one by something else
	    for (int b = 0; b < dimension; b++) {
				
		// current permutation index is vacuously the best seen so far
		int bestPermutationIndex = b;
		// how much one looses, if one removes this row from cover
		BigDecimal bestCoverChange = new BigDecimal(0);
				
		// remove the b'th basis row from the cover
				
		BooleanMatrix actualCandidate = candidates.getRow(permutation.get(b));
		for (int aRow = 0; aRow < h; aRow++) {
		    Cover currentCover = cover.get(aRow);
		    if (combination.apply(aRow, b) == TRUE) {
			combination.update(aRow, b, FALSE);
			bestCoverChange.subtract(new BigDecimal(currentCover.coverChangeOnExclusion(a.getRow(aRow), actualCandidate)));
			currentCover.exclude(actualCandidate);
		    }
		}
				
		// find the best replacement
		for (int alternativePermutationIndex = dimension; 
		     alternativePermutationIndex < permutation.size(); 
		     alternativePermutationIndex++) {
		    BooleanMatrix alternativeCandidate = candidates.getRow(permutation.get(alternativePermutationIndex));
		    BigDecimal alternativeCoverChange = new BigDecimal(0);
					
		    // this loop is slightly optimized, but avoids some of unnecessary checks
		    for (int aRow: potentiallyCoverableRowsOfA[permutation.get(alternativePermutationIndex)]) {
			alternativeCoverChange.add(new BigDecimal(max(0, cover.get(aRow).coverChangeOnInclusion(a.getRow(aRow), alternativeCandidate)))); 
		    }
		    if (alternativeCoverChange.compareTo(bestCoverChange) > 0 ) {
			bestCoverChange = alternativeCoverChange;
			bestPermutationIndex = alternativePermutationIndex;
		    }
		}
				
		// check if there is some change
		if (b != bestPermutationIndex) {
		    basisImproves = true;
		    // swap entries at bestPermutationIndex and b
		    int temp = permutation.get(b);
		    permutation.set(b, permutation.get(bestPermutationIndex));
		    permutation.set(bestPermutationIndex, temp);
		}
				
		// add the new basis column back to the cover
		BooleanMatrix newBasisRow = candidates.getRow(permutation.get(b));
		for (int aRow: potentiallyCoverableRowsOfA[permutation.get(b)]) {
		    Cover currentCover = cover.get(aRow);
		    if (currentCover.coverChangeOnInclusion(a.getRow(aRow), newBasisRow) > 0) {
			currentCover.include(newBasisRow);
			combination.update(aRow, b, TRUE);
		    }
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
	return "FastLoc";
    }
}
