package nz.wicker.bmad.test.performance;

import weka.core.converters.ConverterUtils.DataSource;
import static nz.wicker.bmad.general.Package.*;
import static java.lang.System.*;
import java.util.*;

import java.awt.Toolkit;
import java.io.*;

import nz.wicker.bmad.algorithms.*;
import nz.wicker.bmad.general.Package;
import nz.wicker.bmad.general.Tuple;
import nz.wicker.bmad.matrix.BooleanMatrix;
import nz.wicker.bmad.randommatrixgeneration.RandomMatrixGeneration;
import nz.wicker.bmad.visualization.DecompositionLayout;

public class DimensionAccuracyTest {
	public static void main(String[] args) throws Exception {
		
		String[] files = {
			"kinase_labels.arff", 
			"labels.nci.disc.noid.arff", 
			"labels.gene.arff"
		};
		
		final BooleanMatrix[] matrices = {
				new BooleanMatrix(new DataSource(files[1]).getDataSet()),
				new BooleanMatrix(new DataSource(files[2]).getDataSet())
		};
		
		ArrayList<Tuple<BooleanMatrix, CandidateGenerator>> pairs = new ArrayList<Tuple<BooleanMatrix, CandidateGenerator>> (){{
			add(Package.<BooleanMatrix, CandidateGenerator>tuple(matrices[0], new IdentityGenerator()));
			add(Package.<BooleanMatrix, CandidateGenerator>tuple(matrices[0], new AssociationGenerator(0.65)));
			add(Package.<BooleanMatrix, CandidateGenerator>tuple(matrices[1], new IdentityGenerator()));
			add(Package.<BooleanMatrix, CandidateGenerator>tuple(matrices[1], new AssociationGenerator(0.2)));
		}};
		/*
		 * Accuracy experiment 5: 
		 * What do different candidate generation algorithms produce for different dimensions?
		 */

		{
			String file = files[1];
			FileOutputStream fos = new FileOutputStream("generatorAccuracy.dat");
			//System.setOut(new PrintStream(fos));
			System.out.println("# comparison of generators on real dataset, dimension varies");
			
			
			BasisSelector selector = new FastLoc();
			Combinator combinator = new DensityGreedyCombinator();
			
			out.print("Dimension");
			int fileIndex = 0;
			for (Tuple<BooleanMatrix, CandidateGenerator> t: pairs) {
				out.print(" " + files[fileIndex/2+1] + "(" + t._2 + ")");
				fileIndex++;
			}
					
			
			for (int dim = 25; dim < 1000; dim+=2) {
				out.println();
				out.print(dim);
				for (Tuple<BooleanMatrix, CandidateGenerator> t: pairs) {
					CandidateGenerator generator = t._2;
					BooleanMatrix m = t._1;
					BooleanMatrixDecomposition alg = new BooleanMatrixDecomposition(generator, selector, combinator, 1d);
					if (dim < m.getHeight()) {
						Tuple<BooleanMatrix, BooleanMatrix> cb = alg.decompose(m, dim);
						BooleanMatrix rec = cb._1.booleanProduct(cb._2);
						double thisErr = m.relativeReconstructionError(rec, 1d);
						out.print(" " + thisErr);
					}
				}
					
			}
			
			fos.close();
			
		}
	}
}