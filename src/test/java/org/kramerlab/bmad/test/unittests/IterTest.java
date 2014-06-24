package org.kramerlab.bmad.test.unittests;

import static org.junit.Assert.*;
import org.junit.Test;

import org.kramerlab.bmad.algorithms.*;
import org.kramerlab.bmad.general.*;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import java.util.*;
import org.kramerlab.bmad.randommatrixgeneration.*;

public class IterTest {

	/**
	 * testing a somewhat suspicious fact, that Iter does not seem to improve
	 * anything, and sometimes even makes the results worse. By definition, Iter
	 * should never decrease reconstruction accuracy.
	 */
	@Test
	public void testIterImprovement() {
		CandidateGenerator[] generators = {
			new AssociationGenerator(0.5),
			new IdentityGenerator(),
			new RestrictedIntersectionGenerator()
		};
		
		BasisSelector[] selectors = {
			new FastLoc(),
			new GreedySelector()
		};
		
		Combinator[] firstCombinators = {
			new IdentityCombinator(),
			new DensityGreedyCombinator(),
			new CoverGreedyCombinator()
		};
		
		int maxWidth = 128;
		int maxHeight = 128;
		int tries = 20;
		int basisDimension = 16;
		Random rnd = new Random();
		
		for (int t = 0; t < tries; t++) {
			for (CandidateGenerator generator: generators) {
				for (BasisSelector selector: selectors) {
					for (Combinator firstCombinator: firstCombinators) {
						int height = rnd.nextInt(maxHeight) + 16;
						int width = rnd.nextInt(maxWidth) + 16;
						BooleanMatrix original = RandomMatrixGeneration.randomProductMatrix(
								height, width, basisDimension, 0.2, 0.3, 0.1, 0.05);
						
						BooleanMatrix candidates = generator.generateCandidates(original, basisDimension);
						Tuple<BooleanMatrix, BooleanMatrix> combBas = selector.selectBasis(candidates, original, basisDimension, 1);
						BooleanMatrix combinationFromSelector = combBas._1;
						BooleanMatrix basis = combBas._2;
						BooleanMatrix firstCombination = firstCombinator.combineMatrix(original, combinationFromSelector, basis, 1);
						double firstError = original.reconstructionError(firstCombination.booleanProduct(basis), 1d);
						BooleanMatrix secondCombination = new Iter().combineMatrix(original, firstCombination, basis, 1d);
						double secondError = original.reconstructionError(secondCombination.booleanProduct(basis), 1d);
						assertTrue("Second error " + secondError + " should be at most as large as first error " + firstError, firstError >= secondError);
					}
				}
			}
		}
	}
}
