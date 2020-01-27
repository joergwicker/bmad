package nz.wicker.bmad.algorithms;

import nz.wicker.bmad.general.Tuple;
import nz.wicker.bmad.matrix.BooleanMatrix;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Random;

public class XORDecompose {
    public static boolean canImprove = true;
    public static BooleanMatrix finalResMatrix;
    private int resError;
    public int totalSize = 0, height = 0, width = 0;
    public double relativeRecError;
    public double calculatdRecError;


    public XORDecompose(BooleanMatrix a){
        this.height = a.getHeight();
        this.width = a.getWidth();
        this.totalSize = height * width;
    }



    /**
     * Alternative version of decompose. Iteratively call decompose with startType == 0 (use random vector) for n times,
     * return the two BooleanMatrix factors with the minimum relative reconstruction error.
     * @param input: A BooleanMatrix object, the original matrix to be decomposed.
     * @param decomposeDim: the dimension of factor matrices.
     * @param startType:
     *                 0 = start with randomly generated non-zero vector;
     *                 2 = start with a random copy of non-zero row/col from the original matrix.
     * @param numIteration: the number of time to iterate (repeatedly call decompose).
     * @return: A Tuple<BooleanMatrix, BooleanMatrix> with the smallest relative reconstruction error among all iterations. (._1 = column matrix, ._2 = row matrix).
     */
    public Tuple<BooleanMatrix, BooleanMatrix> iterativeDecompose(BooleanMatrix input, int decomposeDim,  int startType, int numIteration){
            /*
             * startType:    0 = start with randomly generated non-zero vector;
             *               2 = start with a random copy of non-zero row/col from the original matrix.
             */

        double min = 1, error = -99;
        Tuple<BooleanMatrix, BooleanMatrix> temp, output;
        int pos = 0;
        ArrayList<Tuple<BooleanMatrix, BooleanMatrix>> tupleList = new ArrayList<Tuple<BooleanMatrix, BooleanMatrix>> ();

        XORDecompose xorDec = new XORDecompose(input);

        if(input.getDensity() == 0){
            return xorDec.decompose(input, decomposeDim, startType);
        }else{
            for (int i = 0; i < numIteration; i++) {
                temp = xorDec.decompose(input, decomposeDim, startType);
                error = xorDec.relativeRecError;
                tupleList.add(temp);
                if (error <= min) {
                    min = error;
                    pos = i;
                }
            }
            relativeRecError = min;
            output = tupleList.get(pos);
            BooleanMatrix approximation = xorDec.getProductMatrices(output._1, output._2);

            calculatdRecError = input.relativeReconstructionError(approximation, 1d);
            return output;
        }
    }


    /**
     * Standard decomposition method, with 3 initial-parth-choosing types as described below.
     * Takes in a BooleanMatrix object to decompose, returns a Tuple of two factor matrices (BooleanMatrices objects), and updates a public field of relative reconstruction error.
     * @param input: A BooleanMatrix object, the original matrix to be decomposed.
     * @param decomposeDim: the dimension of factor matrices.
     * @param startType: 0 = start with randomly generated non-zero vector;
     *                   1 = start with density-based vector (set to 1 if row/col density >= matrix density);
     *                   2 = start with a random copy of non-zero row/col from the original matrix.
     * @return: A Tuple<BooleanMatrix, BooleanMatrix> with the smallest relative reconstruction error among all iterations. (._1 = column matrix, ._2 = row matrix).
     */
    public Tuple<BooleanMatrix, BooleanMatrix> decompose(BooleanMatrix input, int decomposeDim, int startType) {
        /*
         * startType:    0 = start with randomly generated non-zero vector;
         *               1 = start with density-based vector (set to 1 if row/col density >= matrix density);
         *               2 = start with a random copy of non-zero row/col from the original matrix.
         */

        if (input.getDensity() == 0) {
            relativeRecError = 0;
            calculatdRecError = 0;
            return new Tuple<BooleanMatrix, BooleanMatrix>(new BooleanMatrix(input.getHeight(), decomposeDim), new BooleanMatrix(decomposeDim, input.getWidth()));
        } else {
            canImprove = true;
            int height = input.getHeight();
            int width = input.getWidth();
            int k = decomposeDim;
            BooleanMatrix a, colMatrix, rowMatrix, resMatrix = new BooleanMatrix(height, width);
            BitSet colVec, rowVec;

            ArrayList<byte[]> rowArray = new ArrayList<byte[]>();
            ArrayList<byte[]> colArray = new ArrayList<byte[]>(); // this one will be transposed later;

            while (k > 0 && canImprove) {
                a = (k == decomposeDim) ? input : resMatrix;

                Tuple<BitSet, BitSet> pair = getPair(a, startType);
                colVec = pair._1;
                rowVec = pair._2;

                if (colVec.cardinality() == 0 || rowVec.cardinality() == 0) {
                    canImprove = false;  // if any new pair contains a [0] vector, no improvement can be made, terminate decomposition.
                    k -= 1;
                } else {
                    resMatrix = getResidualMatrix(a, colVec, rowVec);
                    k -= 1;
                    if (resMatrix.getDensity() == 0) {
                        canImprove = false;  // if any new pair contains a [0] vector, no improvement can be made, terminate decomposition.
                    }
                    rowArray.add(toByteArray(rowVec, width));
                    colArray.add(toByteArray(colVec, height));
                }
            }

            rowMatrix = new BooleanMatrix(rowArray.toArray(new byte[0][0]));
            colMatrix = new BooleanMatrix(colArray.toArray(new byte[0][0]));
            colMatrix = BooleanMatrix.deepTranspose(colMatrix);

            resError = resMatrix.elementCount()[2];
            relativeRecError = resMatrix.getDensity();
            finalResMatrix = resMatrix;

            return new Tuple<BooleanMatrix, BooleanMatrix>(colMatrix, rowMatrix);
        }
    }




