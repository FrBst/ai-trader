package application;

import application.Configuration;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Utils {
     private static final List<String> allDailyAdjustedSymbols;
     private static final List<File> testData;
     private static final List<String> trainData;

     static {
          allDailyAdjustedSymbols = Arrays.stream(new File(Configuration.dailyAdjustedFolder()).list())
                  .map(s -> s.substring(0, s.length() - 4))
                  .collect(Collectors.toList());
          testData = Arrays.stream(new File(Configuration.testDataFolder()).listFiles()).collect(Collectors.toList());
          trainData = Arrays.stream(new File(Configuration.trainDataFolder()).list())
                  .collect(Collectors.toList());
     }

     public static List<File> getTestFiles() { return testData; }
}
