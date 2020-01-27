package nz.wicker.bmad.randommatrixgeneration;

import java.util.Random;
import static nz.wicker.bmad.matrix.BooleanMatrix.*;

import nz.wicker.bmad.matrix.BooleanMatrix;

public class RandomMatrixGeneration {
	
	public static void flipElementRandomly(BooleanMatrix a, double density) {
		Random rnd = new Random();
		if (density == 0) return;
		for (int r = 0; r < a.getHeight(); r++) {
			for (int c = 0; c < a.getWidth(); c++) {
				if (rnd.nextDouble() < density) 
					a.update(r, c, not(a.apply(r, c)));
			}
		}
	}
	
	public static void insertElementRandomly(BooleanMatrix a, double density, byte element) {
		Random rnd = new Random();
		if (density == 0) return;
		for (int r = 0; r < a.getHeight(); r++) {
			for (int c = 0; c < a.getWidth(); c++) {
				if (rnd.nextDouble() < density) 
					a.update(r, c, element);
			}
		}
	}
	
	public static BooleanMatrix randomMatrix(int height, int width, double density, double unknownDensity) {
		BooleanMatrix result = new BooleanMatrix(height, width);
		insertElementRandomly(result, density, TRUE);
		insertElementRandomly(result, unknownDensity, UNKNOWN);
		return result;
	}
	
	public static BooleanMatrix randomProductMatrix(
			int height, 
			int width, 
			int basisDimension,
			double basisDensity,
			double inclusionProbability,
			double noise,
			double unknownDensity) {
		
		BooleanMatrix basis = randomMatrix(basisDimension, width, basisDensity, 0d);
		BooleanMatrix combination = randomMatrix(height, basisDimension, inclusionProbability, 0d);
		BooleanMatrix result = combination.booleanProduct(basis);
		flipElementRandomly(result, noise);
		insertElementRandomly(result, unknownDensity, (byte)1);
		
		return result;
	}
}
