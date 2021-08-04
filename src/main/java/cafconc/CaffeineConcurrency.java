package cafconc;

import com.github.benmanes.caffeine.cache.*;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CaffeineConcurrency {
    public static final String FILE = "./data/database.db";

    final static LoadingCache<String, SQLiteManager> cache = Caffeine.newBuilder()
            .expireAfterWrite(30, TimeUnit.MILLISECONDS)
            .removalListener((String filePath, SQLiteManager manager, RemovalCause cause) -> manager.close())
            .build(filePath -> new SQLiteManager(new File(FILE)));


    public byte[] getValue() throws SQLException {
        SQLiteManager sqLiteManager = cache.get(FILE);
        return sqLiteManager.get("0b3a18cb-1026-492b-ad24-c9a8903ff084");
    }

    public static void main(String[] args)  {
         CaffeineConcurrency caffeineConcurrency = new CaffeineConcurrency();
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                caffeineConcurrency.getValue();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    public void close() {
        cache.invalidateAll();
        cache.cleanUp();
    }
}