    /**
     * Takes a BooleanMatrix object, returns a Tuple<BitSet, BitSet> object that contains a row and a col vector that produces the best approximation.
     * The difference between the approximation and the original matrix can be obtained via getResidualMatrix method.
     * @param a: a BooleanMatrix object representing the original input matrix to be decomposed.
     * @param startType: 0 = start with randomly generated non-zero vector;
     *                   1 = start with density-based vector (set to 1 if row/col density >= matrix density);
     *                   2 = start with a random copy of non-zero row/col from the original matrix.
     * @return: a Tuple<BitSet, BitSet> object  (._1 = col vector, ._2 = row vector) that produces the best approximation.
     */
    public Tuple<BitSet, BitSet> getPair(BooleanMatrix a, int startType) {

        int height = a.getHeight();            // assume a is n by m.
        int width = a.getWidth();
        int count = 0;
        boolean fixRow = false;
        BitSet fixedVec, partner;
        BitSet rowVec;     // 1 by m, looks like a column, corresponds to B in LocalSearchReturnMatrix.
        BitSet colVec;    // n by 1, looks like a column, corresponds to S in LocalSearchReturnMatrix.

        // 1) initialise the longer boolean vector X randomly
        /*
         * startType:    0 = start with randomly generated non-zero vector;
         *               1 = start with density-based vector (set to 1 if row/col density >= matrix density);
         *               2 = start with a random copy of non-zero row/col from the original matrix.
         */
        if (width > height) {
            fixRow = true;
            if(startType == 1){
                rowVec = getVectorByDensity(a, fixRow);
            }else if(startType == 2){
                rowVec = copyFromMatrix(a, fixRow);
            }else{
                rowVec = getRandomVector(width);
            }
            colVec = new BitSet(height);        // The other vector is declared with size (default val = false) to be iteratable.

            fixedVec = rowVec;
            partner = colVec;
        } else {
            if(startType == 1){
                colVec = getVectorByDensity(a, fixRow);
            }else if(startType == 2){
                colVec = copyFromMatrix(a, fixRow);
            }else{
                colVec = getRandomVector(height);
            }
            rowVec = new BitSet(width);        // The other vector is declared with size (default val = false) to be iteratable.

            fixedVec = colVec;
            partner = rowVec;
        }

        while (true) {
            BitSet temp;
            if (count > 0) {
                /* Tested, must use clone() here for all reassignment.
                 * Otherwise temp will eventually point at the same object as partner, be updated together, and lost its purpose for comparison.
                 */
                temp = (BitSet) fixedVec.clone();
                fixedVec = (BitSet) partner.clone();
                partner = (BitSet) temp.clone();
                fixRow = !fixRow;

            } else {
                temp = (BitSet) partner.clone();
            }
            partner = computePartner(a, fixedVec, partner, fixRow); // pick value in partner that minimises total mismatch between original and product.
            count++;

            if (temp.equals(partner)) {
                colVec = fixRow ? partner : fixedVec;
                rowVec = fixRow ? fixedVec : partner;
                Tuple<BitSet, BitSet> output = new Tuple<BitSet, BitSet>(colVec, rowVec);
                return output;
            }
        }
    }



