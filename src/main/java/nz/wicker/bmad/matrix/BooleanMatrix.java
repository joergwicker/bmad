package nz.wicker.bmad.matrix;

import static nz.wicker.bmad.general.Package.tuple;

import java.awt.Color;
import java.awt.Image;
import java.util.*;

import nz.wicker.bmad.general.*;
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

	public static BooleanMatrix deepTranspose(BooleanMatrix b) {
		BooleanMatrix x = new BooleanMatrix(b.getWidth(), b.getHeight());
		for (int r = 0; r < x.getHeight(); r++) {
			for (int c = 0; c < x.getWidth(); c++) {
				x.rows[r][c] = b.apply(c, r);
			}
		}
		return x;
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
//		this(instances.numInstances(), instances.numAttributes());
//		int row = 0;
//		for (Instance instance: instances) {
//			for (int i = 0; i < instance.numValues(); i++) {
//				int col = instance.index(i);
//				double value = instance.valueSparse(i);
//				byte b = Double.isNaN(value) ? UNKNOWN : value == 0d ? FALSE : TRUE;
//				this.update(row, col, b);
//			}
//			row++;
//		}
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
	 * Calls the "xor = false" version of baxoy, where addition is defined as OR
	 *
	 * @param alpha factor
	 * @param x other row
	 */
	public void baxoy(byte alpha, BooleanMatrix x) {
		baxoy(alpha, x, false);
	}
	/**
	 * In-place operation on two row vectors, corresponding to SAXPY in
	 * linear algebra
	 *
	 * @param alpha factor
	 * @param x other row
	 * @param xor true = XOR, false = OR.
	 */
	public void baxoy(byte alpha, BooleanMatrix x, boolean xor) {
		if (alpha == 0) return;
		byte[] thisRow = rows[0];
		byte[] otherRow = x.rows[0];

		for (int c = 0; c < width; c++) {
			if(xor){
				// use "xor" for vector addition
				thisRow[c] = (byte)(thisRow[c] ^ (otherRow[c] & alpha));
			} else{
				// use "or" for vector addition
				thisRow[c] = (byte)(thisRow[c] | (otherRow[c] & alpha));
			}
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
	 * Extract single column as a separate matrix. no values copied.
	 * @param c: index of column to extract, starting from 0.
	 * @return: a new BooleanMatrix object representing the target column.
	 */
	public BooleanMatrix getCol(int c){
		byte[][] output = new byte[height][1];
		for(int row = 0; row < height; row++){
			output[row][0] = rows[row][c];
		}
		return new BooleanMatrix(output);
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
	 * Calls the "xor = false" version of booleanProduct.
	 *
	 * @param other boolean matrix with compatible dimension
	 * @return
	 */
	public BooleanMatrix booleanProduct(BooleanMatrix other) {
		return booleanProduct(other, false);
	}

	/**
	 * Calculates the boolean product with the other matrix.
	 * Addition is defined either as logical "or" or "xor".
	 *
	 * @param other boolean matrix with compatible dimension
	 * @return
	 * @param xor boolean indicator of addition version: true = xor, false = or.
	 */
	public BooleanMatrix booleanProduct(BooleanMatrix other, boolean xor) {
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
				row.baxoy(apply(r, c), other.getRow(c), xor);
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

	public double getDensity(){
		double count1 = 0.0;
		double count0 = 0.0;

		for (byte[] arr : rows) {
			for (byte val : arr) {
				if (val == TRUE) {
					count1 ++;
				}
				if (val == FALSE) {
					count0 ++;
				}

			}

		}
		return count1/(count1+count0);
	}

	public double getMissingDensity(){
		double count1 = 0.0;
		double count0 = 0.0;
		double countMiss = 0.0;

		for (byte[] arr : rows) {
			for (byte val : arr) {
				if (val == TRUE) {
					count1 ++;
				}
				if (val == FALSE) {
					count0 ++;
				}
				if (val == UNKNOWN) {
					countMiss ++;
				}

			}

		}
		return countMiss/(count1+count0+countMiss);
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

	public static void printMatrix(BooleanMatrix b){
		for (int i = 0; i < b.getHeight(); i++) {
			for (int j = 0; j <b.getWidth() ; j++) {
				System.out.print(b.apply(i,j));
				System.out.print(", ");
			}
			System.out.print("\n");

		}
	}

	// Returns how close R is to C by interpreting every bitsPerPixel elements within a row as a base-10 number.
	public double averageEuclideanReconstructionError(BooleanMatrix R, int bitsPerPixel){

		assert(this.getWidth()%bitsPerPixel == 0);
		assert(this.getHeight() == R.getHeight());
		assert(this.getWidth() == R.getWidth());
		double error = 0d;
		StringBuilder s1 = new StringBuilder("12345678");
		StringBuilder s2 = new StringBuilder("12345678");
		int nPixels = this.getWidth()/bitsPerPixel;
		for (int i = 0; i < this.getHeight(); i++) {
			for (int p = 0; p < nPixels  ; p++) {

				for (int j = bitsPerPixel*p; j < bitsPerPixel*(p+1) ; j++) {
					s1.setCharAt(j-bitsPerPixel*p, this.apply(i,j) == TRUE ? '1' : '0' );
					s2.setCharAt(j-bitsPerPixel*p, (R.apply(i,j) == TRUE) ? '1' : '0' );
				}

				double v1 = (double) Integer.parseInt(s1.toString(),2);
				double v2 = (double) Integer.parseInt(s2.toString(),2);

				error += Math.sqrt(Math.pow(v1 -v2,2));
			}
		}

		return error/(this.size()._1 * this.size()._2 / (double) bitsPerPixel);

	}

	public BooleanMatrix xorAdd(BooleanMatrix R){
		assert(this.getHeight() == R.getHeight());
		assert(this.getWidth() == R.getWidth());
		byte[][] result = new byte[this.getHeight()][this.getWidth()];
		for (int i = 0; i <this.getHeight() ; i++) {
			for (int j = 0; j < this.getWidth(); j++) {
				result[i][j] =  (this.apply(i,j)==R.apply(i,j)) ? FALSE : TRUE;
			}
		}
		return new BooleanMatrix(result);
	}



	/**
	 * Extracts single row as a byte array.
	 *  No values are copied, the row is backed by the same array.
	 * @param r index of the row
	 * @return row array.
	 */
	public byte[] getRowArray(int r) {
		return rows[r];
	}


	/**
	 * Extract single column as a byte array. no values copied.
	 * @param c: index of column to extract, starting from 0.
	 * @return: a new byte array representing the target column.
	 */
	public byte[] getColArray(int c){
		byte[] output = new byte[height];
		for(int row = 0; row < height; row++){
			output[row] = rows[row][c];
		}
		return output;
	}


	public boolean isEqual(BooleanMatrix b){
	    int height = getHeight();
	    int width = getWidth();
	    double density = getDensity();
	    if(height != b.getHeight() || width != b.getWidth() || density != b.getDensity()){
	        return false;
        }else{
	        for(int row = 0; row < height; row++){
	            for(int col = 0; col < width; col++){
	                if(apply(row, col) != b.apply(row,col)){
	                    return false;
                    }
                }
            }
            return true;
        }
    }


    /**
     * Takes the index of a row to be removed (starting from 0), returns a new BooleanMatrix object as a copy of the original matrix without this row.
     * @param r: an int primitive type indicating the index of the row to be removed (starting from 0).
     * @return: a new BooleanMatrix object as a copy of the original matrix without this row.
     */
    public BooleanMatrix removeRow(int r){
	    ArrayList<byte[]> temp = new ArrayList<byte[]>();
	    for(int i = 0; i < height; i++) {
			if (i != r) {
				temp.add(rows[i]);
			}
		}
        return new BooleanMatrix(temp.toArray(new byte[0][0]));
    }


    /**
     * Takes the index of a column to be removed (starting from 0), returns a new BooleanMatrix object as a copy of the original matrix without this column.
     * @param x: an int primitive type indicating the index of the column to be removed (starting from 0).
     * @return: a new BooleanMatrix object as a copy of the original matrix without this column.
     */
    public BooleanMatrix removeCol(int x){
        int height = this.getHeight();
        int width = this.getWidth();
        byte[][] temp = new byte[height][width - 1];
        for(int r = 0; r < height; r++){
            for(int c1 = 0; c1 < x; c1++){
                temp[r][c1] = apply(r, c1);
            }
            for(int c2 = x + 1; c2 < width; c2++){
                temp[r][c2 - 1] = apply(r, c2);
            }
        }
        return new BooleanMatrix(temp);
    }


	/**
	 * Takes an ArrayList<Integer> of row indices, returns a copy of the matrix without these rows.
	 * @param rlist: an ArrayList<Integer> of row indices.
	 * @return: a BooleanMatrix object representing the original matrix without the rows in the list.
	 */
    public BooleanMatrix removeMultiRows(ArrayList<Integer> rlist) {
		Collections.sort(rlist);
        ArrayList<byte[]> temp = new ArrayList<byte[]>();
        for(int i = 0; i < height; i++){
        	if (!rlist.contains(i)){
        		temp.add(rows[i]);
			}
		}
        return new BooleanMatrix(temp.toArray(new byte[0][0]));
    }


	/**
	 * Takes an ArrayList<Integer> of column indices, returns a copy of the matrix without these columns.
	 * @param clist: an ArrayList<Integer> of column indices.
	 * @return: a BooleanMatrix object representing the original matrix without the columns in the list.
	 */
	public BooleanMatrix removeMultiCols(ArrayList<Integer> clist) {
		Collections.sort(clist);
		int counter, num = clist.size();
        byte[][] temp = new byte[height][width - num];
        for(int r = 0; r < height; r++){
        	counter = 0;
            for(int c = 0; c < width; c++) {
				if (clist.contains(c)) {
					counter++;
				}else{
					temp[r][c - counter] = rows[r][c];
				}
			}
        }
        return new BooleanMatrix(temp);
    }


	/**
	 * Takes an ArrayList<Integer> of indices, a boolean flag indicating whether to remove rows (true) or cols (false).
	 * Return a new BooleanMatrix object without these rows/cols.
	 * @param list: an ArrayList<Integer> containing indices of the rows/cols to be removed.
	 * @param removeRows: a boolean flag indicating whether to remove rows (true) or columns (false).
	 * @return: A BooleanMatrix object representing the original matrix without duplicated rows/cols (all rows/cols (depending on the boolean flag) are unique).
	 */
	public BooleanMatrix removeDuplicates(ArrayList<Integer> list, boolean removeRows) {

		int height = getHeight();
		int width = getWidth();
		int counter = 0;
		int prev = 0;
		BooleanMatrix output = new BooleanMatrix(this);

		if (height <= 1 || width <= 1 || list.isEmpty()) {
			System.out.println("Cannot remove further, returning original input.");
			return this;
		} else {
			for (int i = 0; i < list.size(); i++) {
				int val = list.get(i);
				int pos = (prev < val)? val - counter : val;
				output = removeRows ? output.removeRow(pos) : output.removeCol(pos);
				counter++;
				prev = pos;
			}
			return output;
		}
	}


	/**
	 * Returns the indices of rows/cols that are duplicates of other row/col (the original row/col is not included in the list)
	 * @param compareRows: A boolean flag indicating whether to compare rows (true) or columns (false)
	 * @return: an ArrayList<Integer> object holding indices of all duplicate row/cols (excluding those of the original row/col that has duplicates).
	 */
	public ArrayList<Integer> getDuplicates(boolean compareRows) {

		ArrayList<Integer> output = new ArrayList<Integer>();

		int height = getHeight();
		int width = getWidth();

		if (height <= 1 || width <= 1) {
			System.out.println("Cannot compress further, returning original input.");
			return output;
		} else {
			int k = compareRows? height : width;
			BooleanMatrix current, other;

			for (int r = 0; r < k - 1; r++) {
				current = compareRows ? getRow(r) : getCol(r);
				for (int i = r + 1; i < k; i++) {
					other = compareRows ? getRow(i) : getCol(i);
					if (current.isEqual(other)) {
						output.add(i);
					}
				}
			}
			Collections.sort(output);
			return output;
		}
	}




	/**
	 * Returns the indices of rows/cols that are duplicates of other row/col (the original row/col is not included in the list)
	 * @param compareRows: A boolean flag indicating whether to compare rows (true) or columns (false)
	 * @return: an ArrayList<Integer> object holding indices of all duplicate row/cols (excluding those of the original row/col that has duplicates).
	 */
	public Tuple<ArrayList<Integer>, HashMap<Integer, Set<Integer>>> getDuplicatesAndOriginal(boolean compareRows) {

		ArrayList<Integer> duplicates = new ArrayList<Integer>();
		HashMap<Integer, Set<Integer>> fullList = new HashMap<Integer, Set<Integer>>();

		Tuple<ArrayList<Integer>, HashMap<Integer, Set<Integer>>> output = new Tuple<ArrayList<Integer>, HashMap<Integer, Set<Integer>>> (duplicates, fullList);

		int height = getHeight();
		int width = getWidth();

		if (height <= 1 || width <= 1) {
			System.out.println("Cannot compress further, returning original input.");
			return output;
		} else {
			int k = compareRows? height : width;
			BooleanMatrix current, other;

			for (int r = 0; r < k - 1; r++) {
				current = compareRows ? getRow(r) : getCol(r);
				Set<Integer> temp = new HashSet<Integer>();
				for (int i = r + 1; i < k; i++) {
					other = compareRows ? getRow(i) : getCol(i);
					if (current.isEqual(other)) {
						duplicates.add(i);
						temp.add(i);
					}
				}
				if(!temp.isEmpty()){
					fullList.put(r, temp);
				}
			}
			Collections.sort(duplicates);
			return output;
		}
	}


    /**
     * Takes in a Tuple<ArrayList<Integer>, HashMap<Integer, Set<Integer>>> which is two ways of representing the same duplication info of the same occurrence.
     *          and a boolean flag (true = combine row, false = combine cols).
     * @param pair: Tuple<ArrayList<Integer>, HashMap<Integer, Set<Integer>>> which is two ways of representing the same duplication info:
     *                   Tuple._1:  the indices of all rows/cols to be removed.
     *                   Tuple._2:  keys = first occurrence, vals = all duplications (to be removed) of the item indexed by the key.
     * @param combineRow:  if true, remove all rows whose index is in Tuple._1, otherwise remove indexed cols.
     * @return: A copy of the original BooleanMatrix without duplicated rows (if combineRow if true) or columns (if combineRow is false).
     */
	public BooleanMatrix combineVectors(Tuple<ArrayList<Integer>, HashMap<Integer, Set<Integer>>> pair, boolean combineRow) {

		int height = getHeight();
		int width = getWidth();
		BooleanMatrix output = combineRow? new BooleanMatrix(this) : deepTranspose(this);
		ArrayList<Integer> indices = pair._1;
		HashMap<Integer, Set<Integer>> list = pair._2;

		if (height <= 1 || width <= 1 || list.isEmpty()) {
			System.out.println("Cannot remove further, returning original input.");
			return this;
		} else {
			for (int key : list.keySet()) {
				BooleanMatrix original = output.getRow(key);
				Set<Integer> vals = list.get(key);
				for (int index : vals) {
					original = original.xorAdd(output.getRow(index));
				}
				output.setRow(key, original);
			}
			output = output.removeMultiRows(indices);
			if(!combineRow){
				output = deepTranspose(output);
			}
			return output;
		}
	}



    public static BooleanMatrix not(BooleanMatrix A){
        byte[][] result = new byte[A.getHeight()][A.getWidth()];
        for (int i = 0; i <A.getHeight() ; i++) {
            for (int j = 0; j < A.getWidth(); j++) {
                result[i][j] =  A.apply(i,j) == TRUE ? FALSE : TRUE;
            }
        }
        return new BooleanMatrix(result);
    }

    public static boolean allFalse(BooleanMatrix C){
		for (int i = 0; i <C.getHeight() ; i++) {
			for (int j = 0; j < C.getWidth(); j++) {
				if (C.apply(i,j)==  TRUE){
					return false;
				}
			}
		}
		return true;
	}

	public static int[] getStats(BooleanMatrix P,BooleanMatrix R){
		int TP = 0; //really true
		int TN = 0; //really false
		int FP = 0; //really false, but saying true
		int FN = 0; //really true, but saying negative

		for (int i = 0; i <P.getHeight() ; i++) {
			for (int j = 0; j < P.getWidth(); j++) {
				if (P.apply(i,j)==  TRUE){
					if (R.apply(i,j)==  TRUE) {
						TP+=1;
					}else{
						FN+=1;
					}
				}else{
					if (R.apply(i,j)==  TRUE) {
						FP+=1;
					}else{
						TN+=1;
					}
				}
			}
		}
		int[] res = {TP,TN,FP,FN};
		return res;
	}



}

