package application;

import application.Configuration;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Utils {
     private static final List<String> allDailyAdjustedSymbols;
     private static final List<File> testData;
     private static final List<File> trainData;
     private static final SortedMap<String, Double> marketGlobalMetric;

     static {
          try {
               allDailyAdjustedSymbols = Arrays.stream(new File(Configuration.dailyAdjustedFolder()).list())
                       .map(s -> s.substring(0, s.length() - 4))
                       .collect(Collectors.toList());
               testData = Arrays.stream(new File(Configuration.testDataFolder()).listFiles()).collect(Collectors.toList());
               trainData = Arrays.stream(new File(Configuration.trainDataFolder()).listFiles())
                       .collect(Collectors.toList());
          } catch (NullPointerException e) {
               throw new IllegalArgumentException("A file path is not specified in 'configuration.properties' file.", e);
          }

          // Doing this here since it does not change
          marketGlobalMetric = new TreeMap<>();
          try (BufferedReader br = new BufferedReader(new FileReader(Configuration.getConfig("data-folder") + "global-metric.csv"))) {
               br.readLine();
               String line;
               while ((line = br.readLine()) != null) {
                    String[] tokens = line.split(",");
                         marketGlobalMetric.put(tokens[0], Double.parseDouble(tokens[1]));
               }
          } catch (FileNotFoundException e) {
               throw new IllegalArgumentException(e);
          } catch (IOException e) {
               throw new RuntimeException(e);
          }
     }

     /** Get the market's global metric from startDate, inclusive, to endDate, exclusive.
      *
      * @param startDate
      * @param endDate
      * @return A sorted map containing the data.
      */
     public static SortedMap<String, Double> getGlobalMetric(String startDate, String endDate) {
          return new TreeMap<>(marketGlobalMetric.subMap(startDate, endDate));
     }

     public static void addMetricPoint(String date, double value) {
          marketGlobalMetric.put(date, value);
     }

     public static List<File> getTrainFiles() { return trainData; }
}
