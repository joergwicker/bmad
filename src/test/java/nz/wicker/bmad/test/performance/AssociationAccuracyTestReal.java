package nz.wicker.bmad.test.performance;

import weka.core.converters.ConverterUtils.DataSource;
import static java.lang.System.*;

import java.awt.Toolkit;
import java.io.*;

import nz.wicker.bmad.algorithms.*;
import nz.wicker.bmad.general.Tuple;
import nz.wicker.bmad.matrix.BooleanMatrix;
import nz.wicker.bmad.randommatrixgeneration.RandomMatrixGeneration;
import nz.wicker.bmad.visualization.DecompositionLayout;

public class AssociationAccuracyTestReal {
	public static void main(String[] args) throws Exception {
		
		String[] files = {
			"kinase_labels.arff", 
			"labels.nci.disc.noid.arff", 
			"labels.gene.arff"
		};
		
		/*
		 * Accuracy experiment 4: 
		 * What does association do with real data?
		 */

		{
			int filenumber = 2;
			FileOutputStream fos = new FileOutputStream("associationAccuracyReal2.dat");
			//System.setOut(new PrintStream(fos));
			System.out.println("# investigating influence of the confidence param for association-generator " +
					"on a real dataset " + files[filenumber]);
			
			double[] confidences = {0.12, 0.14, 0.16, 0.18};
			BasisSelector selector = new FastLoc();
			Combinator combinator = new DensityGreedyCombinator();
			
			out.print("Confidence ReconstructionError");
			
			for (double conf: confidences) {
				out.println();
				CandidateGenerator generator = new AssociationGenerator(conf);
				BooleanMatrix m = new BooleanMatrix(new DataSource(files[filenumber]).getDataSet());
				BooleanMatrixDecomposition alg = new BooleanMatrixDecomposition(generator, selector, combinator, 1d);
				Tuple<BooleanMatrix, BooleanMatrix> cb = alg.decompose(m, 10);
				BooleanMatrix rec = cb._1.booleanProduct(cb._2);
				double thisErr = m.relativeReconstructionError(rec, 1d);
				out.print(conf + " " + thisErr);
			}
			
			fos.close();
			
		}
	}
}