package org.kramerlab.bmad.visualization;

import java.awt.Color;

import javax.swing.JFrame;

import org.kramerlab.bmad.general.*;
import org.kramerlab.bmad.matrix.*;
import static org.kramerlab.bmad.matrix.BooleanMatrix.*;

import weka.core.Instances;

public class DecompositionLayout {

	/**
	 * Shows result of a decomposition with following layout:
	 * 
	 * <pre>
	 * {@code
	 * --------------------SecondFactor------------------
	 * ----FirstFactor-----Reconstruction--------Original
	 * }
	 * </pre>
	 * 
	 * @param original
	 *            the matrix that has been decomposed
	 * @param combinations
	 *            first factor
	 * @param basisRows
	 *            second factor
	 */
	public static void showDecomposition(String title, Instances original,
			Instances combinations, Instances basisRows) {

		final BooleanMatrix a = new BooleanMatrix(original);
		BooleanMatrix comb = new BooleanMatrix(combinations);
		BooleanMatrix basis = new BooleanMatrix(basisRows);
		BooleanMatrix reconstruction = comb.booleanProduct(basis);

		// read out the dimensions
		int dim = basis.getHeight();
		int w = a.getWidth();
		int h = a.getHeight();

		// now mark the wrong entries of the reconstruction!
		final byte ONE_ZERO = (byte)16;
		final byte ZERO_ONE = (byte)32;
		BooleanMatrix errorMatrix = new BooleanMatrix(h, w);

		for (int r = 0; r < h; r++) {
			for (int c = 0; c < w; c++) {
				byte should = a.apply(r, c);
				byte is = reconstruction.apply(r, c);
				if (should == is || should == UNKNOWN) {
					errorMatrix.update(r, c, is);
				} else {
					errorMatrix.update(r, c, should == 0 ? ONE_ZERO : ZERO_ONE);
				}
			}
		}

		RowMajor<BooleanMatrix> imgComponents = new RowMajor<BooleanMatrix>(2,
				3, (BooleanMatrix) null);
		imgComponents.update(0, 0, new BooleanMatrix(dim, dim));
		imgComponents.update(0, 1, basis);
		imgComponents.update(0, 2, new BooleanMatrix(dim, w));

		imgComponents.update(1, 0, comb);
		imgComponents.update(1, 1, errorMatrix);
		imgComponents.update(1, 2, a);

		RowMajor<Byte> img = imgComponents
				.flatMap(new Function<BooleanMatrix, RowMajor<Byte>>() {
					public RowMajor<Byte> apply(BooleanMatrix b) {
						return padMatrix(b, 20).toRowMajor();
					}
				});

		JFrame f = new JFrame(title);
		f.getContentPane().add(
				new SingleImageComponent(img
						.toImage(new Function<Byte, Color>() {
							public Color apply(Byte b) {
								if (b == FALSE)
									return Color.WHITE;
								if (b == UNKNOWN)
									return new Color(200, 240, 240);
								if (b == TRUE)
									return Color.BLACK;
								if (b == ONE_ZERO)
									return new Color(255, 100, 100);
								if (b == ZERO_ONE)
									return Color.RED;
								return Color.RED;
							}
						})));
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
		f.setExtendedState(f.getExtendedState() | JFrame.MAXIMIZED_BOTH);
	}

	private static BooleanMatrix padMatrix(BooleanMatrix m, int padding) {
		BooleanMatrix padded = new BooleanMatrix(m.getHeight() + 2 * padding,
				m.getWidth() + 2 * padding);
		for (int c = 0; c < m.getWidth(); c++) {
			for (int r = 0; r < m.getHeight(); r++) {
				padded.update(r + padding, c + padding, m.apply(r, c));
			}
		}
		return padded;
	}
}