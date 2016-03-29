package databases;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public abstract class DataBase {
    private boolean vbAutoCommit;
    protected Connection con;
    protected PreparedStatement ps;
    protected ResultSet rs;

    public Connection getConnection(String url, String user, String password, String jdbcDriver) {
        try {
            vbAutoCommit = false;
            Class.forName(jdbcDriver).newInstance();
            /*if(user.isEmpty() || password.isEmpty())
                con = DriverManager.getConnection(url);
            else */
                con = DriverManager.getConnection(url, user, password);
            //con.setAutoCommit(vbAutoCommit);
            //System.out.println(con.getCatalog());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            //ex.printStackTrace();
            JOptionPane.showMessageDialog(null, ex.getMessage(), "erro", JOptionPane.ERROR_MESSAGE);
        }
        return con;
    }

    public void commit() throws SQLException {
        if(vbAutoCommit) return;
        con.commit();
    }

    public void rollback() {
        if(vbAutoCommit) return;
        try {
            con.rollback();
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    public void closeConnection() {
        try {
            if (rs != null) {
                rs.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (con != null) {
                con.close();
            }
            con = null;
            ps = null;
            rs = null;
            System.gc();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public boolean isConnected() {
        if (con == null) {
            return false;
        }
        try {
            return !con.isClosed();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    protected void Execute(String vsSQL, Object... params) throws Exception {
        ps = con.prepareStatement(vsSQL,
                ResultSet.TYPE_SCROLL_SENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                Object par = params[i];
                System.out.print(par+(i < (params.length-1)?", ":"\n"));
                converteTipo(i, par);
            }
        }
        System.out.println(con.nativeSQL(vsSQL));
        ps.execute();
    }

    protected void Execute(String vsSQL) throws Exception {
        Execute(vsSQL, (Object[]) null);
    }

    protected List<Map<String, Object>> ExecuteQuery(String vsSQL, Object... params) {
        System.out.println(vsSQL);
        List<Map<String, Object>> res = null;
        try {
            ps = con.prepareStatement(vsSQL,
                    ResultSet.TYPE_SCROLL_SENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    Object par = params[i];
                    converteTipo(i, par);
                }
            }
            res = toList(ps.executeQuery());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }finally{
            try {
                if(rs!=null)
                    rs.close();
                rs = null;
            } catch (SQLException ex) {
                Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                if(ps!=null)
                    ps.close();
                ps = null;
            } catch (SQLException ex) {
                Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }

        return res;
    }

    protected List<Map<String, Object>> ExecuteQuery(String vsSQL) {
        return ExecuteQuery(vsSQL, (Object[]) null);
    }

    public List<Map<String, Object>> toList(ResultSet rs) {
        List<Map<String, Object>> res = new ArrayList<>();
        ResultSetMetaData md;
        int columns = 0;
        try {
            md = rs.getMetaData();
            columns = md.getColumnCount();

            try {
                rs.beforeFirst();
            } catch (Exception e) {
            }

            while (rs.next()) {
                HashMap row = new HashMap();
                for (int i = 1; i <= columns; i++) {
                    row.put(md.getColumnLabel(i), rs.getObject(i));
                }
                res.add(row);
            }
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            md = null;
            columns = -1;
            try {
                rs.close();
                rs = null;
            } catch (SQLException ex) {
                Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return res;
    }

    private void converteTipo(Integer i, Object par) throws SQLException {
        if (par == null) {
            //ps.setNull(i + 1, Types.NULL);
            ps.setString(i + 1, "");
        }else if (par.getClass() == String.class) {
            ps.setString(i + 1, par.toString());
        } else if (par.getClass() == Integer.class) {
            ps.setInt(i + 1, Integer.valueOf(par.toString()));
        } else if (par.getClass() == Float.class) {
            ps.setFloat(i + 1, Float.parseFloat(par.toString()));
        } else if (par.getClass() == java.sql.Date.class) {
            ps.setDate(i + 1, (java.sql.Date)par);
        } else if (par.getClass() == java.util.Date.class){
            ps.setDate(i + 1, new java.sql.Date(((java.util.Date)par).getTime()));
        }else {
            ps.setObject(i + 1, par);
        }
    }

}