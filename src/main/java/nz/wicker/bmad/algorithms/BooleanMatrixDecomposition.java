package nz.wicker.bmad.algorithms;

import nz.wicker.bmad.general.*;
import nz.wicker.bmad.matrix.*;
import static nz.wicker.bmad.general.Package.*;
import weka.core.Instances;

/**
 * Implementation of a generic boolean matrix decomposition algorithm, that fits
 * into this framework. Such an algorithm consists of three parts: candidate
 * generator, basis selector, and combinator. This implementation basically
 * invokes methods of the subcomponents in the correct order.
 */
public class BooleanMatrixDecomposition {

	protected CandidateGenerator generator;
	protected BasisSelector selector;
	protected Combinator combinator;
	protected double onesWeight;

	public BooleanMatrixDecomposition(CandidateGenerator generator,
			BasisSelector selector, Combinator combinator, double onesWeight) {

		super();
		this.generator = generator;
		this.selector = selector;
		this.combinator = combinator;
		this.onesWeight = onesWeight;
	}

	/**
	 * Decomposes matrix into two factor matrices.
	 * 
	 * @param a
	 *            original matrix
	 * @param dimension
	 *            number of basis rows
	 * @return <code>(combinationMatrix, basisMatrix)</code>
	 */
	public Tuple<BooleanMatrix, BooleanMatrix> decompose(BooleanMatrix a,
			int dimension) {

		BooleanMatrix candidates = generator.generateCandidates(a, dimension);
		Tuple<BooleanMatrix, BooleanMatrix> combinationAndBasis = selector
				.selectBasis(candidates, a, dimension, onesWeight);
		BooleanMatrix combination = combinationAndBasis._1;
		BooleanMatrix basis = combinationAndBasis._2;
		BooleanMatrix improvedCombination = combinator.combineMatrix(a,
				combination, basis, onesWeight);
		return tuple(improvedCombination, basis);
	}

	/**
	 * Thin wrapper around the other decompose method, that translates input and
	 * output into Weka Instances.
	 * 
	 * @param a
	 *            original matrix
	 * @param dimension
	 *            number of basis rows
	 * @return <code>(combinationMatrix, basisMatrix)</code>
	 */
	public Tuple<Instances, Instances> decompose(Instances a, int dimension) {
		Tuple<BooleanMatrix, BooleanMatrix> t = decompose(new BooleanMatrix(a),
				dimension);
		return tuple(t._1.toInstances(), t._2.toInstances());
	}

	@Override
	public String toString() {
		return "" + generator + "+" + selector + "+" + combinator;
	}
	
	/**
	 * Algorithm proposed by Miettinen for the BCX problem.
	 */
	public static BooleanMatrixDecomposition LOC_ITER = 
			new BooleanMatrixDecomposition(
					new IdentityGenerator(),
					new FastLoc(), 
					new Iter(), 1d);
	
	/**
	 * Algorithm proposed by Miettinen et. al for the DBP problem.
	 */
	public static BooleanMatrixDecomposition DBP = 
			new BooleanMatrixDecomposition(
					new AssociationGenerator(0.5),
					new GreedySelector(),
					new IdentityCombinator(), 1d);
	
	/**
	 * An algorithm that delivers best results (one the two examples we tested) 
	 * without further configuration.
	 */
	public static BooleanMatrixDecomposition BEST_UNCONFIGURED = 
			new BooleanMatrixDecomposition(
					new IdentityGenerator(),
					new FastLoc(),
					new CombinatorPipeline(new DensityGreedyCombinator(), 
					new Iter()), 1d);
	
	/**
	 * Promising algorithm, that requires one further parameter to be learned.
	 * 
	 * @param assocThreshold parameter for the candidate generation step
	 * @return Assoc+FastLoc+DensGreedy+Iter
	 */
	public static BooleanMatrixDecomposition 
		BEST_CONFIGURED(double assocThreshold) {
		
		return new BooleanMatrixDecomposition(
				new AssociationGenerator(assocThreshold),
				new FastLoc(),
				new CombinatorPipeline(new DensityGreedyCombinator(), 
				new Iter()), 1d);
	}
}
