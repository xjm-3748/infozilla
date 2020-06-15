package edu.ncsu.csc.itrust;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import edu.ncsu.csc.itrust.dao.DAOFactory;

/**
 * Provides a few database utilties
 *
 * @author Andy
 *
 */
public class DBUtil {
    /**
     * Used to check if we can actually obtain a connection.
     *
     * @return
     */
    public static boolean canObtainProductionInstance() {
        try {
            DAOFactory.getProductionInstance().getConnection().close();
            return true;
        } catch (SQLException e) {
            System.out.println(e);
            return false;
        }
    }

    /**
     * Close the prepared statement and the connection in a proper way
     *
     * @param conn
     * @param ps
     */
    public static void closeConnection(Connection conn, PreparedStatement ps) {
        try {
            if (ps != null)
                ps.close();
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing connections");
            e.printStackTrace();
        }
    }
