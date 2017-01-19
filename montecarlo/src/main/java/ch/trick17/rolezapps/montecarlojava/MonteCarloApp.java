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

import static java.util.Collections.unmodifiableList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Code, a test-harness for invoking and driving the Applications Demonstrator
 * classes.
 * <p>
 * To do:
 * <ol>
 * <li>Very long delay prior to connecting to the server.</li>
 * <li>Some text output seem to struggle to get out, without the user tapping
 * ENTER on the keyboard!</li>
 * </ol>
 *
 * @author H W Yau
 * @version $Revision: 1.12 $ $Date: 1999/02/16 19:13:38 $
 */
public class MonteCarloApp {
    
    private static final double pathStartValue = 100.0;
    
    private final int runs;
    private final int nthreads;
    
    private final PathParameters pathParams;
    
    public List<Long> seeds;
    public List<Double> results;
    
    public MonteCarloApp(File ratesFile, int timeSteps, int runs, int nthreads) {
        this.runs = runs;
        this.nthreads = nthreads;
        
        seeds = new ArrayList<>(runs);
        results = new ArrayList<>(runs);
        
        // Measure the requested path rate.
        RatePath rateP = RatePath.readRatesFile(ratesFile);
        ReturnPath returnP = rateP.getReturnCompounded();
        returnP.estimatePath();
        
        // Now prepare for MC runs.
        pathParams = new PathParameters(returnP, timeSteps);
        
        // Now create the seeds for the tasks.
        for(int i = 0; i < runs; i++)
            seeds.add((long) i * 11);
    }
    
    public void run() {
        AppDemoTask tasks[] = new AppDemoTask[nthreads];
        Thread threads[] = new Thread[nthreads];
        for(int i = 1; i < nthreads; i++) {
            tasks[i] = new AppDemoTask(i, runs, nthreads, seeds, pathParams);
            threads[i] = new Thread(tasks[i]);
            threads[i].start();
        }
        
        AppDemoTask task = new AppDemoTask(0, runs, nthreads, seeds, pathParams);
        task.run();
        results.addAll(task.getResults());
        
        for(int i = 1; i < nthreads; i++)
            try {
                threads[i].join();
                results.addAll(tasks[i].getResults());
            } catch(final InterruptedException e) {
                throw new AssertionError(e);
            }
    }
    
    /**
     * Method for doing something with the Monte Carlo simulations. It's probably not mathematically
     * correct, but shall take an average over all the simulated rate paths.
     */
    public double avgExpectedReturnRate() {
        double result = 0.0;
        if(runs != results.size())
            throw new AssertionError(
                    "Fatal: TaskRunner managed to finish with no all the results gathered in!");
        
        for(int i = 0; i < runs; i++)
            result += results.get(i);
        
        result /= runs;
        return result;
    }
    
    private static class AppDemoTask implements Runnable {
        
        private final int id, runs, nthreads;
        private final List<Double> results = new ArrayList<>();
        private final List<Long> seeds;
        private final PathParameters pathParams;
        
        public AppDemoTask(int id, int runs, int nthreads, List<Long> seeds,
                PathParameters pathParams) {
            this.id = id;
            this.runs = runs;
            this.nthreads = nthreads;
            this.seeds = seeds;
            this.pathParams = pathParams;
        }
        
        public void run() {
            int slice = (runs + nthreads - 1) / nthreads;
            int ilow = id * slice;
            
            int iupper;
            iupper = (id + 1) * slice;
            if(id == nthreads - 1)
                iupper = runs;
            
            for(int iRun = ilow; iRun < iupper; iRun++) {
                MonteCarloPath mcPath = new MonteCarloPath(pathParams);
                mcPath.computeFluctuationsGaussian(seeds.get(iRun));
                mcPath.computePathValue(pathStartValue);
                RatePath rateP = new RatePath(mcPath.name, mcPath.startDate, mcPath.endDate,
                        mcPath.dTime, mcPath.getPathValue());
                ReturnPath returnP = rateP.getReturnCompounded();
                returnP.estimatePath();
                
                results.add(returnP.getExpectedReturnRate());
            }
        }
        
        public List<Double> getResults() {
            return unmodifiableList(results);
        }
    }
}
