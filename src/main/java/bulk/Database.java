package bulk;

import bulk.dto.OperationStat;
import bulk.dto.Role;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class Database {

    private List<Role> roles = new ArrayList<>();

    private int primaryKey = 1;

    /**
     * Add role to database.
     *
     * @param role role to add.
     * @return <code>true</code> if role unique, in other way returns <code>false</code>
     */
    public OperationStat addRole(Role role) {
        for (Role roleDb : roles) {
            if (roleDb.getName().equals(role.getName())) {
                return new OperationStat(false,"Duplicate role error!");
            }
        }
        role.setId(this.primaryKey++);
        this.roles.add(role);
        return new OperationStat(true,"Role add!");
    }

    /**
     * Reset database data.
     */
    public void resetDatabase() {
        this.roles.clear();
        this.primaryKey = 1;
    }
}
