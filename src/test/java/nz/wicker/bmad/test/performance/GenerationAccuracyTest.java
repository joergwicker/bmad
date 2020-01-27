package nz.wicker.bmad.test.performance;

import static java.lang.System.*;

import java.awt.Toolkit;
import java.io.*;

import nz.wicker.bmad.algorithms.*;
import nz.wicker.bmad.general.Tuple;
import nz.wicker.bmad.matrix.BooleanMatrix;
import nz.wicker.bmad.randommatrixgeneration.RandomMatrixGeneration;
import nz.wicker.bmad.visualization.DecompositionLayout;

public class GenerationAccuracyTest {
	public static void main(String[] args) throws Exception {
		
		String[] files = {
			"kinase_labels.arff", 
			"labels.nci.disc.noid.arff", 
			"labels.gene.arff"
		};
		
		/*
		 * Accuracy experiment 2: 
		 * Testing reconstruction accuracy for different generators
		 * with a fixed combinator and a fixed selector.
		 */

		{
			FileOutputStream fos = new FileOutputStream("generatorAccuracy.dat");
			//System.setOut(new PrintStream(fos));
			System.out.println("# comparison of generators on slightly noisy synthetic data of varying density");
			
			
			CandidateGenerator[] generators = {
				new RestrictedIntersectionGenerator(),
				new AssociationGenerator(0.25),
				new AssociationGenerator(0.5),
				new AssociationGenerator(0.75),
				new IdentityGenerator()
			};
			BasisSelector selector = new FastLoc();
			Combinator combinator = new DensityGreedyCombinator();
			double[] densities = {0.01, 0.02, 0.03, 0.05, 0.1, 0.15, 0.2, 0.3};
			int n = 128;
			int repetitions = 10;
			
			out.print("Density");
			for (int c = 0; c < generators.length; c++) {
				out.print(" " + generators[c]);
			}
			
			double[] cumulativeErrors = new double[generators.length];
			for (double d: densities) {
				out.println();
				out.print(d);
				int combIndex = 0;
				for (CandidateGenerator generator: generators) {
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
			for (int i = 0; i < generators.length; i++) {
				System.out.println("#" + generators[i] + ": \t" + cumulativeErrors[i]);
			}
			
			fos.close();
			
			for (int i = 0; i < 100; i++) {
				Toolkit.getDefaultToolkit().beep();
				Thread.sleep(1000);
			}
		}
	}
}