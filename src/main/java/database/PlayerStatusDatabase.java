package database;

import config.Config;
import database.common.DatabaseUtil;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.UUID;

public class PlayerStatusDatabase implements AutoCloseable{
    private final Connection con;

    public PlayerStatusDatabase() throws SQLException {
        this.con = DatabaseUtil.getConnection();
    }

    public int initialize() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS users(" +
                "uuid CHAR(38) UNIQUE NOT NULL," +
                "name VARCHAR(16) NOT NULL," +
                "using_language CHAR(2) DEFAULT 'ja' NOT NULL," +
                "max_thread INTEGER NOT NULL," +
                "use_thread INTEGER NOT NULL" +
                ");";
        Statement statement = con.createStatement();
        return statement.executeUpdate(sql);
    }

    private PlayerStatus getData(UUID uuid) throws SQLException {
        String sql = "SELECT * FROM users WHERE uuid = ?;";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setString(1, uuid.toString());
        ResultSet rs =  preparedStatement.executeQuery();
        if(rs.next()){
            return new PlayerStatus(UUID.fromString(rs.getString("uuid")), rs.getString("name"), rs.getString("using_language"), rs.getInt("max_thread"), rs.getInt("use_thread"));
        }
        return null;
    }private PlayerStatus getData(String name) throws SQLException {
        String sql = "SELECT * FROM users WHERE name = ?;";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setString(1, name);
        ResultSet rs =  preparedStatement.executeQuery();
        if(rs.next()){
            return new PlayerStatus(UUID.fromString(rs.getString("uuid")), rs.getString("name"), rs.getString("using_language"), rs.getInt("max_thread"), rs.getInt("use_thread"));
        }
        return null;
    }

    private int addData(UUID uuid, String name) throws SQLException {
        String sql = "INSERT INTO users(uuid, name, max_thread, use_thread) VALUES(?,?,?,?);";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setString(1, uuid.toString());
        preparedStatement.setString(2, name);
        preparedStatement.setInt(3,Config.getDefaultMaxThread());
        preparedStatement.setInt(4,Config.getDefaultUseThread());
        return preparedStatement.executeUpdate();
    }

    private int updateData(PlayerStatus playerStatus) throws SQLException {
        String sql = "UPDATE users SET name = ?, using_language = ?, max_thread = ?, use_thread = ? WHERE uuid = ?;";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setString(1, playerStatus.getName());
        preparedStatement.setString(2, playerStatus.getUsingLanguage());
        preparedStatement.setInt(3, playerStatus.getMaxThread());
        preparedStatement.setInt(4, playerStatus.getUseThread());
        preparedStatement.setString(5, playerStatus.getUuid().toString());
        return preparedStatement.executeUpdate();
    }

    public PlayerStatus getPlayerStatusByName(String name) throws SQLException {
        return getData(name);
    }

    public PlayerStatus getPlayerStatus(Player player) throws SQLException {
        PlayerStatus playerStatus = getData(player.getUniqueId());
        if(playerStatus == null){
            addData(player.getUniqueId(),player.getName());
            return getPlayerStatus(player);
        }
        return playerStatus;
    }

    public int updatePlayerStatus(PlayerStatus playerStatus) throws SQLException {
        return updateData(playerStatus);
    }

    public PlayerStatus setLanguage(Player player, String language) throws SQLException {
        PlayerStatus playerStatus = getPlayerStatus(player);
        updateData(new PlayerStatus(playerStatus.getUuid(),playerStatus.getName(),language,playerStatus.getMaxThread(),playerStatus.getUseThread()));
        return getPlayerStatus(player);
    }

    @Override
    public void close() throws SQLException {
        this.con.close();
    }
}
