package mames1.net.mamesosu.Object;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQL {

    final String HOST;
    final String USER;
    final String PASSWORD;
    final String DATABASE;

    public MySQL() {
        // MySQLの情報を読み込む
        Dotenv dotenv = Dotenv.configure().load();
        HOST = dotenv.get("MYSQL_HOST");
        USER = dotenv.get("MYSQL_USER");
        PASSWORD = dotenv.get("MYSQL_PASSWORD");
        DATABASE = dotenv.get("MYSQL_DATABASE");
    }

    // mysqlに接続する
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://" + HOST + "/" + DATABASE + "?useSSL=false",
                USER,
                PASSWORD
        );
    }
}
