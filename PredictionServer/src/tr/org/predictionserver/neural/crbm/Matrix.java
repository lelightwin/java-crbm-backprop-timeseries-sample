package tr.org.predictionserver.neural.crbm;
/*
 * Matrix.java
 *
 * Created on December 11, 2006, 12:52 AM
 *
 */

/************* License relating to this Java implementation **********
 * This is a Java implementation of a Restricted Boltzmann Machine.
 * The GPL license applies to this Java implementation only.
 *
 * Copyright (C) 2006 Benjamin Chung   [benchung AT NO $PAM gmail DOT com]
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 ********************************************************************* */

import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author chungb
 */
public class Matrix implements Serializable {
    
    public static boolean FORCE_SINGLE_THREADED_MULTIPLICATION = false;
    public static int DEFAULT_NUM_THREADS = Runtime.getRuntime().availableProcessors();
    
    public static ExecutorService es = Executors.newFixedThreadPool(DEFAULT_NUM_THREADS);
    private static ExecutorCompletionService ecs = new ExecutorCompletionService(es);
    
    private static Random rnd = new Random(123L);
    
    /** Creates a new instance of Matrix */
    public Matrix() {
        
    }
    
    public static double[][] zero(double[][] someMatrix) {
        return new double[Matrix.rows(someMatrix)][Matrix.cols(someMatrix)];
    }

    public static double[][] set(double[][] someMatrix, double value) {
        double[][] resultMatrix = Matrix.zero(someMatrix);
        for (int i = 0; i < resultMatrix.length; i++)
            java.util.Arrays.fill(resultMatrix[i], value);
        return resultMatrix;
    }
    

    public static double[][] randomizeElements(double[][] someMatrix) {
        //Random rnd = new Random(1234L);
        for (int i = 0; i < someMatrix.length; i++) 
            for (int j = 0; j < someMatrix[i].length; j++) 
                someMatrix[i][j] = 0.1 * rnd.nextGaussian();
        return someMatrix;
    }
    
    public static double[][] add(double[][] leftSideMatrix, double[][] rightSideMatrix) {
        double[][] sum = Matrix.zero(leftSideMatrix);
        for (int i = 0; i < leftSideMatrix.length; i++) {
            for (int j = 0; j < leftSideMatrix[i].length; j++ ) {
                sum[i][j] = leftSideMatrix[i][j] + rightSideMatrix[i][j];
            }
        }
        return sum;
    }
    public static double[][] subtract(double[][] leftSideMatrix, double[][] rightSideMatrix) {
        double[][] difference = Matrix.zero(leftSideMatrix);
        for (int i = 0; i < leftSideMatrix.length; i++) {
            for (int j = 0; j < leftSideMatrix[i].length; j++ ) {
                difference[i][j] = leftSideMatrix[i][j] - rightSideMatrix[i][j];
            }
        }
        return difference;
    }
    
    public static double[][] subtract(double[][] leftSideMatrix, double[] rightSideArray) {
        double[][] difference = Matrix.zero(leftSideMatrix);
        for (int i = 0; i < difference.length; i++) {
            for (int j = 0; j < difference[i].length; j++ ) {
                difference[i][j] = leftSideMatrix[i][j] - rightSideArray[j];
            }
        }
        return difference;
    }
    
    public static double[][] multiplyElements(double[][] leftSideMatrix, double[][] rightSideMatrix) {
        double[][] product = Matrix.zero(leftSideMatrix);
        for (int i = 0; i < leftSideMatrix.length; i++) {
            for (int j = 0; j < leftSideMatrix[i].length; j++ ) {
                product[i][j] = leftSideMatrix[i][j] * rightSideMatrix[i][j];
            }
        }
        return product;
    }

    public static double vectorProduct( double[] a, double[] b ) {
        double value = 0;
        for ( int i = 0; i < a.length; ++i )
            value += a[i] * b[i];
        return value;
    }
  
