/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import com.sun.xml.internal.ws.org.objectweb.asm.Type;
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


/**
 *
 * @author Rafael
 */
public class Principal {
    private static Connection conDest, conDBF;
    public static void main(String[] args) {
        String vsContent = "";
        List<String> lInserts = new ArrayList<>();
        try {
            //Class.forName("com.mysql.jdbc.Driver").newInstance();
            //conDest = DriverManager.getConnection("jdbc:mysql://localhost:3306/ferri_0001", "RHEDE", "IRHEDE9SYSTEM");
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
            conDest = DriverManager.getConnection("jdbc:sqlserver://RAFAEL-PC\\SAGE;databaseName=ferri0001;integratedSecurity=false;CharacterSet=UTF-8",
                    "SA", "Cordilheir@2008");
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver").newInstance();
            //conDBF = DriverManager.getConnection("jdbc:odbc:TESTEFERRI0001", "", "");
            conDBF = DriverManager.getConnection("jdbc:odbc:PERDIOESTE", "", "");

            PreparedStatement ps = conDBF.prepareStatement("select top 51 * from func");
            ResultSet rs = ps.executeQuery();

            ResultSetMetaData md = rs.getMetaData();

            int columnCount = md.getColumnCount();
            String tableName = md.getTableName(1);
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
            //conDest.prepareCall(vsInsert).execute();
            vsContent += vsCreate + vsInsert;

        } catch (Exception ex) {
            ////JOptionPane.showMessageDialog(null, ex.getMessage(), "erro", JOptionPane.ERROR_MESSAGE, null);
            System.err.println(ex.getMessage());
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                conDest.close();
            } catch (SQLException ex) {
                Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                conDBF.close();
            } catch (SQLException ex) {
                Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //System.out.println(vsContent);
    }
}
