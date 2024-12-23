package fr.iban.warps.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sql.DataSource;

import fr.iban.bukkitcore.CoreBukkitPlugin;
import fr.iban.common.model.MSPlayer;
import fr.iban.warps.WarpsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import fr.iban.common.data.sql.DbAccess;
import fr.iban.common.teleport.SLocation;
import fr.iban.warps.model.PlayerWarp;
import fr.iban.warps.model.Vote;
import fr.iban.warps.model.Warp;

public class Storage {

	/*
	 * Schema relationnel :
	 *
	 * sc_warps (_idW_, name, description, opened, server, x, y, z, pitch, yaw)
	 *
	 * sc_warps_players(_uuid_, #idW)
	 *
	 * sc_warps_votes(_#idW_, _uuid_, vote, date)
	 *
	 * sc_warps_tags(_#idW_, _tag_)
	 */

	private final WarpsPlugin plugin;
	private final DataSource dataSource = DbAccess.getDataSource();

	public Storage(WarpsPlugin plugin) {
		this.plugin = plugin;
	}

	public List<PlayerWarp> getPlayersWarps(){
		long now = System.currentTimeMillis();
		List<PlayerWarp> warps = new ArrayList<>();
		try(Connection connection = dataSource.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(
					"SELECT * FROM sc_warps JOIN sc_warps_players ON sc_warps.idW=sc_warps_players.idW;")){
				//ps.setString(1, CoreBukkitPlugin.getInstance().getServerName());
				try(ResultSet rs = ps.executeQuery()){
					while(rs.next()) {
						int id = rs.getInt("idW");

						//location
						String server = rs.getString("server");
						String world = rs.getString("world");
						double x = rs.getDouble("x");
						double y = rs.getDouble("y");
						double z = rs.getDouble("z");
						float pitch = rs.getFloat("pitch");
						float yaw = rs.getFloat("yaw");

						String name = rs.getString("name");
						String description = rs.getString("description");

						boolean isOpened = rs.getBoolean("opened");

						UUID uuid = UUID.fromString(rs.getString("uuid"));

						PlayerWarp pwarp = new PlayerWarp(id, uuid, new SLocation(server, world, x, y, z, pitch, yaw), name, description);

						pwarp.setOpened(isOpened);
						pwarp.setTags(getTags(id, connection));
						pwarp.setVotes(getVotes(id, connection));

						MSPlayer msPlayer = CoreBukkitPlugin.getInstance().getPlayerManager().getOfflinePlayer(uuid);

						OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
						if(isOpened && now - msPlayer.getLastSeenTimestamp() > 1296000000L) {
							plugin.getLogger().info("[Warps]" + op.getName() + " est inactif depuis plus de 15 jours, son warp a été fermé.");
							pwarp.setOpened(false);
							saveWarp(pwarp);
						}
						warps.add(pwarp);
					}
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		plugin.getLogger().info(warps.size() + " warps joueurs chargés en " + (System.currentTimeMillis() - now) + " ms.");
		return warps;
	}

	public List<Warp> getWarps(){
		List<Warp> warps = new ArrayList<>();
		try(Connection connection = dataSource.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(
					"SELECT * " +
							"FROM sc_warps LEFT JOIN sc_warps_players ON sc_warps.idW=sc_warps_players.idW " +
							"WHERE sc_warps_players.idW IS NULL;")){
				//ps.setString(1, CoreBukkitPlugin.getInstance().getServerName());
				try(ResultSet rs = ps.executeQuery()){
					while(rs.next()) {
						int id = rs.getInt("idW");

						//location
						String server = rs.getString("server");
						String world = rs.getString("world");
						double x = rs.getDouble("x");
						double y = rs.getDouble("y");
						double z = rs.getDouble("z");
						float pitch = rs.getFloat("pitch");
						float yaw = rs.getFloat("yaw");

						String name = rs.getString("name");
						String description = rs.getString("description");

						boolean isOpened = rs.getBoolean("opened");


						Warp warp = new Warp(id, new SLocation(server, world, x, y, z, pitch, yaw), name, description);
						warp.setOpened(isOpened);
						warp.setTags(getTags(id, connection));
						warp.setVotes(getVotes(id, connection));
						warps.add(warp);
					}
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return warps;
	}

	public boolean hasWarp(UUID uuid) {
		try(Connection connection = dataSource.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(
					"SELECT * " +
							"FROM sc_warps JOIN sc_warps_players ON sc_warps.idW=sc_warps_players.idW WHERE uuid=?;")){
				ps.setString(1, uuid.toString());
				try(ResultSet rs = ps.executeQuery()){
					return rs.next();
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public PlayerWarp getPlayerWarp(UUID uuid){
		try(Connection connection = dataSource.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(
					"SELECT * " +
							"FROM sc_warps JOIN sc_warps_players ON sc_warps.idW=sc_warps_players.idW WHERE uuid=?;")){
				ps.setString(1, uuid.toString());
				try(ResultSet rs = ps.executeQuery()){
					if(rs.next()) {
						int id = rs.getInt("idW");

						//location
						String server = rs.getString("server");
						String world = rs.getString("world");
						double x = rs.getDouble("x");
						double y = rs.getDouble("y");
						double z = rs.getDouble("z");
						float pitch = rs.getFloat("pitch");
						float yaw = rs.getFloat("yaw");

						String name = rs.getString("name");
						String description = rs.getString("description");

						boolean isOpened = rs.getBoolean("opened");

						PlayerWarp pwarp = new PlayerWarp(id, uuid, new SLocation(server, world, x, y, z, pitch, yaw), name, description);
						pwarp.setOpened(isOpened);
						pwarp.setTags(getTags(id, connection));
						pwarp.setVotes(getVotes(id, connection));
						return pwarp;
					}
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Warp getWarp(int id){
		try(Connection connection = dataSource.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(
					"SELECT * FROM sc_warps WHERE idW=?")){
				ps.setInt(1, id);
				try(ResultSet rs = ps.executeQuery()){
					if(rs.next()) {
						//location
						String server = rs.getString("server");
						String world = rs.getString("world");
						double x = rs.getDouble("x");
						double y = rs.getDouble("y");
						double z = rs.getDouble("z");
						float pitch = rs.getFloat("pitch");
						float yaw = rs.getFloat("yaw");

						String name = rs.getString("name");
						String description = rs.getString("description");

						boolean isOpened = rs.getBoolean("opened");

						Warp warp = new Warp(id, new SLocation(server, world, x, y, z, pitch, yaw), name, description);
						warp.setOpened(isOpened);
						warp.setTags(getTags(id, connection));
						warp.setVotes(getVotes(id, connection));
						return warp;
					}
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Warp getSystemWarp(String warpName) {
		String sql = "SELECT * FROM sc_warps LEFT JOIN sc_warps_players ON sc_warps.idW=sc_warps_players.idW WHERE sc_warps_players.idW IS NULL AND name LIKE ?;";
		try(Connection connection = dataSource.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(sql)){
				ps.setString(1, warpName);
				try(ResultSet rs = ps.executeQuery()){
					if(rs.next()) {
						//location
						int id = rs.getInt("idW");
						String server = rs.getString("server");
						String world = rs.getString("world");
						double x = rs.getDouble("x");
						double y = rs.getDouble("y");
						double z = rs.getDouble("z");
						float pitch = rs.getFloat("pitch");
						float yaw = rs.getFloat("yaw");

						String name = rs.getString("name");
						String description = rs.getString("description");

						boolean isOpened = rs.getBoolean("opened");

						Warp warp = new Warp(id, new SLocation(server, world, x, y, z, pitch, yaw), name, description);
						warp.setOpened(isOpened);
						warp.setTags(getTags(id, connection));
						warp.setVotes(getVotes(id, connection));
						return warp;
					}
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void deleteWarp(Warp warp) {
		String warpSql = "DELETE FROM sc_warps WHERE idW=?;";
		try (Connection connection = dataSource.getConnection()) {
			try(PreparedStatement ps = connection.prepareStatement(warpSql)){
				ps.setInt(1, warp.getId());
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		if(!warp.getTags().isEmpty()) {
			String tagsSql = "DELETE FROM sc_warps_tags WHERE idW=?;";
			try (Connection connection = dataSource.getConnection()) {
				try(PreparedStatement ps = connection.prepareStatement(tagsSql)){
					ps.setInt(1, warp.getId());
					ps.executeUpdate();
				}
			}catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(!warp.getVotes().isEmpty()) {
			String rateSql = "DELETE FROM sc_warps_votes WHERE idW=?;";
			try (Connection connection = dataSource.getConnection()) {
				try(PreparedStatement ps = connection.prepareStatement(rateSql)){
					ps.setInt(1, warp.getId());
					ps.executeUpdate();
				}
			}catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if(warp instanceof PlayerWarp) {
			String pwarpSql = "DELETE FROM sc_warps_players WHERE idW=?;";
			try (Connection connection = dataSource.getConnection()) {
				try(PreparedStatement ps = connection.prepareStatement(pwarpSql)){
					ps.setInt(1, warp.getId());
					ps.executeUpdate();
				}
			}catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void addWarp(Warp warp) {
		String sql = "INSERT INTO sc_warps (name, description, datecreated, server, world, x, y, z, pitch, yaw, opened)"
				+ " VALUES(?, ?, ?, ?, ?, ?, ?, ? , ?, ?, ?);";
		try(Connection connection = dataSource.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(sql)){
				ps.setString(1, warp.getName());
				ps.setString(2, warp.getDesc());
				ps.setLong(3, System.currentTimeMillis());
				ps.setString(4, warp.getLocation().getServer());
				ps.setString(5, warp.getLocation().getWorld());
				ps.setDouble(6, warp.getLocation().getX());
				ps.setDouble(7, warp.getLocation().getY());
				ps.setDouble(8, warp.getLocation().getZ());
				ps.setFloat(9, warp.getLocation().getPitch());
				ps.setFloat(10, warp.getLocation().getYaw());
				ps.setBoolean(11, warp.isOpened());
				ps.executeUpdate();
			}
			if(warp instanceof PlayerWarp pwarp) {
				String pwarpsql = "INSERT INTO sc_warps_players (idW, uuid) VALUES(LAST_INSERT_ID(), ?);";
				try(PreparedStatement ps = connection.prepareStatement(pwarpsql)){
					ps.setString(1, pwarp.getOwner().toString());
					ps.executeUpdate();
				}
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void saveWarp(Warp warp) {
		String sql = "UPDATE sc_warps SET name=?, description= ?, server=?, world=?, x=?, y=?, z=?, pitch=?, yaw=?, opened=? WHERE idW=?";
		try(Connection connection = dataSource.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(sql)){
				ps.setString(1, warp.getName());
				ps.setString(2, warp.getDesc());
				ps.setString(3, warp.getLocation().getServer());
				ps.setString(4, warp.getLocation().getWorld());
				ps.setDouble(5, warp.getLocation().getX());
				ps.setDouble(6, warp.getLocation().getY());
				ps.setDouble(7, warp.getLocation().getZ());
				ps.setFloat(8, warp.getLocation().getPitch());
				ps.setFloat(9, warp.getLocation().getYaw());
				ps.setBoolean(10, warp.isOpened());
				ps.setInt(11, warp.getId());
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<String> getTags(int id, Connection connection) throws SQLException{
		List<String> tags = new ArrayList<>();
		String sql = "SELECT tag FROM sc_warps_tags WHERE idW=?;";
		try(PreparedStatement ps = connection.prepareStatement(sql)){
			ps.setInt(1, id);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					tags.add(rs.getString("tag"));
				}
			}
		}
		return tags;
	}

	// sc_warps_votes(_#idW_, _uuid_, vote, date)
	public Map<String, Vote> getVotes(int id, Connection connection) throws SQLException{
		Map<String, Vote> votes = new HashMap<>();
		String sql = "SELECT uuid, vote, date FROM sc_warps_votes WHERE idW=?;";
		try(PreparedStatement ps = connection.prepareStatement(sql)){
			ps.setInt(1, id);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					votes.put(rs.getString("uuid"), new Vote(rs.getByte("vote"), rs.getLong("date")));
				}
			}
		}
		return votes;
	}

	public void addTag(Warp warp, String tag) {
		String sql = "INSERT INTO sc_warps_tags (idW, tag) VALUES(?, ?);";
		try(Connection connection = dataSource.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(sql)){
				ps.setInt(1, warp.getId());
				ps.setString(2, tag);
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void removeTag(Warp warp, String tag) {
		String sql = "DELETE FROM sc_warps_tags WHERE idW=? AND tag=?;";
		try(Connection connection = dataSource.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(sql)){
				ps.setInt(1, warp.getId());
				ps.setString(2, tag);
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	//sc_warps_votes(_#idW_, _uuid_, vote, date)
	public void vote(Warp warp, UUID uuid, Vote vote) {
		String sql = "INSERT INTO sc_warps_votes (idW, uuid, vote, date) VALUES(?, ?, ?, ?) ON DUPLICATE KEY UPDATE vote=VALUES(vote), date=VALUES(date);";
		try(Connection connection = dataSource.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(sql)){
				ps.setInt(1, warp.getId());
				ps.setString(2, uuid.toString());
				ps.setByte(3, vote.getVote());
				ps.setLong(4, vote.getDate());
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void unvote(Warp warp, UUID uuid) {
		String sql = "DELETE FROM sc_warps_votes WHERE idW=? AND uuid=?;";
		try(Connection connection = dataSource.getConnection()){
			try(PreparedStatement ps = connection.prepareStatement(sql)){
				ps.setInt(1, warp.getId());
				ps.setString(2, uuid.toString());
				ps.executeUpdate();
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}


}
