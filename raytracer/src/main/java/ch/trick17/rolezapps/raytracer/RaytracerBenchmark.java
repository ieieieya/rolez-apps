package ch.trick17.rolezapps.raytracer;

import static ch.trick17.rolezapps.BenchmarkUtils.instantiateBenchmark;
import static ch.trick17.rolezapps.BenchmarkUtils.runAndPlot;
import static org.openjdk.jmh.annotations.Mode.SingleShotTime;
import static org.openjdk.jmh.annotations.Scope.Thread;
import static rolez.lang.Task.currentTask;

import java.util.Random;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import rolez.lang.Task;

@BenchmarkMode(SingleShotTime)
@Fork(1)
@State(Thread)
public class RaytracerBenchmark {
    
    @Param({"45", "90", "180"})
    int n; // height
    
    @Param({"1", "2", "4", "8", "16", "32"})
    int tasks;
    
    @Param({"Rolez", "Java", "RolezEager"})
    String impl;
    
    RaytracerBenchmarkSetup setup;
    
    @Setup(Level.Iteration)
    public void setup() {
        Task.registerNewRootTask();
        Random random = new Random(42);
        setup = instantiateBenchmark(RaytracerBenchmarkSetup.class, impl, n, tasks, random,
                currentTask().idBits());
    }
    
    @Benchmark
    public int raytracer() {
        return setup.runRaytracer$Unguarded(currentTask().idBits());
    }
    
    @TearDown(Level.Iteration)
    public void tearDown() {
        Task.unregisterRootTask();
    }

    public static void main(String[] args) {
        Options options = new OptionsBuilder().include(RaytracerBenchmark.class.getSimpleName())
                .warmupIterations(10).measurementIterations(30).build();
        runAndPlot(options);
    }
}
