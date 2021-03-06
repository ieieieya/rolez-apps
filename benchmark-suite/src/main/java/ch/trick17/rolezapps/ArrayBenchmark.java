package ch.trick17.rolezapps;

import static org.openjdk.jmh.annotations.Mode.SingleShotTime;

import java.util.ArrayList;
import java.util.Random;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.carrotsearch.hppc.IntArrayList;

import rolez.lang.GuardedArray;

@BenchmarkMode(SingleShotTime)
@Fork(1)
@State(Scope.Thread)
@Warmup(iterations = 30)
@Measurement(iterations = 30)
public class ArrayBenchmark {
    
    int n = 10000000;
    
    int[] intArray = new int[n];
    Integer[] integerArray = new Integer[n];
    ArrayList<Integer> integerList = new ArrayList<>(n);
    GuardedArray<int[]> guardedIntArray = new GuardedArray<>(new int[n]);
    IntArrayList intList = new IntArrayList(n);
    
    @Setup
    public void setup() {
        Random random = new Random();
        for(int i = 0; i < n; i++) {
            int a = random.nextInt();
            intArray[i] = a;
            integerArray[i] = a;
            integerList.add(a);
            guardedIntArray.setInt(i, a);
            intList.set(i, a);
        }
    }
    
    @Benchmark
    public long intArray() {
        long sum = 0;
        for(int i = 0; i < n; i++)
            sum += intArray[i];
        return sum;
    }
    
    @Benchmark
    public long integerArray() {
        long sum = 0;
        for(int i = 0; i < n; i++)
            sum += integerArray[i];
        return sum;
    }
    
    @Benchmark
    public long integerList() {
        long sum = 0;
        for(int i = 0; i < n; i++)
            sum += integerList.get(i);
        return sum;
    }
    
    @Benchmark
    public long guardedIntArray() {
        long sum = 0;
        for(int i = 0; i < n; i++)
            sum += guardedIntArray.getInt(i);
        return sum;
    }
    
    @Benchmark
    public long intList() {
        long sum = 0;
        for(int i = 0; i < n; i++)
            sum += intList.get(i);
        return sum;
    }
    
    public static void main(String[] args) throws RunnerException {
        String name = ArrayBenchmark.class.getSimpleName();
        new Runner(new OptionsBuilder().include(name).build()).run();
    }
    
    public static class GuardedArrayProgram {
        public static void main(String[] args) {
            ArrayBenchmark benchmark = new ArrayBenchmark();
            benchmark.setup();
            long sum = 0;
            for(int i = 0; i < 50000; i++)
                sum += benchmark.guardedIntArray();
            System.out.println(sum);
        }
    }
    
    public static class IntegerListProgram {
        public static void main(String[] args) {
            ArrayBenchmark benchmark = new ArrayBenchmark();
            benchmark.setup();
            long sum = 0;
            for(int i = 0; i < 50000; i++)
                sum += benchmark.integerList();
            System.out.println(sum);
        }
    }
}
