package fr.iban.warps.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import fr.iban.common.data.sql.DbAccess;

public class SqlTables {
	
	/*
	 * Schema relationnel :
	 * 
	 * sc_warps (_idW_, name, description, datecreated, server, world x, y, z, pitch, yaw)
	 * 
	 * sc_warps_players(_uuid_, #idW)
	 * 
	 * sc_warps_rates(_#idW_, _uuid_, vote, date)
	 * 
	 * sc_warps_tags(_#idW_, _tag_)
	 */
	
	public static void createTables() {
		createTable("CREATE TABLE IF NOT EXISTS sc_warps(" + 
				"    idW INT AUTO_INCREMENT," + 
				"    name VARCHAR(255) NOT NULL," + 
				"    description VARCHAR(255) NOT NULL," + 
				"    datecreated bigint," + 
				"    opened BOOLEAN NOT NULL," + 
				"    server VARCHAR(255) NOT NULL," + 
				"    world VARCHAR(255) NOT NULL," + 
				"    x DOUBLE NOT NULL," + 
				"    y DOUBLE NOT NULL," + 
				"    z DOUBLE NOT NULL," + 
				"    pitch FLOAT NOT NULL," + 
				"    YAW FLOAT NOT NULL," + 
				"    CONSTRAINT PK_warps PRIMARY KEY (idW)\n" + 
				");");
		createTable("CREATE TABLE IF NOT EXISTS sc_warps_players(" + 
				"    idW INT NOT NULL AUTO_INCREMENT," + 
				"    uuid VARCHAR(255) NOT NULL UNIQUE," + 
				"    CONSTRAINT PK_pwarps PRIMARY KEY (idW)\n" + 
				");");
		createTable("CREATE TABLE IF NOT EXISTS sc_warps_votes(" + 
				"    idW INT NOT NULL AUTO_INCREMENT," + 
				"    uuid VARCHAR(255) NOT NULL," + 
				"    date BIGINT NOT NULL," + 
				"    vote TINYINT NOT NULL," + 
				"    CONSTRAINT PK_pwarps PRIMARY KEY (idW, uuid)\n" + 
				");");
		createTable("CREATE TABLE IF NOT EXISTS sc_warps_tags(" + 
				"    idW INT NOT NULL AUTO_INCREMENT," + 
				"    tag VARCHAR(255) NOT NULL," + 
				"    CONSTRAINT PK_pwarps PRIMARY KEY (idW, tag)\n" +
				");");
	}
	
	private static void createTable(String statement) {
		try (Connection connection = DbAccess.getDataSource().getConnection()) {
			try(PreparedStatement preparedStatemente = connection.prepareStatement(statement)){
				preparedStatemente.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
