/**
 * ExecutorTest.java Copyright UCAR (c) 2019. University Corporation for Atmospheric Research
 * (UCAR), National Center for Atmospheric Research (NCAR), Research Applications Laboratory (RAL),
 * P.O. Box 3000, Boulder, Colorado, 80307-3000, USA.Copyright UCAR (c) 2019.
 */

package edu.ucar.metviewer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author : tatiana $
 * @version : 1.0 : 2019-02-04 14:09 $
 */
public class ExecutorTest {

  public static void main(String args[]) throws ExecutionException, InterruptedException {

    //int numberOfTasks = Integer.parseInt(args[0]);
    int numberOfTasks = 20;
    ExecutorService executor = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    List<Future<String>> set = new ArrayList<>();
    try {
      for (int i = 0; i < numberOfTasks; i++) {


        Callable<String> callable = new WordLengthCallable(i);
        Future<String> future = executor.submit(callable);
        set.add(future);

      }
    } catch (Exception err) {
      err.printStackTrace();
    }
    executor.shutdown(); // once you are done with ExecutorService
    for (Future<String> future : set) {
      System.out.println(future.get());
    }
  }

  private static class WordLengthCallable implements Callable<String> {
    int intt;

    public WordLengthCallable(int i) {
      intt = i;
    }

    @Override
    public String call() throws Exception {
      int k = intt * intt;
      return String.valueOf(k);
    }
  }
}