    /**
     * For each cell of partner vector, compare sum of errors if multiplied with fixed vector (refVec)..
     * error = 1 if original cell value != product matrix cell value, = 0 otherwise.
     * partner[cell] is set to 1 if errorOne < errorZero.
     *
     * @param: a BooleanMatrix object.
     * @param: refVec the BitSet object (representing a vector) that is fixed as a basis for computing its partner
     * @param: partner a BitSet object (representing a vector) that is improved based on comparing its cell-wise product with fixedVec vs. original matrix cell value.
     * @param: fixRow a boolean flag, true if fixVec is the row vector (1 by width_of_a), false if it's the col vector (height_of_a by 1)
     * @return: a BitSet object representing the partner vector being computed.
     */
    public BitSet computePartner(BooleanMatrix a, BitSet refVec, BitSet partner, boolean fixRow) {

        int partnerLength, refLength;
        if (fixRow) {
            partnerLength = a.getHeight();
            refLength = a.getWidth();
        } else {
            partnerLength = a.getWidth();
            refLength = a.getHeight();
        }
        int lastPos = 0;

        for (int partnerCell = 0; partnerCell < partnerLength; partnerCell++) {
            int errorOne = 0;
            int errorZero = 0;

            for (int refCell = 0; refCell < refLength; refCell++) {
                boolean original;
                if (fixRow) { // refCell = columns, partnerCell = rows
                    original = (a.apply(partnerCell, refCell) == (byte) 3);
                } else {
                    original = (a.apply(refCell, partnerCell) == (byte) 3);
                }
                boolean refVal = refVec.get(refCell);
                errorOne += (refVal != original) ? 1 : 0;  // if original value != x, errorOne += 1;
                errorZero += (original) ? 1 : 0;       // if original vlue = 1, errorOne += 1;
            }
            if ((errorOne < errorZero) && partner.get(partnerCell) != true) {
                partner.set(partnerCell, true);
            } else if ((errorZero < errorOne) && partner.get(partnerCell) != false) {
                partner.set(partnerCell, false);
            } else if(errorOne == errorZero){
                lastPos = partnerCell;
            }
        }
        if(partner.cardinality() == 0){
            if(lastPos != 0) {
                partner.set(lastPos, true);
            }else{
                Random rnd = new Random();
                partner.set(rnd.nextInt(partnerLength));
            }
        }
        return partner;
    }



    /**
     * Takes three BooleanMatrix objects  (the original matrix a (m by n), a column matrix (m by k) and row matrices (k by n)).
     * Returns their product matrix.
     *         Total number of mismatch (cardinality of the residual matrix) can be checked via totalError, which is used for computing the relative reconstruction error.
     * @param colMatrix: a m by k BooleanMatrix object as one of the decomposition output pair.
     * @param rowMatrix: a k by n BooleanMatrix object as the other decomposition output pair.
     * @return: A BooleanMatrix.
     */
    public BooleanMatrix getProductMatrices(BooleanMatrix colMatrix, BooleanMatrix rowMatrix) {

        // Compute product matrix of rowMatrix and colMatrix.  The addition in dot-product uses XOR. !!!
        BooleanMatrix productMatrix = colMatrix.booleanProduct(rowMatrix, true);
        return productMatrix;
    }



