package database.common;

import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
    private static String PLUGIN_NAME;
    private static String DATABASE_NAME;

    private DatabaseUtil(){}

    public static void initialize(Plugin plugin, String databaseName){
        PLUGIN_NAME = plugin.getName();
        DATABASE_NAME = databaseName;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:plugins/" + PLUGIN_NAME + "/" + DATABASE_NAME);
    }
}
