package controller;

import application.Configuration;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class LoadDataWindow {
    String datasetFolder = "src/main/resources/dataset";
    String activeSymbolsFile = "src/main/resources/dataset/active.csv";
    String delistedSymbolsFile = "src/main/resources/dataset/delisted.csv";
    String dailyDataFolder = "src/main/resources/dataset/daily/";
    String dailyAdjustedDataFolder = "src/main/resources/dataset/daily-adjusted/";

    public void downloadAllDailyAdjusted() throws IOException {
        File active = new File(activeSymbolsFile);
        File delisted = new File(delistedSymbolsFile);
        File dailyAdjustedDir = new File(dailyAdjustedDataFolder);
        if (!active.exists() || !delisted.exists()) {
            downloadSymbolList();
        }
        if (!dailyAdjustedDir.exists()) {
            if (!dailyAdjustedDir.mkdirs()) {
                throw new FileSystemException(dailyAdjustedDataFolder);
            }
        }

        List<String> records = new LinkedList<>();
        // Fill the list.
        // todo: propper logging.
        // todo: better UI! + loading bars.
        // todo: separate into two try-blocks.
        try (BufferedReader br1 = new BufferedReader(new FileReader(active));
                BufferedReader br2 = new BufferedReader(new FileReader(delisted))) {
            String line;
            br1.readLine();
            br2.readLine();
            while((line = br1.readLine()) != null) {
                String[] values = line.split(",");
                if (values[3].equals("Stock")) {
                    records.add(values[0]);
                } else {
                    System.out.println(values[0] + " skipped");
                }
            }
            while((line = br2.readLine()) != null) {
                String[] values = line.split(",");
                if (values[3].equals("Stock")) {
                    records.add(values[0]);
                } else {
                    System.out.println(values[0] + " skipped");
                }
            }
        }

        records.removeAll(Arrays.stream(dailyAdjustedDir.list())
                .map(s -> s.substring(0, s.length() - 4))
                .collect(Collectors.toList()));

        for (String entry : records) {
            downloadDailyAdjusted(entry);
        }
    }

    public void downloadSymbolList() throws IOException {

        FileUtils.copyURLToFile(
                new URL("https://www.alphavantage.co/query?function=LISTING_STATUS&apikey=" + Configuration.getConfig("key")),
                new File(activeSymbolsFile),
                3000,
                3000);
        System.out.println("Downloaded active symbols");
        FileUtils.copyURLToFile(
                new URL("https://www.alphavantage.co/query?function=LISTING_STATUS&state=delisted&apikey=" + Configuration.getConfig("key")),
                new File(delistedSymbolsFile),
                3000,
                3000);
        System.out.println("Downloaded delisted symbols");
    }

    private void downloadDailyAdjusted(String symbol) throws IOException {
        int attempts = 0;
        while (attempts < 10) {
            try {
                FileUtils.copyURLToFile(
                        new URL("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol=" + symbol +
                                "&outputsize=full&datatype=csv&apikey=" + Configuration.getConfig("key")),
                        new File(dailyAdjustedDataFolder + symbol + ".csv"),
                        3000,
                        3000);
                System.out.println(symbol + " downloaded");
                break;
            } catch (SocketTimeoutException e) {
                attempts++;
                System.out.println("Socket timeout on attempt " + attempts);
            } catch (IOException e) {
                if (e.getMessage().contains("Server returned HTTP response code: 505")) {
                    System.out.println("Server did not accept symbol \'" + symbol + "\'");
                    return;
                } else {
                    System.out.println("Exception message " + e.getMessage());
                    System.out.println("for symbol " + symbol);
                    return;
                }
            }
        }
        if (attempts == 10)
            throw new IOException("Server not responding");
    }

    public void checkFiles() throws IOException {
        Files.walk(Paths.get(datasetFolder)).filter(Files::isRegularFile).forEach(f -> deleteIfInvalid(f.toString()));
    }

    private void deleteIfInvalid(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            if ((line = br.readLine()) == null || line.equals("{")) {
                System.out.println("File " + path + " downloaded with error, deleting...");
                br.close();
                if (!new File(path).delete()) {
                    throw new IOException("Could not delete " + path);
                }
            }
        } catch (IOException e) {
            // todo: should probably do it some other way.
            throw new RuntimeException();
        }
    }

    // todo: autocreate directories.
    public void divideData() throws IOException {
        ArrayList<File> symbols = Arrays.stream(new File(dailyAdjustedDataFolder).listFiles()).collect(Collectors.toCollection(ArrayList<File>::new));
        Collections.shuffle(symbols);
        Path copied;
        for (int i = 0; i < symbols.size(); i++) {
            if (i >= 500) {
                copied = Paths.get("src/main/resources/dataset/train/" + symbols.get(i).getName());
            } else {
                copied = Paths.get("src/main/resources/dataset/test/" + symbols.get(i).getName());
            }
            Path originalPath = symbols.get(i).toPath();
            Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
