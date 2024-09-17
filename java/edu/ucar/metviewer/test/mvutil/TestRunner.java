package edu.ucar.metviewer.test.mvutil;

import com.sun.net.httpserver.Authenticator;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;


// Useful for running from the command line


public class TestRunner {
    public static void main(String[] args){
        Result result = JUnitCore.runClasses(TestMVUtil.class);

        for (Failure failure : result.getFailures()){
            System.out.println(failure.toString());
        }

        System.out.println("Tests pass: "+ result.wasSuccessful() + "\nNumber of runs: " +
                        result.getRunCount());
    }
}
