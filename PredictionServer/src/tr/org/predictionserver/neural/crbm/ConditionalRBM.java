/************* License relating to this Java implementation **********
 * This is a Java implementation of a Restricted Boltzmann Machine.
 * The GPL license applies to this Java implementation only.
 *
 * Copyright (C) 2008 Benjamin Chung   [benchung AT NO $PAM gmail DOT com]
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

package tr.org.predictionserver.neural.crbm;

import java.util.Random;

/**
 *
 * @author chungb
 */
public class ConditionalRBM {
    
    public static double DEFAULT_LEARNING_RATE = 0.05;
    public static double DEFAULT_MOMENTUM = 0.6;
    public static double DEFAULT_WEIGHTCOST = 0.00002 * DEFAULT_LEARNING_RATE;
    
    public StochasticBinaryLayer hiddenLayer;
    
    public LinearLayer visibleLayer;
    public double[][] weights; // Connection weights for the locked visible layer to the hidden layer.
    public double[][] savedWeights; // Connection weights for the locked visible layer to the hidden layer.
    
    public LinearLayer  visibleOutputLayer;
    public double[][] outputWeights; // Connection weights for the output visible layer to the hidden layer.
    public double[][] savedOutputWeights; // Connection weights for the output visible layer to the hidden layer.
    
    public Random rnd;
    
    public double learningRate;
    public double momentum;
    public double weightCost;
    
    
    
    
    // ------------------ TODO ------------------
    // Change implementation to use RBM instead of double[][] for weights.
    // For this to happen, need to modify RBM interface to include
    // RBM.getUpwardSWSum(batchVisible, visibleHiddenWeights) and
    // RBM.getDownwardSWSum(batchHidden, visibleHiddenWeights) functions
    // ------------------ TODO ------------------
    
    public ConditionalRBM(LinearLayer visibleLayer, LinearLayer visibleOutputLayer, StochasticBinaryLayer hiddenLayer) {
        this(visibleLayer, visibleOutputLayer, hiddenLayer, 
             DEFAULT_LEARNING_RATE, DEFAULT_MOMENTUM, DEFAULT_WEIGHTCOST);
    }
    
    public ConditionalRBM(LinearLayer visibleLayer, LinearLayer visibleOutputLayer, StochasticBinaryLayer hiddenLayer, 
                          double learningRate, double momentum, double weightCost) {
        this.hiddenLayer = hiddenLayer;
        this.visibleOutputLayer = visibleOutputLayer;
        this.visibleLayer = visibleLayer;
        
        final int numHiddenUnits = this.hiddenLayer.numUnits;
        
        this.weights = new double[this.visibleLayer.numUnits][numHiddenUnits];
        this.outputWeights = new double[this.visibleOutputLayer.numUnits][numHiddenUnits];
        Matrix.randomizeElements(this.weights);
        Matrix.randomizeElements(this.outputWeights);
        
        this.rnd = new Random(123L);
        
        this.learningRate=learningRate;
        this.momentum=momentum;
        this.weightCost=weightCost;
    }
 
    
    public void saveWeights(){
    	savedOutputWeights=Matrix.clone(outputWeights);
    	savedWeights=Matrix.clone(weights);
    }
    public void restoreWeights(){
    	outputWeights=savedOutputWeights;
    	weights=savedWeights;
    }

    
    
    
    // Don't need to update visible input layer  biases. Only interested in updating the output visible units.

    
}
