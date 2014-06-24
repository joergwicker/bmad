package org.kramerlab.bmad.test.performance;

import static java.lang.System.*;
import java.io.*;

import org.kramerlab.bmad.algorithms.*;
import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import org.kramerlab.bmad.randommatrixgeneration.RandomMatrixGeneration;
import org.kramerlab.bmad.visualization.DecompositionLayout;

public class CombinationAccuracyTest {
	public static void main(String[] args) throws Exception {
		
		String[] files = {
			"kinase_labels.arff", 
			"labels.nci.disc.noid.arff", 
			"labels.gene.arff"
		};
		
		/*
		 * Accuracy experiment 1: 
		 * Testing reconstruction accuracy for three different combinators
		 * with a fixed generator and a fixed selector.
		 */

		{
			FileOutputStream fos = new FileOutputStream("combinatorsAccuracy.dat");
			//System.setOut(new PrintStream(fos));
			System.out.println("# comparison of combinators on slightly noisy synthetic data of varying density");
			
			CandidateGenerator generator = new RestrictedIntersectionGenerator();//new AssociationGenerator(0.75);
			BasisSelector selector = new FastLoc();
			Combinator[] combinators = {new IdentityCombinator(), new Iter(), new CoverGreedyCombinator(), new DensityGreedyCombinator()};
			double[] densities = {0.01, 0.02, 0.03, 0.05, 0.1, 0.15, 0.2, 0.3};
			int n = 128;
			int repetitions = 10;
			
			out.print("Density");
			for (int c = 0; c < combinators.length; c++) {
				out.print(" " + combinators[c]);
			}
			
			double[] cumulativeErrors = new double[combinators.length];
			for (double d: densities) {
				out.println();
				out.print(d);
				int combIndex = 0;
				for (Combinator combinator: combinators) {
					double err = 0;
					for (int rep = 0; rep < repetitions; rep++) {
						BooleanMatrix m = RandomMatrixGeneration.randomProductMatrix(n, n, 16, d, 0.3, d/10, 0);
						BooleanMatrixDecomposition alg = new BooleanMatrixDecomposition(generator, selector, combinator, 1d);
						Tuple<BooleanMatrix, BooleanMatrix> cb = alg.decompose(m, 10);
						BooleanMatrix rec = cb._1.booleanProduct(cb._2);
						double thisErr = m.relativeReconstructionError(rec, 1d);
						err += thisErr;
						//Tuple<Double, Double> ozRecErrors = m.relativeOneZeroZeroOneReconstructionError(rec, 1);
						//DecompositionLayout.showDecomposition(title, m.toInstances(), cb._1.toInstances(), cb._2.toInstances());
						cumulativeErrors[combIndex] += thisErr;
					}
					err /= repetitions;
					out.print(" " + err);
					combIndex++;
				}
			}
			
			out.println();
			out.println("\n# cumulated errors: ");
			for (int i = 0; i < combinators.length; i++) {
				System.out.println("#" + combinators[i] + ": \t" + cumulativeErrors[i]);
			}
			
			fos.close();
		}
	}
}
