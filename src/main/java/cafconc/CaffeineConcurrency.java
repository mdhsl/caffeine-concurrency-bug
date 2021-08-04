package cafconc;

import com.github.benmanes.caffeine.cache.*;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CaffeineConcurrency {
    public static final String FILE = "./data/result.gpkg";

    final static LoadingCache<String, SQLiteManager> cache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MILLISECONDS)
            .removalListener((String filePath, SQLiteManager manager, RemovalCause cause) -> manager.close())
            .build(filePath -> new SQLiteManager(new File(FILE)));


    public byte[] getValue() throws SQLException {
        SQLiteManager sqLiteManager = cache.get(FILE);
        return sqLiteManager.get(37265,19249,16);
    }

    public static void main(String[] args)  {
        CaffeineConcurrency caffeineConcurrency = new CaffeineConcurrency();
        int nbJobs = 10_000;
        int nbThreads = 32;
        ExecutorService executorService = Executors.newFixedThreadPool(nbThreads);
        try {
            for (int i = 0; i < nbJobs; i++) {
                executorService.submit(() -> {
                    try {
                        byte[] data = caffeineConcurrency.getValue();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            caffeineConcurrency.close();
        }
    }

    public void close() {
        cache.invalidateAll();
        cache.cleanUp();
    }
}