    /**
     * Takes a BooleanMatrix a, two BitSet objects colVec and rowVec.
     * Returns a Tuple of two BooleanMatrix objects of the same size as a, representing the product of the two vectors and the residual matrix.
     * @param a: a BooleanMatrix object, the original matrix to be decomposed/ approximated.
     * @param colVec: a height_of_a by 1 vector, represented by a BitSet.  can be all Zero.
     * @param rowVec: a 1 by width_of_a vector, represented by a BitSet. Can be all zero.
     * @return: Tuple<BooleanMatrix, BooleanMatrix>,  ._1 = product matrix, ._2 = residual matrix.
     */
    public Tuple<BooleanMatrix, BooleanMatrix> getProductAndErrorMatrices(BooleanMatrix a, BitSet colVec, BitSet rowVec) {
        int height = a.getHeight();
        int width = a.getWidth();
        BooleanMatrix productMatrix = new BooleanMatrix(height, width);

        // 1) Compute product matrix of rowVec and colVec
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                byte val = (colVec.get(row) & rowVec.get(col)) ? (byte) 3 : (byte) 0;
                productMatrix.update(row, col, val);
            }
        }
        // 2) Get residual matrix by comparing the original matrix a, and the product matrix just computed.
        BooleanMatrix resMatrix = getResidualMatrix(a, colVec, rowVec);

        Tuple<BooleanMatrix, BooleanMatrix> output = new Tuple<BooleanMatrix, BooleanMatrix>(productMatrix, resMatrix);
        return output;
    }


    //@overload
    /**
     * Takes three BooleanMatrix objects  (the original matrix a (m by n), a column matrix (m by k) and row matrices (k by n)).
     * Returns their product matrix and residual matrix, both are m by n.
     *         Total number of mismatch (cardinality of the residual matrix) can be checked via totalError, which is used for computing the relative reconstruction error.
     * @param a: the original m by n BooleanMatrix object to be decomposed/ approximated.
     * @param colMatrix: a m by k BooleanMatrix object as one of the decomposition output pair.
     * @param rowMatrix: a k by n BooleanMatrix object as the other decomposition output pair.
     * @return: A Tuple<BooleanMatrix, BooleanMatrix>, where ._1 is the prodct matrix, ._2 is the residual matrix, both of size m by n.
     */
    public Tuple<BooleanMatrix, BooleanMatrix> getProductAndErrorMatrices(BooleanMatrix a, BooleanMatrix colMatrix, BooleanMatrix rowMatrix) {

        // 1) Compute product matrix of rowMatrix and colMatrix.  The addition in dot-product uses XOR. !!!
        BooleanMatrix productMatrix = colMatrix.booleanProduct(rowMatrix, true);

        // 2) Get residual matrix by comparing the original matrix a, and the product matrix just computed.
        BooleanMatrix resMatrix = new BooleanMatrix(a.getHeight(), a.getWidth());
        int height = a.getHeight();
        int width = a.getWidth();

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                byte val = (a.apply(row, col) != productMatrix.apply(row, col)) ? (byte) 3 : (byte) 0;
                resMatrix.update(row, col, val);
            }
        }
        Tuple<BooleanMatrix, BooleanMatrix> output = new Tuple<BooleanMatrix, BooleanMatrix>(productMatrix, resMatrix);
        return output;
    }


    /**
     * Takes in an original BooleanMatrix a and two BitSet objects representing a col and row vector pair.
     * Computes the difference-matrix between the original matrix and the prodct of the two vectors.
     *
     * @param a:        a BooleanMatrix object representing the original matrix to be decomposed.
     * @param colVec:   A BitSet object representing a (height_of_a by 1) col vector.
     * @param rowVec:   A BitSet object representing a (1 by width_of_a) row vector.
     * @return: a BooleanMatrix object with the same dimension as the input matrix, which cell value = true if there is a mismatch, otherwise false.
     */
    public BooleanMatrix getResidualMatrix(BooleanMatrix a, BitSet colVec, BitSet rowVec) {
        int height = a.getHeight();
        int width = a.getWidth();

        BooleanMatrix resMatrix = new BooleanMatrix(height, width);

        for (int i = 0; i < height; i++) {
            boolean original, rowval, product, resVal;
            byte fillVal;
            boolean colval = colVec.get(i);

            for (int j = 0; j < width; j++) {
                original = (a.apply(i, j) == (byte) 3);
                rowval = rowVec.get(j);
                product = colval & rowval;

                /* if colval for this row == true, product = rowVec. Compare original cell val in this row with rowval,
                 * otherwise, product == [0], compare original cell val in this row with 0, i.e., copy the original cell val..
                 */
                resVal = colval ? (original != rowval) : original;
                fillVal = resVal ? (byte) 3 : (byte) 0;
                resMatrix.update(i, j, fillVal);
            }
        }
        return resMatrix;
    }


    /**
     * Generate random non-all-zero boolean BitSet object of the given length.
     *
     * @param: length: number of bits of the BitSet object (vector)
     * @return: a BitSet object containing at least one bit == true.
     */
    public static BitSet getRandomVector(int length) {
        Random rnd = new Random();
        BitSet vector = new BitSet(length);
        while (vector.isEmpty()) {
            for (int i = 0; i < length; i++) {
                vector.set(i, rnd.nextBoolean());
            }
        }
        return vector;
    }


    /**
     * Takes a BooleanMatrix a, a boolean flat fixRow, returns a BitSet representing a randomly-chosen non-zero row(if width > height) or col(if height > width) from a.
     * @param a: a BooleanMatrix object.
     * @param fixRow: a boolean flat, true if want to return a copy of row of a, false if want to return a copy of column of a.
     * @return: A BitSet object which bits are set true if the corresponding cell value of the row/col of a is TRUE (byte 3).
     */
    public static BitSet copyFromMatrix(BooleanMatrix a, boolean fixRow){
        int height = a.getHeight();
        int width = a.getWidth();
        Random rnd = new Random();
        int r, length = fixRow? width: height, rBound = fixRow? height : width;
        BitSet copy;

        while(true){
            r = rnd.nextInt(rBound);
            copy = matrixVectorToBitSet(a, r, fixRow);
            if(copy.cardinality() != 0){
                break;
            }
        }
        return copy;
    }


    /**
     * Takes a BitSet object and an int of its designated length, returns a string representation of all its bits.
     *
     * @param a:    a BitSet object to be printed as consecutive bits of length n.
     * @param size: the total number of n bits this BitSet object should have (whether set or not).
     * @return: a string representation of all its bits: "1" = true, "0" = false.
     */
    public static String printableBitSet(BitSet a, int size) {
        String output = "[";
        for (int i = 0; i < size; i++) {
            output += a.get(i) ? "1" : "0";
            if (i < (size - 1)) {
                output += ", ";
            }
        }
        output += "]";
        return output;
    }


    /**
     * Converting a BitSet object with n bits to a byte array (for generating BooleanMatrix object).
     * @param a:    a BitSet object.
     * @param size: the total number of bits of the input BitSet object, whether set or not.
     * @return: a byte array, 3 = "true", 0 = "false".
     */
    public static byte[] toByteArray(BitSet a, int size) {
        byte[] output = new byte[size];
        for (int i = 0; i < size; i++) {
            output[i] = a.get(i) ? (byte) 3 : (byte) 0;
        }
        return output;
    }


    /**
     * Takes a BooleanMatrix object and a boolean flag, returns a BitSet object (a vector) which value = 1 if the corresponding row/col has density > matrix density.
     * @param a: The input BooleanMatrix object.
     * @param fixRow: a boolean flag, = true if the returned BitSet represents a row vector (length = width_of_a), = false if returns a col vector (length = height_of_a).
     * @return: BitSet represents a row(fixRow == true) or col (fixRow == false) vector.
     */
    public static BitSet getVectorByDensity(BooleanMatrix a, boolean fixRow) {
        double density = a.getDensity();
        BitSet output;
        BooleanMatrix b;

        if (fixRow) {
            b = BooleanMatrix.deepTranspose(a);
        } else {
            b = a;
        }
        output = new BitSet(b.getWidth());

        for (int i = 0; i < b.getHeight(); i++) {
            BooleanMatrix row = b.getRow(i);
            double rowDensity = row.getDensity();
            if (rowDensity >= density) {
                output.set(i);
            }
        }
        return output;
    }


    /**
     * Takes a 1 by n, or n by 1 BooleanMatrix object , a pos integer, a boolean flag fixRow.
     * Returns a BitSet representing a copy of the pos-th row/col of the matrix.
     * @param a: A BooleanMatrix.
     * @param pos: index of the target row or col, starting from 0.
     * @param fixRow: true if want to copy a row, false if want to copy a column.
     * @return: a BitSet object, where a bit is set to true if the corresponding value in the row/col of a is TRUE(byte 3).
     */
    public static BitSet matrixVectorToBitSet(BooleanMatrix a, int pos, boolean fixRow) {
        int height = a.getHeight();
        int width = a.getWidth();
        int length = fixRow ? width : height;
        BitSet output = new BitSet(length);

        for (int i = 0; i < length; i++) {
            if (fixRow) {
                if (a.apply(pos, i) != (byte) 0) {
                    output.set(i);
                }
            } else {
                if (a.apply(i, pos) != (byte) 0) {
                    output.set(i);
                }
            }
        }
        return output;
    }
}



