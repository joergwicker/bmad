package nz.wicker.bmad.test.unittests;

import static nz.wicker.bmad.general.Package.*;
import static org.junit.Assert.*;
import org.junit.Test;

import nz.wicker.bmad.algorithms.Cover;
import nz.wicker.bmad.general.*;
import nz.wicker.bmad.matrix.BooleanMatrix;
import nz.wicker.bmad.randommatrixgeneration.RandomMatrixGeneration;
import static nz.wicker.bmad.matrix.BooleanMatrix.*;

public class ReconstructionErrorTest {
	
	private static double delta = 0.0000000001;
	
	@Test
	public void testRelativeOZZOErrorOn3x3Example() {
		/*
		 * m = [000;???;111]
		 * r = [010;101;010]
		 */
		BooleanMatrix mat = new BooleanMatrix(3, 3);
		BooleanMatrix rec = new BooleanMatrix(3, 3);
		byte[] B = {FALSE, UNKNOWN, TRUE};
		
		for (byte r = 0; r < 3; r++) {
			for (byte c = 0; c < 3; c++) {
				mat.update(r, c, B[r]);
				rec.update(r, c, (r + c) % 2 == 1 ? TRUE : FALSE);
			}
		}
		for (double weight: new double[]{0.1, 0.3, 0.5, 0.7, 0.9, 0.1, 10, 100}) {
			double totalWeight = 3 * (1 + weight);
			double expectedOneZero = 2 * weight / totalWeight;
			double expectedZeroOne = 1 / totalWeight;
			Tuple<Double, Double> is = mat.relativeOneZeroZeroOneReconstructionError(rec, weight);
			assertEquals(expectedOneZero, is._1, delta);
			assertEquals(expectedZeroOne, is._2, delta);
			assertEquals(expectedOneZero + expectedZeroOne, mat.relativeReconstructionError(rec, weight), delta);
		}
	}
	
	@Test
	public void testElementCounting() {
		for (int i = 0; i < 5; i ++) {
			BooleanMatrix x = RandomMatrixGeneration.randomProductMatrix(193, 317, 13, 0.1, 0.3, 0.2, 0.3);
			BooleanMatrix zeros = new BooleanMatrix(x.getHeight(), x.getWidth());
			BooleanMatrix ones = zeros.mapBoolean(new Function<Byte, Byte>() {
				public Byte apply(Byte b) {
					return TRUE;
				}
			});
			
			int[] count = x.elementCount();
			assertEquals("#0: ", count[0], x.reconstructionError(ones, 1), delta);
			assertEquals("#1: ", count[2], x.reconstructionError(zeros, 1), delta);
			assertEquals("#?: ", count[1], x.getWidth()*x.getHeight() - x.reconstructionError(ones, 1) - x.reconstructionError(zeros, 1), delta);
		}
	}
	
	@Test
	public void testRelativeOZZOErrors() {
		
	}
}