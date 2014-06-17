package org.kramerlab.bmad.demo;

import static java.lang.Math.max;

import org.kramerlab.bmad.algorithms.AssociationGenerator;
import org.kramerlab.bmad.algorithms.BooleanMatrixDecomposition;
import org.kramerlab.bmad.algorithms.CombinatorPipeline;
import org.kramerlab.bmad.algorithms.DensityGreedyCombinator;
import org.kramerlab.bmad.algorithms.FastLoc;
import org.kramerlab.bmad.algorithms.Iter;
import org.kramerlab.bmad.general.Tuple;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import org.kramerlab.bmad.visualization.DecompositionLayout;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Demonstrates the usage of boolean matrix decomposition on weka instances
 */
public class Demo {
	public static void main(String... _) throws Throwable {
		for (String file : new String[] { 
		        // "datasets/kinase_labels.arff",
				// "datasets/labels.nci.disc.noid.arff", 
				// "datasets/labels.gene.arff",
				"datasets/demo.arff"}) {

		    try {
			    // load some data
			    Instances instances = new DataSource(file).getDataSet();
                
			    for (double tau = 0.15; tau < 0.25; tau += 0.01) {
			        // create boolean matrix decomposition algorithm,
			        // consisting of a candidate generator, basis selector,
			        // and a combinator
			        BooleanMatrixDecomposition algorithm = 
			        	new BooleanMatrixDecomposition(
			                new AssociationGenerator(tau), 
			                new FastLoc(),
			                new CombinatorPipeline(
			                    new DensityGreedyCombinator(), 
			                    new Iter()
			                ), 
			                1d
			            );
                    
			        // decompose
			        Tuple<Instances, Instances> t = algorithm.decompose(instances,
			        		max(instances.numAttributes() / 100, 8));
                    
			        // notice, that the decompose() method is "the right way round",
			        // from Weka's point of view
			        Instances basisRows = t._2;
			        Instances learnableRepresentation = t._1;
                    
			        // draw the result
			        DecompositionLayout.showDecomposition(
			            file + " tau = " + tau, 
			            instances,
			        	learnableRepresentation, 
			        	basisRows
			        );
			        
			        // for calculation of errors,
			        // convert everything to boolean matrices again
			        BooleanMatrix a = new BooleanMatrix(instances);
			        BooleanMatrix b = new BooleanMatrix(basisRows);
			        BooleanMatrix c = new BooleanMatrix(learnableRepresentation);
			        
			        // print the reconstruction error
			        System.out.println(
			            "Relative reconstruction error for tau = " + tau + 
			            " = " + 
			            a.relativeReconstructionError(c.booleanProduct(b), 1d)
			        );
			    }
		    } catch (Exception e) {
		        System.out.println(
		            "The file '" + file + 
		            "' is probably not in the current directory."
		        );
		        System.out.println("Full stack trace: ");
		        e.printStackTrace();
		    }
		}
	}
}