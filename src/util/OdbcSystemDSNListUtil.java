package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class OdbcSystemDSNListUtil {

    public static Set getODBCSystemDNSUser() {
        String PERSONAL_FOLDER_CMD = "HKEY_CURRENT_USER\\SOFTWARE\\ODBC\\ODBC.INI";
        String[] command = new String[]{"reg", "query", PERSONAL_FOLDER_CMD};
        Set dsnList = new HashSet();
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader stream = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String dsn = "";
            while ((dsn = stream.readLine()) != null) {
                if (dsn.indexOf(PERSONAL_FOLDER_CMD + "\\") != -1) {
                    dsnList.add(dsn.substring(dsn.lastIndexOf("\\") + 1));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dsnList;
    }
    
    public static String getODBCSystemDNSUserFolder(String vsDNS) {
        String PERSONAL_FOLDER_CMD = "HKEY_CURRENT_USER\\SOFTWARE\\ODBC\\ODBC.INI\\"+vsDNS;
        String[] command = new String[]{"reg", "query", PERSONAL_FOLDER_CMD,"/v","DefaultDir"};
        Set dsnList = new HashSet();
        String res = "";
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader stream = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String dsn = "";
            while ((dsn = stream.readLine()) != null) {
                if(!dsn.isEmpty())
                    res = dsn;                
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        //gambiarra :/
        int indexOf = res.indexOf("REG_SZ");
        res = res.substring(indexOf+7).trim();
        //gambiarra ;\
        return res;
    }

    public static Set getODBCSystemDNSSistema() {
        String PERSONAL_FOLDER_CMD = "HKEY_LOCAL_MACHINE\\SOFTWARE\\ODBC\\ODBC.INI";
        String[] command = new String[]{"reg", "query", PERSONAL_FOLDER_CMD};
        Set dsnList = new HashSet();
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader stream = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String dsn = "";
            while ((dsn = stream.readLine()) != null) {
                if (dsn.indexOf(PERSONAL_FOLDER_CMD + "\\") != -1) {
                    dsnList.add(dsn.substring(dsn.lastIndexOf("\\") + 1));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dsnList;
    }

//    public static void main(String[] args) {
//        String saida = getODBCSystemDNSUserFolder("PERDIOESTE");
//        System.out.println(saida);
//
//        dsnList = null;
//        dsnList = getODBCSystemDNSUser();
//        for (Object dsn : dsnList) {
//            System.out.println("dsn name: " + dsn);
//        }
//        dsnList = null;
//        dsnList = getODBCSystemDNSSistema();
//        for (Object dsn : dsnList) {
//            System.out.println("dsn name: " + dsn);
//        }
//    }
}