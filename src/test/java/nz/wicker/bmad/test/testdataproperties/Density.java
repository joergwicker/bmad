package nz.wicker.bmad.test.testdataproperties;

import weka.core.converters.ConverterUtils.DataSource;
import static java.lang.System.*;

import java.awt.Toolkit;
import java.io.*;
import nz.wicker.bmad.general.*;
import nz.wicker.bmad.algorithms.*;
import nz.wicker.bmad.matrix.BooleanMatrix;
import nz.wicker.bmad.randommatrixgeneration.RandomMatrixGeneration;
import nz.wicker.bmad.visualization.DecompositionLayout;

/**
 * Finds out, what the actual density of the example-arff's is.
 */
public class Density {
	public static void main(String[] args) throws Exception {
		
		String[] files = {
			"kinase_labels.arff", 
			"labels.nci.disc.noid.arff", 
			"labels.gene.arff"
		};
		
		/*
		 * What is the actual density of the real data?
		 */

		{
			for (String file: files) {
				out.println();
				BooleanMatrix m = new BooleanMatrix(new DataSource(file).getDataSet());
				BooleanMatrix zeros = new BooleanMatrix(m.getHeight(), m.getWidth());
				BooleanMatrix ones = zeros.mapBoolean(new Function<Byte, Byte>() {
					public Byte apply(Byte b) {
						return (byte)(2-b);
					}
				});
				double numberOfOnes = m.reconstructionError(zeros, 1);
				double numberOfZeros = m.reconstructionError(ones, 1);
				System.out.println("file: " + file + " #attr: " + m.getWidth() + " #inst: " + m.getHeight());
				System.out.println("file: " + file + " #1: " + numberOfOnes);
				System.out.println("file: " + file + " #0: " + numberOfZeros);
			}
		}
	}
}