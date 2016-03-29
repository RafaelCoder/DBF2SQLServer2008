package databases;

import java.util.List;
import java.util.Map;

public class DBFODBCDataBase extends DataBase{
    
    public List<Map<String, Object>> ExecQuery(String vsSQL){
        return ExecuteQuery(vsSQL);
    }
    
}
