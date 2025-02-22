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
        int result = statement.executeUpdate(sql);
        statement.close();
        return result;
    }

    private PlayerStatus getData(UUID uuid) throws SQLException {
        String sql = "SELECT * FROM users WHERE uuid = ?;";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setString(1, uuid.toString());
        ResultSet rs =  preparedStatement.executeQuery();
        PlayerStatus playerStatus = null;
        if(rs.next()){
            playerStatus = new PlayerStatus(UUID.fromString(rs.getString("uuid")), rs.getString("name"), rs.getString("using_language"), rs.getInt("max_thread"), rs.getInt("use_thread"));
        }
        rs.close();
        preparedStatement.close();
        return playerStatus;
    }

    private PlayerStatus getData(String name) throws SQLException {
        String sql = "SELECT * FROM users WHERE name = ?;";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setString(1, name);
        ResultSet rs =  preparedStatement.executeQuery();
        PlayerStatus playerStatus = null;
        if(rs.next()){
            playerStatus = new PlayerStatus(UUID.fromString(rs.getString("uuid")), rs.getString("name"), rs.getString("using_language"), rs.getInt("max_thread"), rs.getInt("use_thread"));
        }
        rs.close();
        preparedStatement.close();
        return playerStatus;
    }

    private int addData(UUID uuid, String name) throws SQLException {
        String sql = "INSERT INTO users(uuid, name, max_thread, use_thread) VALUES(?,?,?,?);";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setString(1, uuid.toString());
        preparedStatement.setString(2, name);
        preparedStatement.setInt(3,Config.getDefaultMaxThread());
        preparedStatement.setInt(4,Config.getDefaultUseThread());
        int result = preparedStatement.executeUpdate();
        preparedStatement.close();
        return result;
    }

    private int updateData(PlayerStatus playerStatus) throws SQLException {
        String sql = "UPDATE users SET name = ?, using_language = ?, max_thread = ?, use_thread = ? WHERE uuid = ?;";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setString(1, playerStatus.getName());
        preparedStatement.setString(2, playerStatus.getUsingLanguage());
        preparedStatement.setInt(3, playerStatus.getMaxThread());
        preparedStatement.setInt(4, playerStatus.getUseThread());
        preparedStatement.setString(5, playerStatus.getUuid().toString());
        int result = preparedStatement.executeUpdate();
        preparedStatement.close();
        return result;
    }

    public PlayerStatus getPlayerStatusByName(String name) throws SQLException {
        return this.getData(name);
    }

    public PlayerStatus getPlayerStatus(Player player) throws SQLException {
        PlayerStatus playerStatus = this.getData(player.getUniqueId());
        if(playerStatus == null){
            this.addData(player.getUniqueId(),player.getName());
            return this.getPlayerStatus(player);
        }
        return playerStatus;
    }

    public int updatePlayerStatus(PlayerStatus playerStatus) throws SQLException {
        return this.updateData(playerStatus);
    }

    public PlayerStatus setLanguage(Player player, String language) throws SQLException {
        PlayerStatus playerStatus = this.getPlayerStatus(player);
        this.updateData(new PlayerStatus(playerStatus.getUuid(),playerStatus.getName(),language,playerStatus.getMaxThread(),playerStatus.getUseThread()));
        return this.getPlayerStatus(player);
    }

    @Override
    public void close() throws SQLException {
        this.con.close();
    }
}
