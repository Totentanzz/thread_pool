package com.po_lab.rgr;

import com.po_lab.rgr.other.CustomThreadTest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) {
        int iterationsNum = 10;
        List<Integer> threadsNumbersList = IntStream
                .rangeClosed(1,20)
                .boxed()
                .collect(Collectors.toList());
        List<Integer> tasksNumbersList = IntStream
                .of(1,10,100,1000,10000,100000)
                .boxed()
                .collect(Collectors.toList());
        threadsNumbersList.forEach(threadsNumber->{
            tasksNumbersList.forEach(tasksNumber->{
                System.out.printf(
                        "RUNNING CREATING THREADS EFFICIENCY TEST WITH THREADS NUM=%d, TASKS NUM=%d\n",
                        threadsNumber,
                        tasksNumber);
                CustomThreadTest.runEfficiencyTest(
                        iterationsNum,
                        threadsNumber,
                        tasksNumber,
                        CustomThreadTest.EfficiencyTestType.CREATING_THREADS);
                System.out.printf(
                        "RUNNING HEAVY WORK EFFICIENCY TEST WITH THREADS NUM=%d, TASKS NUM=%d\n",
                        threadsNumber,
                        tasksNumber);
                CustomThreadTest.runEfficiencyTest(
                        iterationsNum,
                        threadsNumber,
                        tasksNumber,
                        CustomThreadTest.EfficiencyTestType.HEAVY_WORK);
            });
        });
        System.exit(0);
    }
}