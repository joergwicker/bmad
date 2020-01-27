package nz.wicker.bmad.experiments;

import static java.lang.System.out;
import static nz.wicker.bmad.algorithms.BooleanMatrixDecomposition.BEST_CONFIGURED;
import static nz.wicker.bmad.algorithms.BooleanMatrixDecomposition.BEST_UNCONFIGURED;
import static nz.wicker.bmad.algorithms.BooleanMatrixDecomposition.DBP;
import static nz.wicker.bmad.algorithms.BooleanMatrixDecomposition.LOC_ITER;

import nz.wicker.bmad.algorithms.BooleanMatrixDecomposition;
import nz.wicker.bmad.general.Tuple;
import nz.wicker.bmad.matrix.BooleanMatrix;

import weka.core.converters.ConverterUtils.DataSource;

public class DimensionToError {
    
    private static BooleanMatrixDecomposition[] algorithms = {
        BEST_UNCONFIGURED,
        BEST_CONFIGURED(0.1),
        BEST_CONFIGURED(0.2),
        BEST_CONFIGURED(0.3),
        BEST_CONFIGURED(0.4),
        BEST_CONFIGURED(0.5),
        BEST_CONFIGURED(0.6),
        LOC_ITER,
        DBP
    };
    
    public static void main(String[] args) throws Exception {
        
        String pathToDataset = args[0];
        int startDim = Integer.parseInt(args[1]);
        int endDim = Integer.parseInt(args[2]);
        int dimStep = Integer.parseInt(args[3]);
        
        double c = 1;
        
        out.print("dim ");
        for (Object o: algorithms) {
            out.print(o + " ");
        }
        out.println();
        
        for (int dim = startDim; dim <= endDim; dim += dimStep) {
            System.out.print(dim + " ");
            for (BooleanMatrixDecomposition a: algorithms) {
                BooleanMatrix matrix = new BooleanMatrix(
                        new DataSource(pathToDataset).getDataSet());
                Tuple<BooleanMatrix, BooleanMatrix> result = 
                        a.decompose(matrix, Math.min(matrix.getWidth(), dim));
                double err = matrix.relativeReconstructionError(
                    result._1.booleanProduct(result._2), c);
                out.print(err + " ");
            }
            out.println();
        }
    }
}
