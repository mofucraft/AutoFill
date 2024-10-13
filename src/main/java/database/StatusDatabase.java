package database;

import database.common.DatabaseUtil;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.UUID;

public class StatusDatabase implements AutoCloseable{
    private final Connection con;

    public StatusDatabase() throws SQLException {
        this.con = DatabaseUtil.getConnection();
        Initialize();
    }

    private int Initialize() throws SQLException {
        Statement statement = con.createStatement();
        String sql = "CREATE TABLE IF NOT EXISTS users(" +
                "uuid CHAR(38) UNIQUE NOT NULL," +
                "name VARCHAR(16) NOT NULL," +
                "length INTEGER DEFAULT 0 NOT NULL," +
                "using_language CHAR(2) DEFAULT 'ja' NOT NULL" +
                ");";
        return statement.executeUpdate(sql);
    }

    private PlayerStatus getData(UUID uuid) throws SQLException {
        String sql = "SELECT * FROM users WHERE uuid = ?;";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setString(1, uuid.toString());
        ResultSet rs =  preparedStatement.executeQuery();
        if(rs.next()){
            return new PlayerStatus(UUID.fromString(rs.getString("uuid")), rs.getString("name"), rs.getInt("length"), rs.getString("using_language"));
        }
        return null;
    }

    private int addData(UUID uuid, String name) throws SQLException {
        String sql = "INSERT INTO users(uuid, name) VALUES(?,?);";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setString(1, uuid.toString());
        preparedStatement.setString(2, name);
        return preparedStatement.executeUpdate();
    }

    private int updateData(PlayerStatus playerStatus) throws SQLException {
        String sql = "UPDATE users SET name = ?, length = ?, using_language = ? WHERE uuid = ?;";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setString(1, playerStatus.getName());
        preparedStatement.setInt(2, playerStatus.getLength());
        preparedStatement.setString(3, playerStatus.getUsingLanguage());
        preparedStatement.setString(4, playerStatus.getUuid().toString());
        return preparedStatement.executeUpdate();
    }

    public PlayerStatus getPlayerStatus(Player player) throws SQLException {
        PlayerStatus playerStatus = getData(player.getUniqueId());
        if(playerStatus == null){
            addData(player.getUniqueId(),player.getName());
            return getPlayerStatus(player);
        }
        return playerStatus;
    }

    public PlayerStatus setLength(Player player, int length) throws SQLException {
        PlayerStatus playerStatus = getPlayerStatus(player);
        updateData(new PlayerStatus(playerStatus.getUuid(),playerStatus.getName(),length,playerStatus.getUsingLanguage()));
        return getPlayerStatus(player);
    }

    public PlayerStatus setLanguage(Player player, String language) throws SQLException {
        PlayerStatus playerStatus = getPlayerStatus(player);
        updateData(new PlayerStatus(playerStatus.getUuid(),playerStatus.getName(),playerStatus.getLength(),language));
        return getPlayerStatus(player);
    }

    @Override
    public void close() throws SQLException {
        this.con.close();
    }
}
