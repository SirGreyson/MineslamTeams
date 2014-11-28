package net.mineslam.teams;

import java.sql.*;

public class DataManager {

    private MineslamTeams plugin;
    private Connection connection;

    public DataManager(MineslamTeams plugin) {
        this.plugin = plugin;
    }

    public void setupConnection() {
        //Setup Connection to MySQL Database
        openConnection();
        try {
            //Create Database if it doesn't already exist
            updateSQL("CREATE SCHEMA IF NOT EXISTS " + Settings.MySQL.DATABASE.getString());
            //Setup Team Table
            updateSQL("CREATE TABLE IF NOT EXISTS " +
                    Settings.MySQL.DATABASE.getString() + ".team_data (" +
                    "team_id VARCHAR(16) NOT NULL, " +
                    "leader CHAR(36) NOT NULL, " +
                    "kills INT NULL DEFAULT 0, " +
                    "deaths INT NULL DEFAULT 0, " +
                    "friendly_fire INT NULL DEFAULT 0, " +
                    "PRIMARY KEY (team_id));");
            //Setup Player Table
            updateSQL("CREATE TABLE IF NOT EXISTS " +
                    Settings.MySQL.DATABASE.getString() + ".player_data (" +
                    "player_id CHAR(36) NOT NULL, " +
                    "team_id VARCHAR(16) NOT NULL, " +
                    "promoted INT NULL DEFAULT 0, " +
                    "PRIMARY KEY (player_id));");
            //Print success message
            plugin.getLogger().info("Successfully setup MySQL Database!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error! Could not setup MySQL Database: " + e.getMessage());
        }
    }

    //MySQL Database Connection Methods

    public boolean checkConnection() throws SQLException {
        return connection != null && !connection.isClosed();
    }

    public Connection openConnection() {
        try {
            //Make sure the Connection is not already opened
            if (checkConnection()) {
                return connection;
            }
            //Setup Connection from Configured MySQL Settings
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://" +
                    Settings.MySQL.HOST.getString() + ":" + Settings.MySQL.PORT.getString() + "/" +
                    Settings.MySQL.DATABASE.getString(), Settings.MySQL.USERNAME.getString(), Settings.MySQL.PASSWORD.getString());
            //If successful, print message
            plugin.getLogger().info("Successfully connected to MySQL Database!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error! Could not open MySQL Connection: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            plugin.getLogger().severe("Error! MySQL Driver not found! Please install MySQL on this machine");
        }
        return connection;
    }

    public void closeConnection() {
        try {
            //Make sure the Connection is not already closed
            if (!checkConnection()) {
                return;
            }
            //Close the open Connection
            connection.close();
            connection = null;
            //If successful, print message
            plugin.getLogger().info("Successfully closed connection to MySQL Database!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Error! Could not close MySQL Connection: " + e.getMessage());
        }
    }

    public ResultSet querySQL(String query) throws SQLException {
        if (!checkConnection()) {
            closeConnection();
        }
        Statement statement = connection.createStatement();
        return statement.executeQuery(query);
    }

    public int updateSQL(String update) throws SQLException {
        if (!checkConnection()) {
            openConnection();
        }
        Statement statement = connection.createStatement();
        return statement.executeUpdate(update);
    }
}
