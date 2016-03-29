/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controller;

import com.sun.xml.internal.ws.org.objectweb.asm.Type;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import main.Principal;
import util.DBFFileFilter;
import static util.OdbcSystemDSNListUtil.getODBCSystemDNSUserFolder;

/**
 *
 * @author Rafael
 */
public class Converter {

    private Connection conDest, conDBF;
    private Requester req;
    private String pathname;

    public Converter(Requester req) {
        this.req = req;
    }

    public void conectarDBF(String vsBase, String user, String password) throws Exception {
        if (conDBF != null) {
            return;
        }
        Class.forName("sun.jdbc.odbc.JdbcOdbcDriver").newInstance();
        //conDBF = DriverManager.getConnection("jdbc:odbc:TESTEFERRI0001", "", "");
        //conDBF = DriverManager.getConnection("jdbc:odbc:PERDIOESTE", user, password);
        conDBF = DriverManager.getConnection("jdbc:odbc:" + vsBase, user, password);
        pathname = getODBCSystemDNSUserFolder(vsBase);
    }

    public void desconectarDBF() {
        if (conDBF == null) {
            return;
        }
        try {
            conDBF.close();
            conDBF = null;
        } catch (SQLException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void conectarDest(String host, String base, String user, String password) throws Exception {
        if (conDest != null) {
            return;
        }
        Class.forName("com.mysql.jdbc.Driver");//.newInstance();
        conDest = DriverManager.getConnection("jdbc:mysql://"+host+":3306/"+base, user, password);
//        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
//        conDest = DriverManager.getConnection("jdbc:sqlserver://" + host + ";databaseName=" + base + ";integratedSecurity=false;CharacterSet=UTF-8",
//                user, password);
    }

    public void desconectarDest() {
        if (conDest == null) {
            return;
        }
        try {
            conDest.close();
            conDest = null;
        } catch (SQLException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void convert() {
        try {
            if (conDBF == null || conDest == null) {
                return;
            }

            if (conDBF.isClosed() || conDest.isClosed()) {
                return;
            }

            File f = new File(pathname);
            File[] listFiles = f.listFiles(new DBFFileFilter());
            for (File file : listFiles) {
                if (file.isDirectory()) {
                    continue;
                }
                System.out.println(file.getName().substring(0, file.getName().length()-4));

            }
            //importTable("func");

        } catch (Exception ex) {
            ////JOptionPane.showMessageDialog(null, ex.getMessage(), "erro", JOptionPane.ERROR_MESSAGE, null);
            System.err.println(ex.getMessage());
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void importTable(String tableName) throws Exception {
        List<String> lInserts = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        ResultSetMetaData md = null;
        try {
            lInserts = new ArrayList<>();
            ps = conDBF.prepareStatement("select top 51 * from " + tableName);
            rs = ps.executeQuery();

            md = rs.getMetaData();

            int columnCount = md.getColumnCount();
            //String tableName = md.getTableName(1);
            String vsCreate = "CREATE TABLE " + tableName + "(\n\r";
            String vsInsert = "INSERT INTO " + tableName + "(";
            for (int i = 1; i <= columnCount; i++) {
                String vsType;
                String vsColumn = md.getColumnName(i).trim().toUpperCase();
                if (vsColumn.equalsIgnoreCase("END")) {
                    vsColumn = "END1";
                }
                int viType = md.getColumnType(i);
                vsInsert += (i > 1 ? "," : "") + vsColumn;
                switch (viType) {
                    case 12:
                    case Type.CHAR:
                        vsType = "CHAR(" + md.getColumnDisplaySize(i) + ")";
                        break;
                    case Type.INT:
                        //vsType = "INTEGER(" + md.getColumnDisplaySize(i) + ")";
                        vsType = "INTEGER";
                        break;
                    case Type.DOUBLE:
                    case Type.FLOAT:
                        vsType = "FLOAT";
                        break;
                    case -7:
                    case Type.BOOLEAN:
                        vsType = "CHAR(1)";
                        break;
                    case 91: // date
                        vsType = "DATE";
                        break;
                    default:
                        vsType = "VARCHAR";
                        break;
                }
                //vsType = vsType.toUpperCase()
                //        .replaceAll("LOGICAL", "BOOLEAN");
                vsCreate += "   " + vsColumn + " " + vsType;
                if (i < columnCount) {
                    vsCreate += ",";
                }
                vsCreate += "\n\r";
            }
            vsCreate += ");\n\r";
            vsInsert += ") VALUES \n\r";

            String vsValues = "";
            int count = 0;
            while (rs.next()) {
                if (++count > 1) {
                    vsValues += ",\n\r";
                }
                vsValues += "(";
                for (int i = 1; i <= columnCount; i++) {
                    Object voValor = rs.getObject(i);
                    vsValues += (i > 1 ? "," : "");
                    if (voValor == null) {
                        vsValues += "null";
                    } else {
                        switch (md.getColumnType(i)) {
                            case Type.INT:
                            case Type.DOUBLE:
                            case Type.FLOAT:
                                vsValues += voValor.toString();
                                break;
                            case -7:
                            case Type.BOOLEAN:
                                //vsValues += ((boolean) voValor ? "true" : "false");
                                vsValues += ((boolean) voValor ? "'S'" : "'N'");
                                break;
                            case 91: // date
                                vsValues += "'" + ((java.sql.Date) voValor).toString() + "'";
                                break;
                            default:
                                vsValues += "'" + voValor.toString() + "'";
                                break;
                        }
                    }

                }
                vsValues += ")";
                if (count == 50) {
                    count = 0;
                    lInserts.add(vsInsert + vsValues);
                    vsValues = "";
                }
            }

            if (!vsValues.isEmpty()) {
                lInserts.add(vsInsert + vsValues);
            }

            try {
                conDest.prepareCall("DROP TABLE " + tableName).execute();
            } catch (Exception ex) {
                System.out.println("[ERRO PREVISTO] " + ex.getMessage());
            }

            conDest.prepareCall(vsCreate).execute();
            for (String ins : lInserts) {
                conDest.prepareCall(ins).execute();
            }
            req.addMessage(" - " + tableName + " OK.");
        } finally {
            lInserts = null;
            md = null;
            if (rs != null) {
                rs.close();
                rs = null;
            }
            if (ps != null) {
                ps.close();
                ps = null;
            }
        }
    }
}
