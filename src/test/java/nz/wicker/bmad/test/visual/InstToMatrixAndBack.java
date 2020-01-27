package nz.wicker.bmad.test.visual;

import nz.wicker.bmad.matrix.*;
import weka.core.*;
import weka.core.converters.ConverterUtils.DataSource;

public class InstToMatrixAndBack {

	public static void main(String[] args) throws Exception {
		for (String file: new String[]{"kinase_labels.arff", "labels.nci.disc.noid.arff"/*, "labels.gene.arff"*/}) {
			
			// load some data
			Instances a = new DataSource(file).getDataSet();
			
			// print instances
			System.out.println(a);
			
			// convert to boolean
			BooleanMatrix b = new BooleanMatrix(a);
			System.out.println(b);
			
			// convert back
			System.out.println(b.toInstances());
		}
	}
}
