package nz.wicker.bmad.test.unittests;


import static nz.wicker.bmad.general.Package.*;
import static org.junit.Assert.*;
import org.junit.Test;

import nz.wicker.bmad.algorithms.Cover;
import nz.wicker.bmad.general.*;
import nz.wicker.bmad.matrix.BooleanMatrix;
import java.util.*;
import nz.wicker.bmad.randommatrixgeneration.*;
import static nz.wicker.bmad.matrix.BooleanMatrix.*;

public class CoverTest {

	public static double delta = 0.000000000000001;
	
	@Test
	public void testInclude() {
		Cover cover = new Cover(3, 1d);
		Cover should = new Cover(3, 1d);
		should.update(1, 1, 0);
		BooleanMatrix basis = new BooleanMatrix(1, 3);
		basis.update(1, (byte)2);
		cover.include(basis);
		assertEquals(should, cover);
	}

	@Test
	public void testOnesAndZerosOnInclusion() {
		// 01000
		Cover cover = new Cover(5, 1d);
		cover.update(1, 1, 0);
		
		// 01101
		BooleanMatrix basis1 = new BooleanMatrix(1, 5);
		basis1.update(1, TRUE);
		basis1.update(2, TRUE);
		basis1.update(4, TRUE);
		
		// 00011
		BooleanMatrix basis2 = new BooleanMatrix(1, 5);
		basis2.update(3, TRUE);
		basis2.update(4, TRUE);
		
		// 11101
		BooleanMatrix row = new BooleanMatrix(1, 5);
		for (int c = 0; c < 5; c++) if (c != 3) row.update(c, TRUE);
		
		
		assertEquals(tuple(2,0), cover.coveredOnesAndZerosOnInclusion(row, basis1));
		basis1.update(3, TRUE); // 01111
		assertEquals(tuple(2,1), cover.coveredOnesAndZerosOnInclusion(row, basis1));
		assertEquals(tuple(1,1), cover.coveredOnesAndZerosOnInclusion(row, basis2));
		assertEquals(tuple(0,0), cover.zero(5).coveredOnesAndZerosOnInclusion(row, new BooleanMatrix(1, 5)));
	}
	
	@Test
	public void testDensity() {
		// 01000
		Cover cover = new Cover(5, 1d);
		cover.update(1, 1, 0);
		
		// 01101
		BooleanMatrix basis1 = new BooleanMatrix(1, 5);
		basis1.update(1, TRUE);
		basis1.update(2, TRUE);
		basis1.update(4, TRUE);
		
		// 00011
		BooleanMatrix basis2 = new BooleanMatrix(1, 5);
		basis2.update(3, TRUE);
		basis2.update(4, TRUE);
		
		// 11101
		BooleanMatrix row = new BooleanMatrix(1, 5);
		for (int c = 0; c < 5; c++) if (c != 3) row.update(c, TRUE);
		
		
		assertEquals(1d, cover.coverChangeDensityOnInclusion(row, basis1), delta);
		basis1.update(3, TRUE); // 01111
		assertEquals(1/3d, cover.coverChangeDensityOnInclusion(row, basis1), delta);
		assertEquals(0d, cover.coverChangeDensityOnInclusion(row, basis2), delta);
		assertEquals(-1d, cover.zero(5).coverChangeDensityOnInclusion(row, new BooleanMatrix(1, 5)), delta);
	}
	
	@Test
	public void testDensityAgain() {
		Cover c = new Cover(3, 1d);
		
		BooleanMatrix r = new BooleanMatrix(1, 3);
		r.update(1, TRUE);
		r.update(2, TRUE);
		
		BooleanMatrix b = new BooleanMatrix(1, 3);
		b.update(2, TRUE);
		
		assertEquals(1d, c.coverChangeDensityOnInclusion(r, b), delta);
	}
	
	@Test
	/**
	 * Idea: cover changes are just negative reconstruction error changes.
	 * So we OR random columns, and keep track of the cover changes at each step,
	 * as well of the final reconstruction error. If the implementations are correct, 
	 * we should obtain the same answer by "integrating" and by calculating the total error change
	 * from start to end.
	 */
	public void testCoverChanges() {
		
		int n = 10000;
		Random rnd = new Random();
		
		BooleanMatrix row = RandomMatrixGeneration.randomProductMatrix(
				1, n, 16, 0.2, 0.3, 0.1, 0.05);
		
		// initialize all observed values
		int coveredOnes = 0;
		int uncoveredOnes = 0;
		int coveredZeros = 0;
		int uncoveredZeros = 0;
		
		int unknowns = 0;
		for (int i = 0; i < n; i++) {
			byte b = row.apply(i);
			if (b == UNKNOWN) {
				unknowns ++;
			} else if (b == TRUE) {
				uncoveredOnes ++;
			}
		}
		
		uncoveredZeros = row.getWidth() - uncoveredOnes - unknowns;
		
		// initialize cover column, and a list for the other random columns
		Cover cover = new Cover(n, 1d);
		ArrayList<BooleanMatrix> includedRows = new ArrayList<BooleanMatrix>();
		
		// loop, randomly add or remove columns to/from cover
		for (int i = 0; i < 1000; i++) {
			if (java.lang.Math.random() < 0.5) {
				// create new random column, add to the cover
				BooleanMatrix basisRow = RandomMatrixGeneration.randomMatrix(1, n, 0.2, 0.05);
				includedRows.add(basisRow);
				Tuple<Integer, Integer> t = cover.coveredOnesAndZerosOnInclusion(row, basisRow);
				coveredOnes += t._1;
				coveredZeros += t._2;
				uncoveredOnes -= t._1;
				uncoveredZeros -= t._2;
				cover.include(basisRow);
			} else if (!includedRows.isEmpty()) {
				int r = rnd.nextInt(includedRows.size());
				BooleanMatrix basisRow = includedRows.get(r);
				includedRows.remove(r);
				Tuple<Integer, Integer> t = cover.uncoveredOnesAndZerosOnExclusion(row, basisRow);
				coveredOnes -= t._1;
				coveredZeros -= t._2;
				uncoveredOnes += t._1;
				uncoveredZeros += t._2;
				cover.exclude(basisRow);
			}
		}
		
		// calculate everything again
		int _coveredOnes = 0;
		int _uncoveredOnes = 0;
		int _coveredZeros = 0;
		int _uncoveredZeros = 0;
		
		BooleanMatrix coverToBoolean = cover.toBooleanMatrix();
		
		for (int c = 0; c < n; c++) {
			byte b = row.apply(c);
			int coverValue = coverToBoolean.apply(c);
			if (b == UNKNOWN) {
				// ignore
			} else if (b == FALSE) {
				if (coverValue == FALSE) {
					_uncoveredZeros++;
				} else {
					_coveredZeros++;
				}
			} else if (b == TRUE) {
				if (coverValue == TRUE) {
					_coveredOnes++;
				} else {
					_uncoveredOnes++;
				}
			}
		}
		
		
		// assert
		assertEquals("cov 0", coveredZeros, _coveredZeros);
		assertEquals("uncov 0", uncoveredZeros, _uncoveredZeros);
		assertEquals("cov 1", coveredOnes, _coveredOnes);
		assertEquals("uncov 1", uncoveredOnes, _uncoveredOnes);
		
		// System.out.println("cov 0: " + coveredZeros + " " + _coveredZeros);
		// System.out.println("uncov 0: " + uncoveredZeros + " " + _uncoveredZeros);
		// System.out.println("cov 1: " + coveredOnes + " " + _coveredOnes);
		// System.out.println("uncov 1: " + uncoveredOnes + " " + _uncoveredOnes);
	}
}