    public static double[][] multiply(final double[][] A, final double[][] B) {
        final double[][] C = new double[A.length][B[0].length];

        //es = Executors.newFixedThreadPool(DEFAULT_NUM_THREADS);
        ecs = new ExecutorCompletionService(es);
        
        for (int i = 0; i < A.length; i++) {
            final int row = i;
            ecs.submit(
                new Runnable() {
                    public void run() {
                        double[] arowi = A[row];
                        double[] crowi = C[row];
                        for (int k = 0; k < A[row].length; k++) {
                            double[] browk = B[k];
                            double aik = arowi[k];
                            for (int j = 0; j < B[k].length; j++) {
                                crowi[j] += aik * browk[j];
                            }
                        }

                    }
                 }, 
             null);
	}
        for (int i = 0; i < A.length; i++) {
            try {
                ecs.take();
            } catch (InterruptedException ex) {
                Logger.getLogger(Matrix.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println(ex.getMessage());
            }
        }
        //es.shutdown();
        
        return C;
    }
    
    // --------------------------  Transpose(A) * B -------------------------- //
    /* Single-threaded multiplication */
    public static double[][] transposeMultiply(double[][] leftSideMatrix, double[][] rightSideMatrix) {
        double[][] At = Matrix.getTranspose(leftSideMatrix);
        return Matrix.multiply(At, rightSideMatrix);
    }
    
    // --------------------------  A * Transpose(B) -------------------------- //
    /* Single-threaded multiplication */
    public static double[][] multiplyTranspose(double[][] leftSideMatrix, double[][] rightSideMatrix) {
        double[][] Bt = Matrix.getTranspose(rightSideMatrix);
        return Matrix.multiply(leftSideMatrix, Bt);
    }

    
    public static double[][] multiply(double[][] someMatrix, double someNumber) {
        double[][] resultMatrix = new double[Matrix.rows(someMatrix)][Matrix.cols(someMatrix)];
        for (int i = 0; i < someMatrix.length; i++) {
            for (int j = 0; j < someMatrix[i].length; j++ ) {
                resultMatrix[i][j] = someMatrix[i][j] * someNumber;
            }
        }
        return resultMatrix;
    }
    
    public static double[][] add(double[][] someMatrix, double rightSideNumber) {
        double[][] resultMatrix = new double[Matrix.rows(someMatrix)][Matrix.cols(someMatrix)];
        for (int i = 0; i < someMatrix.length; i++) {
            for (int j = 0; j < someMatrix[i].length; j++ ) {
                resultMatrix[i][j] = someMatrix[i][j] + rightSideNumber;
            }
        }
        return resultMatrix;
    }
    
    public static double[][] normalizeRows(double[][] someMatrix) {
        final int rows = Matrix.rows(someMatrix);
        final int cols = Matrix.cols(someMatrix);
        for (int i = 0; i < rows; i++) {
            double sum = 0.;
            for (int j = 0; j < cols; j++ ) {
                sum += someMatrix[i][j];
            }
            for (int j = 0; j < cols; j++ ) {
                someMatrix[i][j] /= sum;
            }
        }
        return someMatrix;
    }
    
    public static int rows(double[][] someMatrix) {
        return someMatrix.length;
    }
    
    public static int cols(double[][] someMatrix) {
        return someMatrix[0].length;
    }
    
    public static double[] getRow(double[][] someMatrix, int i) {
        return someMatrix[i];
    }
    
    public static double[] getCol(double[][] someMatrix, int i) {
        double[] col_i = new double[Matrix.rows(someMatrix)];
        for (int j = 0; j < Matrix.rows(someMatrix); j++) {
            col_i[j] = someMatrix[j][i];
        }
        return col_i;
    }
    
    public static double[][] addRow(double[][] someMatrix, double[] newRow) {
        double[][] resultMatrix = new double[Matrix.rows(someMatrix) + 1][Matrix.cols(someMatrix)];
        for (int i = 0; i < resultMatrix.length - 1; i++) {
            resultMatrix[i] = someMatrix[i].clone();
        }
        resultMatrix[resultMatrix.length - 1] = newRow.clone();
        return resultMatrix;
    }
    
    public static double[][] addCol(double[][] somematrix, double[] newCol) {
        return Matrix.getTranspose(Matrix.addRow(Matrix.getTranspose(somematrix), newCol));
    }
    
    public static double[] sumRows(double[][] someMatrix) {
        double[] sum = new double[Matrix.cols(someMatrix)];
        for (int i = 0; i < Matrix.cols(someMatrix); i++) {
            sum[i] = Matrix.sumArray(getCol(someMatrix, i));
        }
        return sum;
    }
    /*
     * For an array:
     * 1 2 3 4
     * 5 6 7 8
     *
     * Function returns:
     * 10
     * 26
     */
    public static double[] sumCols(double[][] someMatrix) {
        double[] sum = new double[Matrix.rows(someMatrix)];
        for (int i = 0; i < Matrix.rows(someMatrix); i++) {
            sum[i] = Matrix.sumArray(Matrix.getRow(someMatrix, i));
        }
        return sum;
    }
    
    public static double[][] getTranspose(double[][] someMatrix) {
        double[][] resultMatrix = new double[Matrix.cols(someMatrix)][];
        for (int i = 0; i < Matrix.cols(someMatrix); i++) {
            resultMatrix[i] = Matrix.getCol(someMatrix, i);
        }
        return resultMatrix;
    }

    public static double sumArray(double[] someArray) {
        double sum = 0.0;
        for (int i = 0; i < someArray.length; i++) {
            sum += someArray[i];
        }
        return sum;
    }
    
    public static double[][] sumMatrices(double[][][] arrayOfMatrices) {
        double[][] sumMatrix = new double[arrayOfMatrices[0].length][arrayOfMatrices[0][0].length];
        for (int i = 0; i < arrayOfMatrices.length; i++) {
            for (int j = 0; j < arrayOfMatrices[i].length; j++) {
                for (int k = 0; k < arrayOfMatrices[i][j].length; k++) {
                    sumMatrix[j][k] += arrayOfMatrices[i][j][k];
                }
            }
        }
        return sumMatrix;
    }
    
    /*
     * Takes an array, and shuffles them into random positions.
     *
     * @param data  An array of data vectors. 
     *              eg. data[data_vector_index] = {1, 0, 0, 1}
     * @return      The same array of data vectors, but in rearranged in 
     *              random order.
     */
    public static double[] Randomize(double[] data) {
        for (int i = 0; i < data.length; i++) {
            int r = i + (int) (Math.random() * (data.length - i));
            double t = data[r];
            data[r] = data[i];
            data[i] = t;
        }
        return data;
    }
    
    public static double[] Append(double[] firstarray, double[] secondarray) {
        double[] concatresult;
        if (firstarray == null) {
            concatresult = new double[secondarray.length];
            for (int i = 0; i < secondarray.length; i++) {
                concatresult[i] = secondarray[i];
            }
        } else if (secondarray == null) {
            concatresult = new double[firstarray.length];
            for (int i = 0; i < firstarray.length; i++) {
                concatresult[i] = firstarray[i];
            }
        } else {
            concatresult = new double[firstarray.length + secondarray.length];
            int position = 0;
            for (int i = 0; i < firstarray.length; i++) {
                concatresult[position] = firstarray[i];
                position++;
            }
            for (int i = 0; i < secondarray.length; i++) {
                concatresult[position] = secondarray[i];
                position++;
            }
        }
        return concatresult;
    }
    public static double[][] Append(double[][] firstbatch, double[][] secondbatch) {
        double[][] concatresult = new double[firstbatch.length][];
        for (int i = 0; i < concatresult.length; i++) {
            concatresult[i] = Append(firstbatch[i], secondbatch[i]);
        }
        return concatresult;
    }
    
    public static double[] rowize(double[][] someMatrix) {
        int size = 0;
        for (int i = 0; i < someMatrix.length; i++)
            size += someMatrix[i].length;
        
        double[] combinedArray = new double[size];
        for (int i = 0; i < someMatrix.length; i++) {
            int start = someMatrix[i].length;
            for (int j = 0; j < someMatrix[i].length; j++) {
                combinedArray[j + start * i] = someMatrix[i][j];
            }
        }
        return combinedArray;
    }
    
    public static long[] Randomize(long[] data) {
        for (int i = 0; i < data.length; i++) {
            int r = i + (int) (Math.random() * (data.length - i));
            long t = data[r];
            data[r] = data[i];
            data[i] = t;
        }
        return data;
    }
    
    /*
     * Takes an array of data vectors, and shuffles them into random positions.
     *
     * @param data  An array of data vectors. 
     *              eg. data[data_vector_index] = {1, 0, 0, 1}
     * @return      The same array of data vectors, but in rearranged in 
     *              random order.
     */
    public static double[][] Randomize(double[][] data) {
        for (int i = 0; i < data.length; i++) {
            int r = i + (int) (Math.random() * (data.length - i));
            double[] t = data[r];
            data[r] = data[i];
            data[i] = t;
        }
        return data;
    }

    /*
     * Takes an array of data vectors, and returns an array groups of data
     * vectors. For eg. If, the datavectors[row][] were the following matrix:
     *
     * 1  2  3              datavectors[0]
     * 4  5  6              datavectors[1]x
     * 7  8  9              datavectors[2]
     * 10 11 12             datavectors[3]
     * 13       <-- jagged! datavectors[4].length == 1!
     *
     * And batchSize were = 2, then this function will return:
     *
     * 1.0 2.0 3.0     <-- batches[0][0][]
     * 4.0 5.0 6.0     <-- batches[0][1][] 
     *
     * 7.0 8.0 9.0     <-- batches[1][0][]
     * 10.0 11.0 12.0  <-- batches[1][1][] 
     *
     * 13.0            <-- batches[2][0][] 
     *
     * @param datavectors   The array of data vectors to group into batches.
     * @param batchSize     A positive non-zero number indicating the desired 
     *                      number of data vectors per batch.
     * @return  An array of groups of data vectors.
     */
    public static double[][][] Batchify(double[][] datavectors, int batchSize) {
        int N = datavectors.length;
        int D = datavectors[0].length;
        int numbatches = N / batchSize;
        //double[][][] batches = new double[numbatches + 1][][];
        double[][][] batches;
        
        if (datavectors.length % batchSize == 0) {
            batches = new double[numbatches][][];
            for (int i = 0; i < numbatches; i++) 
                batches[i] = new double[batchSize][D];
            
            int batchindex = 0;
            int batchcounter = 0;
            for (int i = 0; i < N; i++) {
                batches[batchindex][i % batchSize] = datavectors[i].clone();
                batchcounter++;
                if (batchcounter == batchSize) {
                    batchcounter = 0;
                    batchindex++;
                }
            }
        }
        else {
            batches = new double[numbatches + 1][][];
            for (int i = 0; i < numbatches + 1; i++) 
                batches[i] = new double[batchSize][D];

            batches[numbatches] = new double[N % batchSize][D];

            int batchindex = 0;
            int batchcounter = 0;
            for (int i = 0; i < N; i++) {
                batches[batchindex][i % batchSize] = datavectors[i].clone();
                batchcounter++;
                if (batchcounter == batchSize) {
                    batchcounter = 0;
                    batchindex++;
                }
            }
        }
        
        return batches;
    }
    
    public static double[][] Batchify(double[] somearray, int batchsize) {
        double[][] batches;
        int numbatches;

        if (somearray.length % batchsize == 0) {
            numbatches = somearray.length / batchsize;
            batches = new double[numbatches][];
            for (int i = 0; i < numbatches; i++)
                batches[i] = new double[batchsize];
            
            int batchindex = 0;
            int batchcounter = 0;
            for (int i = 0; i < somearray.length; i++) {
                batches[batchindex][i % batchsize] = somearray[i];
                batchcounter++;
                if (batchcounter == batchsize) {
                    batchcounter = 0;
                    batchindex++;
                }
            }
            
        }
        else {
            numbatches = somearray.length / batchsize + 1;
            batches = new double[numbatches][];
            
            for (int i = 0; i < numbatches - 1; i++)
                batches[i] = new double[batchsize];

            batches[numbatches - 1] = new double[somearray.length % batchsize];
            
            int batchindex = 0;
            int batchcounter = 0;
            for (int i = 0; i < somearray.length; i++) {
                batches[batchindex][i % batchsize] = somearray[i];
                batchcounter++;
                if (batchcounter == batchsize) {
                    batchcounter = 0;
                    batchindex++;
                }
            }
        }
        return batches;
    }

    public static double[] Combine(double[] leftArray, double[] rightArray) {
        double[] combinedArray = new double[leftArray.length + rightArray.length];
        int i = 0;
        for (int j = 0; j < leftArray.length; j++) {
            combinedArray[i] = leftArray[j];
            i++;
        }
        for (int j = 0; j < rightArray.length; j++) {
            combinedArray[i] = rightArray[j];
            i++;
        }
        return combinedArray;
    }
    
    public static double getSquaredError(double[] vector1, double[] vector2) {
        double squaredError = 0;
        for (int i = 0; i < vector1.length; i++) {
            squaredError += (vector1[i] - vector2[i]) * (vector1[i] - vector2[i]);
        }
        return squaredError;
    }
    public static double getMeanSquaredError(double[][] vectorBatch1, double[][] vectorBatch2) {
       	double error = 0;
        for (int i = 0; i < vectorBatch1.length; i++) {
            error += getSquaredError(vectorBatch1[i], vectorBatch2[i]);
        }
        return error / vectorBatch1.length;
    }
    
    public static void printArray(double[] array) {
        for (int j = 0; j < array.length; j++)
            System.out.print(array[j] + " ");
        System.out.println("");
    }
    public static void printArray(int[] array) {
        for (int j = 0; j < array.length; j++)
            System.out.print(array[j] + " ");
        System.out.println("");
    }
    
    public static void printMatrix(double[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++)
                System.out.print(matrix[i][j] + " ");
            System.out.println("");
        }
    }
    public static void printMatrix(int[][] matrix) {
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++)
                System.out.print(matrix[i][j] + " ");
            System.out.println("");
        }
    }
    
    
    public static void printCube(double[][][] cube) {
        for (int i = 0; i < cube.length; i++) {
            printMatrix(cube[i]);
        }
        System.out.println("");
    }

    
    public static double[][] clone(double[][] somematrix) {
        double[][] newcopy = new double[somematrix.length][];
        for (int i = 0; i < newcopy.length; i++) {
            newcopy[i] = somematrix[i].clone();
        }
        return newcopy;
    }
    
    public static String toString(double[][] somematrix) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < Matrix.rows(somematrix); i++) {
            for (int j = 0; j < Matrix.cols(somematrix); j++) {
                sb.append(somematrix[i][j]);
                sb.append(" ");
            }
            sb.append('\n');
        }
        return sb.toString();
    }
    
    
    // Function originally from
    public static double hypot(double a, double b) {
        double r;
        if (Math.abs(a) > Math.abs(b)) {
            r = b/a;
            r = Math.abs(a)*Math.sqrt(1+r*r);
        } else if (b != 0) {
            r = a/b;
            r = Math.abs(b)*Math.sqrt(1+r*r);
        } else {
            r = 0.0;
        }
        return r;
    }
    
    // Taken and incorporated from the Jama library of MathWorks and NIST.
    public static double[][] invert(double[][] someMatrix) {
        return Matrix.LUDecomposition(someMatrix, Matrix.identity(someMatrix.length));
    }
    
    // Taken and incorporated from the Jama library of MathWorks and NIST.
    private static double[][] getMatrix(double[][] someMatrix, int i0, int i1, int j0, int j1) {
        double[][] X = new double[i1-i0+1][j1-j0+1];
        try {
            for (int i = i0; i <= i1; i++) {
                for (int j = j0; j <= j1; j++) {
                    X[i-i0][j-j0] = someMatrix[i][j];
                }
            }
        } catch(ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
        return X;
    }
    
    // Taken and incorporated from the Jama library of MathWorks and NIST.
    private static double[][] getMatrix(double[][] someMatrix, int[] r, int j0, int j1) {
        double[][] X = new double[r.length][j1-j0+1];
        try {
            for (int i = 0; i < r.length; i++) {
                for (int j = j0; j <= j1; j++) {
                    X[i][j-j0] = someMatrix[r[i]][j];
                }
            }
        } catch(ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException("Submatrix indices");
        }
        return X;
    }
    
    // Taken and incorporated from the Jama library of MathWorks and NIST.
    private static double[][] LUDecomposition(double[][] A, double[][] B) {
        double[][] LU = Matrix.clone(A);
        int m = A.length;
        int n = A[0].length;
        int[] piv = new int[m];
        for (int i = 0; i < m; i++) {
            piv[i] = i;
        }
        int pivsign = 1;
        double[] LUrowi;
        double[] LUcolj = new double[m];
        
        // Outer loop.
        for (int j = 0; j < n; j++) {
            
            // Make a copy of the j-th column to localize references.
            for (int i = 0; i < m; i++) {
                LUcolj[i] = LU[i][j];
            }
            
            // Apply previous transformations.
            for (int i = 0; i < m; i++) {
                LUrowi = LU[i];
                
                // Most of the time is spent in the following dot product.
                int kmax = Math.min(i,j);
                double s = 0.0;
                for (int k = 0; k < kmax; k++) {
                    s += LUrowi[k]*LUcolj[k];
                }
                
                LUrowi[j] = LUcolj[i] -= s;
            }
            
            // Find pivot and exchange if necessary.
            int p = j;
            for (int i = j+1; i < m; i++) {
                if (Math.abs(LUcolj[i]) > Math.abs(LUcolj[p])) {
                    p = i;
                }
            }
            if (p != j) {
                for (int k = 0; k < n; k++) {
                    double t = LU[p][k]; LU[p][k] = LU[j][k]; LU[j][k] = t;
                }
                int k = piv[p]; piv[p] = piv[j]; piv[j] = k;
                pivsign = -pivsign;
            }
            
            // Compute multipliers.
            if (j < m & LU[j][j] != 0.0) {
                for (int i = j+1; i < m; i++) {
                    LU[i][j] /= LU[j][j];
                }
            }
            
        }
        
        if (B.length != m) {
            throw new IllegalArgumentException("Matrix row dimensions must agree.");
        }
        for (int j = 0; j < n; j++) {
            if (LU[j][j] == 0)
                throw new RuntimeException("Matrix is singular.");
        }
        
        // Copy right hand side with pivoting
        int nx = B[0].length;
        double[][] Xmat = Matrix.getMatrix(B, piv,0,nx-1);
        double[][] X = Xmat;
        
        // Solve L*Y = B(piv,:)
        for (int k = 0; k < n; k++) {
            for (int i = k+1; i < n; i++) {
                for (int j = 0; j < nx; j++) {
                    X[i][j] -= X[k][j]*LU[i][k];
                }
            }
        }
        // Solve U*X = Y;
        for (int k = n-1; k >= 0; k--) {
            for (int j = 0; j < nx; j++) {
                X[k][j] /= LU[k][k];
            }
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < nx; j++) {
                    X[i][j] -= X[k][j]*LU[i][k];
                }
            }
        }
        return Xmat;
        
    }
    
    public static double[][] identity(int n) {
        double[][] identityMatrix = new double[n][n];
        for (int i = 0; i < n; i++)
            identityMatrix[i][i] = 1;
        return identityMatrix;
    }
    
}    
    
    
