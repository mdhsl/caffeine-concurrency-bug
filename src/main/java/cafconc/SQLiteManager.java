package cafconc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class SQLiteManager {
    private static Logger LOGGER = LoggerFactory.getLogger(SQLiteManager.class);

    private Connection connection;

    protected String tableName;
    protected String dataName;
    protected String xName;
    protected String yName;
    protected String zName;

    protected File dbFile;

    public SQLiteManager(File dbFile) {
        this.dbFile = dbFile;
        initColumnsName();
        this.initConnection();
    }

    private void initConnection() {
        // caution this constructor will create a new connection pool
        String url = dbFile.getAbsolutePath();
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + url + "");
        } catch (SQLException ex) {
            LOGGER.error(ex.getMessage());
        }
        LOGGER.debug("The database " + url + " has been opened");
    }

    protected void initColumnsName() {
        tableName = "tiles";
        dataName = "tile_data";
        xName = "tile_column";
        yName = "tile_row";
        zName = "zoom_level";
    }

    byte[] get(int x, int y, int z) throws SQLException {
        try(Statement statement = connection.createStatement()) {
            byte[] data = null;
            try (ResultSet rs = statement.executeQuery(
                    new StringBuilder().append("SELECT ").append(dataName).append(" from ").append(tableName)
                            .append(" where ").append(xName).append("=").append(x).append("").append(" and ")
                            .append(yName).append("=").append(y).append(" and ").append(zName).append("=").append(z).toString()
            )) {
                if (rs.next()) {
                    data = rs.getBytes(dataName);
                }
            }
            return data;
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
                LOGGER.info("The database " + this.dbFile.getAbsolutePath() + " has been closed");
            }
        } catch (SQLException e) {
            LOGGER.info("Cannot close DB connection: " + e.getMessage());
        }
    }
}
