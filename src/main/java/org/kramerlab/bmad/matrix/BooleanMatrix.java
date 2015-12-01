package org.kramerlab.bmad.matrix;

import static org.kramerlab.bmad.general.Package.tuple;

import java.awt.Color;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import org.kramerlab.bmad.general.*;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Instance;
import weka.core.DenseInstance;

/**
 * Dense boolean matrix with 3-valued boolean logic
 * 
 * TRUE is represented as 2, UNKNOWN is represented as 1, 
 * FALSE is represented as 0, AND corresponds to MIN,
 * OR corresponds to MAX
 */

public class BooleanMatrix {
	
    public static final byte TRUE = 3;
    public static final byte UNKNOWN = 1;
    public static final byte FALSE = 0;

    /**
     * Negation of a byte.
     *
     * @param b the bute to negate
     * @return the negated byte
     */
    public static byte not(byte b) {
        switch(b) {
            case TRUE: return FALSE;
            case UNKNOWN: return UNKNOWN;
            case FALSE: return TRUE;
            default: throw new IllegalArgumentException(
                "b = " + b + " is not in {TRUE, FALSE, UNKNOWN}"
            );
        }
    }
    
	private int width;
	private int height;
	private byte[][] rows;
	
	/**
	 * Constructs new empty matrix with given dimensions, filled with FALSE
	 * 
	 * @param h height
	 * @param w width
	 */
	public BooleanMatrix(int h, int w) {
		this.height = h;
		this.width = w;
		rows = new byte[h][w];
	}
	
