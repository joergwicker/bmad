package org.kramerlab.bmad.test.performance;

import weka.core.converters.ConverterUtils.DataSource;
import org.kramerlab.bmad.algorithms.*;
import org.kramerlab.bmad.matrix.*;
import org.kramerlab.bmad.general.*;
import static java.lang.System.*;
import java.util.*;

public class AllCombinationsExperiment {

	public static void main(String[] args) throws Exception {

		CandidateGenerator[] generators = new CandidateGenerator[] {
				new IdentityGenerator(), 
				new AssociationGenerator(0.2),
				new AssociationGenerator(0.65),
				new RestrictedIntersectionGenerator()
		};

		BasisSelector[] selectors = new BasisSelector[] { 
				new GreedySelector(),
				new FastLoc() 
		};

		Combinator[] combinators = new Combinator[] {
				new IdentityCombinator(),
				new DensityGreedyCombinator(),
				new CoverGreedyCombinator(),
				new Iter(),
				new CombinatorPipeline(new DensityGreedyCombinator(), new Iter()),
				new CombinatorPipeline(new CoverGreedyCombinator(), new Iter()) 
		};

		String[] files = new String[] { 
				"datasets/labels.nci.disc.noid.arff",
				"datasets/labels.gene.arff" 
		};

		int dim = 16;
		double onesWeight = 1d;
		int repetitions = 5;

		// create result maps: file -> algorithm -> (ozErr, zoErr, totalErr)
		Map<String, Map<String, double[]>> results = new HashMap<String, Map<String, double[]>>();
		for (String file : files) {
			results.put(file, new HashMap<String, double[]>() {
				/**
                 * 
                 */
                private static final long serialVersionUID = 1L;

                @Override
				public double[] get(Object foo) {
					String key = (String) foo;
					if (!super.containsKey(key)) {
						super.put(key, new double[3]);
					}
					return super.get(key);
				}
			});
		}

		// perform calculation
		long time = currentTimeMillis();

		for (int repetition = 0; repetition < repetitions; repetition++) {
			for (String file : files) {
				BooleanMatrix original = new BooleanMatrix(
						new DataSource(file).getDataSet());

				for (CandidateGenerator generator : generators) {
					BooleanMatrix originalBackupGenerators = new BooleanMatrix(
							original);
					BooleanMatrix candidates = generator.generateCandidates(
							originalBackupGenerators, dim);
					for (BasisSelector selector : selectors) {
						BooleanMatrix originalBackupSelectors = new BooleanMatrix(
								original);
						BooleanMatrix candidateBackupSelectors = new BooleanMatrix(
								candidates);
						Tuple<BooleanMatrix, BooleanMatrix> t = selector
								.selectBasis(candidateBackupSelectors,
										originalBackupSelectors, dim,
										onesWeight);
						BooleanMatrix firstCombination = t._1;
						BooleanMatrix basis = t._2;
						for (Combinator combinator : combinators) {
							BooleanMatrix firstCombinationBackupCombinators = new BooleanMatrix(
									firstCombination);
							BooleanMatrix basisBackupCombinators = new BooleanMatrix(
									basis);
							BooleanMatrix originalBackupCombinators = new BooleanMatrix(
									original);
							firstCombinationBackupCombinators = combinator
									.combineMatrix(originalBackupCombinators,
											firstCombinationBackupCombinators,
											basisBackupCombinators, onesWeight);
							BooleanMatrix reconstruction = firstCombinationBackupCombinators
									.booleanProduct(basisBackupCombinators);
							Tuple<Double, Double> reconstructionError = original
									.relativeOneZeroZeroOneReconstructionError(
											reconstruction, onesWeight);
							String name = new BooleanMatrixDecomposition(
									generator, selector, combinator, onesWeight)
									.toString();
							double[] entry = results.get(file).get(name);
							entry[0] += reconstructionError._1 / repetitions;
							entry[1] += reconstructionError._2 / repetitions;
							entry[2] += (reconstructionError._1
									+ reconstructionError._2) / repetitions;
							
							out.println("rep = " + repetition + " alg = " + name);
						}
					}
				}
			}
		}

		
		time = currentTimeMillis() - time;

		// print results
		for (String file : files) {
			TreeSet<String> lines = new TreeSet<String>(new Comparator<String>() {
				public int compare(String o1, String o2) {
					String[] split1 = o1.split(" ");
					String[] split2 = o2.split(" ");
					String suffix1 = split1[split1.length-1];
					String suffix2 = split2[split2.length-1];
					int compSuff = suffix1.compareTo(suffix2);
					if (compSuff == 0) {
						return o1.compareTo(o2);	
					} else {
						return compSuff;
					}
				}
				
			});
			
			for (Map.Entry<String, double[]> e: results.get(file).entrySet()) {
				double[] value = e.getValue();
				lines.add(String.format("\"%-60s\" %-25s %-25s %-25s", e.getKey(), value[0], value[1], value[2]));
			}
			
			out.println("######## File: " + file);
			
			for (String line: lines) {
				out.println(line);
			}
		}
		
		int millis = (int) (time % 1000);
		time /= 1000;
		int seconds = (int) (time % 60);
		time /= 60;
		int minutes = (int) time;
		System.out.println("# The calculation took " + minutes + " minutes "
				+ seconds + " seconds and " + millis + " milliseconds");
	}
}
