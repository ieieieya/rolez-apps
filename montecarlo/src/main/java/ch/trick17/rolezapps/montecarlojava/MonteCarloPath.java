/**************************************************************************
 *                                                                         *
 *         Java Grande Forum Benchmark Suite - Thread Version 1.0          *
 *                                                                         *
 *                            produced by                                  *
 *                                                                         *
 *                  Java Grande Benchmarking Project                       *
 *                                                                         *
 *                                at                                       *
 *                                                                         *
 *                Edinburgh Parallel Computing Centre                      *
 *                                                                         *
 *                email: epcc-javagrande@epcc.ed.ac.uk                     *
 *                                                                         *
 *      Original version of this code by Hon Yau (hwyau@epcc.ed.ac.uk)     *
 *                                                                         *
 *      This version copyright (c) The University of Edinburgh, 2001.      *
 *                         All rights reserved.                            *
 *                                                                         *
 **************************************************************************/

package ch.trick17.rolezapps.montecarlojava;

import static java.lang.Math.exp;
import static java.lang.Math.sqrt;

import java.util.Random;

/**
 * Class representing the paths generated by the Monte Carlo engine.
 * <p>
 * To do list:
 * <ol>
 * <li><code>double[] pathDate</code> is not simulated.</li>
 * </ol>
 *
 * @author H W Yau
 * @version $Revision: 1.18 $ $Date: 1999/02/16 18:51:28 $
 */
public class MonteCarloPath extends Path {
    
    /**
     * Random fluctuations generated as a series of random numbers with given
     * distribution.
     */
    private final double[] fluctuations;
    /**
     * The path values from which the random fluctuations are used to update.
     */
    private final double[] pathValues;
    /**
     * Value for the mean drift, for use in the generation of the random path.
     */
    private final double expectedReturnRate;
    /**
     * Value for the volatility, for use in the generation of the random path.
     */
    private final double volatility;
    
    public MonteCarloPath(Returns returns, int steps) {
        super(returns.name, returns.startDate, returns.endDate, returns.dTime);
        
        this.expectedReturnRate = returns.expectedReturnRate;
        this.volatility = returns.volatility;
        this.pathValues = new double[steps];
        this.fluctuations = new double[steps];
    }
    
    /**
     * Accessor method for private instance variable <code>pathValue</code>.
     *
     * @return Value of instance variable <code>pathValue</code>.
     */
    public double[] getPathValues() {
        return this.pathValues;
    }
    
    /**
     * Method for calculating the sequence of fluctuations, based around a
     * Gaussian distribution of given mean and variance, as defined in this
     * class' instance variables. Mapping from Gaussian distribution of (0,1) to
     * (mean-drift,volatility) is done via Ito's lemma on the log of the stock
     * price.
     * 
     * @param randomSeed
     *            The psuedo-random number seed value, to start off a given
     *            sequence of Gaussian fluctuations.
     */
    public void computeFluctuationsGaussian(int randomSeed) {
        // First, make use of the passed in seed value.
        Random rnd = new Random(randomSeed);
        
        // Determine the mean and standard-deviation, from the mean-drift and
        // volatility.
        double mean = (expectedReturnRate - 0.5 * volatility * volatility) * this.dTime;
        double sd = volatility * sqrt(this.dTime);
        for(int i = 0; i < fluctuations.length; i++)
            // Now map this onto a general Gaussian of given mean and variance.
            fluctuations[i] = mean + sd * rnd.nextGaussian();
    }
    
    /**
     * Method for calculating the corresponding rate path, given the
     * fluctuations and starting rate value.
     * 
     * @param startValue
     *            the starting value of the rate path, to be updated with the
     *            precomputed fluctuations.
     */
    public void computePathValue(double startValue) {
        pathValues[0] = startValue;
        for(int i = 1; i < pathValues.length; i++)
            pathValues[i] = pathValues[i - 1] * exp(fluctuations[i]);
    }
}