	/**
	 * Creates a deep copy of another matrix
	 * @param b
	 */
	public BooleanMatrix(BooleanMatrix b) {
		this(b.getHeight(), b.getWidth());
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				rows[r][c] = b.apply(r, c);
			}
		}
	}
	
	/**
	 * Constructs dense boolean matrix from given array in 
	 * row major format. Assumes that the array is rectangular
	 * (all rows have equal length), and not degenerate (has entries).
	 * 
	 * @param rowMajor entries in column major format
	 */
	public BooleanMatrix(byte[][] rowMajor) {
		this.width = rowMajor[0].length;
		this.height = rowMajor.length;
		this.rows = rowMajor;
	}
	
	/**
	 * Constructs dense boolean matrix from weka instances, 
	 * that is expected to be filled with values 0, ?, 1
	 */
	public BooleanMatrix(Instances instances) {
		this(instances.numInstances(), instances.numAttributes());
		int row = 0;
		for (Instance instance: instances) {
			for (int i = 0; i < instance.numValues(); i++) {
				int col = instance.index(i);
				double value = instance.valueSparse(i);
				byte b = Double.isNaN(value) ? UNKNOWN : value == 0d ? FALSE : TRUE;
				this.update(row, col, b);
			}
			row++;
		}
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	/**
	 * Sets the entry at position <code>(r, c)</code> 
	 * to <code>b</code> 
	 * @param r row
	 * @param c column
	 * @param b 0, 1 or 2
	 */
	public void update(int r, int c, byte b) {
		rows[r][c] = b;
	}
	
	public byte apply(int r, int c) {
		return rows[r][c];
	}

	public byte apply(int c) {
		return rows[0][c];
	}
	
	public void update(int c, byte b) {
		rows[0][c] = b;
	}
	
	public BooleanMatrix getRows(List<Integer> indices) {
		byte[][] entries = new byte[indices.size()][width];
		
		int r = 0;
		for (int i: indices) {
			entries[r] = rows[i];
			r++;
		}
		
		return new BooleanMatrix(entries);
	}
	
	/**
	 * In-place operation on two row vectors, corresponding to SAXPY in 
	 * linear algebra
	 * 
	 * @param alpha factor
	 * @param x other row
	 */
	public void baxoy(byte alpha, BooleanMatrix x) {
		if (alpha == 0) return;
		byte[] thisRow = rows[0];
		byte[] otherRow = x.rows[0];
		
		for (int c = 0; c < width; c++) {
			thisRow[c] = (byte)(thisRow[c] | (otherRow[c] & alpha));
		}
		
	}
	
	/**
	 * Extracts single row as separate matrix. No values are copied, the row
	 * is backed by the same array
	 * 
	 * @param r index of the row
	 * @return row matrix
	 */
	public BooleanMatrix getRow(int r) {
		return new BooleanMatrix(new byte[][]{rows[r]});
	}

	/**
	 * Analogous to get row: no values are copied
	 * 
	 * @param r index of the row
	 * @param row row vector
	 */
	public void setRow(int r, BooleanMatrix row) {
		rows[r] = row.rows[0];
	}
	
	/**
	 * Calculates the boolean product with the other matrix.
	 * 
	 * @param other boolean matrix with compatible dimension
	 * @return
	 */
	public BooleanMatrix booleanProduct(BooleanMatrix other) {
	    if (this.width != other.height) {
	        throw new IllegalArgumentException(
	            "Incompatible matrix dimensions: " +
	            "cannot multiply " + this.height + " x " + this.width +
	            " matrix with a " + other.height + " x " + other.width +
	            " matrix."
	        );
	    }
		BooleanMatrix result = new BooleanMatrix(this.height, other.width);
		for (int r = 0; r < this.height; r++) {
			BooleanMatrix row = result.getRow(r);
			for (int c = 0; c < this.width; c++) {
				row.baxoy(apply(r, c), other.getRow(c));
			}
		}
		return result;
	}
	
	/**
	 * Transforms this matrix to a generic matrix. 
	 * The generic versions are significantly slower for computations,
	 * but provide many handy O(n^2) methods (e.g. for printing and
	 * drawing matrices)
	 * 
	 * @return same matrix in generic format
	 */
	public RowMajor<Byte> toRowMajor() {
		RowMajor<Byte> m = new RowMajor<Byte>(height, width, (byte)0);
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				m.update(r, c, rows[r][c]);
			}
		}
		return m;
	}
    
	/**
	 * Calculates the reconstruction error between this matrix and the
	 * reconstruction.
	 *  
	 * @param reconstruction some other matrix
	 * @param onesWeight weight of <code>1-&gt;0</code> errors 
	 * relative to <code>0-&gt;1</code> errors
	 * 
	 * @return
	 */
	public double reconstructionError(BooleanMatrix reconstruction, double onesWeight) {
		double totalError = 0;
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				byte thisByte = apply(r, c);
				if (thisByte != UNKNOWN) {
					byte otherByte = reconstruction.apply(r, c);
					if (thisByte == FALSE && otherByte != FALSE) {
						totalError += 1;
					} else if (thisByte == TRUE && otherByte != TRUE) {
						totalError += onesWeight;
					}
				}
			}
		}
		return totalError;
	}
	
	/**
	 * Calculates the relative <code>1-&gt;0</code> and <code>0-&gt;1</code> 
	 * reconstruction error, that is, the total error divided by total
	 * maximum possible error.
	 * 
	 * Assumes, that there are no unknowns in the reconstruction.
	 * The total weight equals <code>#0 + onesWeight * #1</code>.
	 * Each <code>1-&gt;0</code> error costs <code>onesWeight</code>.
	 * Each <code>0-&gt;1</code> error costs <code>1</code>.
	 * 
	 * @param reconstruction
	 * @param onesWeight
	 * @return 
	 *   tuple with relative <code>1-&gt;0</code> and <code>0-&gt;1</code> errors
	 */
	public Tuple<Double, Double> relativeOneZeroZeroOneReconstructionError(BooleanMatrix reconstruction, double onesWeight) {
		double oneZeroError = 0;
		double zeroOneError = 0;
		double totalWeight = 0;
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				byte thisByte = apply(r, c);
				if (thisByte != UNKNOWN) {
					byte otherByte = reconstruction.apply(r, c);
					if (thisByte == FALSE) {
						totalWeight += 1;
						if (otherByte != FALSE)
							zeroOneError += 1;
					} else if (thisByte == TRUE) {
						totalWeight += onesWeight;
						if (otherByte != TRUE)
							oneZeroError += onesWeight;
					}
				}
			}
		}
		return tuple(oneZeroError / totalWeight, zeroOneError / totalWeight);
	}
	
	/**
	 * Calculates the relative error 
	 * (error divided by the maximum possible error).
	 * 
	 * @param reconstruction
	 * @param onesWeight relative weight of <code>1-&gt;0</code> errors
	 * @return total relative error
	 */
	public double relativeReconstructionError(BooleanMatrix reconstruction, double onesWeight) {
		Tuple<Double, Double> t = relativeOneZeroZeroOneReconstructionError(reconstruction, onesWeight);
		return t._1 + t._2;
	}
	
	@Override
	public String toString() {
		return toRowMajor().map(new Function<Byte, Character>() {
			public Character apply(Byte b) {
				return b == FALSE ? '.' : b == UNKNOWN ? '?' : '1';
			}
		}).toString();
	}
	
	/**
	 * Draws this matrix as image. False is white, true is black, unknown is
	 * gray.
	 * 
	 * @return image representation of the matrix.
	 */
	public Image toImage() {
		return toRowMajor().toImage(new Function<Byte, Color>() {
			public Color apply(Byte b) {
				switch((byte)b) {
				    case FALSE: return Color.WHITE;
				    case TRUE: return Color.BLACK;
				    case UNKNOWN: return Color.GRAY;
				    default: throw new IllegalArgumentException(
				        "b = " + b + " not ternary boolean."
				    );
				}
			}
		});
	}
	
	/**
	 * Converts the matrix to Weka-Instances
	 * 
	 * @return instances
	 */
	public Instances toInstances() {
		ArrayList<String> nominalValues = new ArrayList<String>();
		nominalValues.add("0");
		nominalValues.add("1");
		
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		for (int c = 0; c < getWidth(); c++) {
			attributes.add(new Attribute("col " + c, nominalValues));
		}
		
		Instances instances = new Instances("boolean matrix", attributes, getHeight());
		for (int r = 0; r < height; r++) {
			double[] entries = new double[getWidth()];
			DenseInstance inst = new DenseInstance(1, entries);
			for (int c = 0; c < width; c++) {
				byte b = apply(r, c);
				if (b == UNKNOWN) {
					inst.setMissing(c);
				} else {
					inst.setValue(c, b == TRUE ? 1 : 0);
				}
			}
			instances.add(inst);
		}
		
		return instances;
	}

	/**
	 * Map-method, restricted to boolean matrices as output.
	 * Creates a new matrix, that consists of point-wise applications
	 * of the function on the entries of this matrix.
	 * 
	 * @param function
	 * @return
	 */
	public BooleanMatrix mapBoolean(Function<Byte, Byte> function) {
		BooleanMatrix result = new BooleanMatrix(getHeight(), getWidth());
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				result.update(r, c, function.apply(this.apply(r, c)));
			}
		}
		return result;
	}

	/**
	 * Returns width and height of the matrix.
	 * 
	 * @return
	 */
	public Tuple<Integer, Integer> size() {
		return tuple(height, width);
	}
	
	/**
	 * Counts ones, unknowns, and zeros contained in this matrix
	 * 
	 * @return array with number of zeros, unknowns and ones (in this order)
	 */
	public int[] elementCount() {
		int zeros = 0;
		int ones = 0;
		int unknowns = 0;
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				byte b = apply(r, c);
				if (b == FALSE) zeros++;
				if (b == UNKNOWN) unknowns++;
				if (b == TRUE) ones++;
			}
		}
		return new int[]{zeros, unknowns, ones};
	}
}
