package bulk;

import bulk.dto.BulkResponse;
import bulk.dto.OperationStat;
import bulk.dto.RoleInfo;
import bulk.dto.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BulkRoleManager {

    private final String delimiter = "-";

    @Autowired
    Database database;

    /**
     * Validate and save roles list.
     * 
     * @param roles list of roles to validate.
     * @return validation response.
     */
    public  BulkResponse validateAndSaveRoles(List<Role> roles) {
        BulkResponse bulkResponse = new BulkResponse();
        int iter = 0;
        for (Role role : roles) {
            OperationStat operationStat = addRoleAppPrefix(role);
            if (operationStat.isStatus()){
                operationStat = database.addRole(role);
                if(operationStat.isStatus()) {
                    bulkResponse.getAcceptedRoles().add(role);
                } else {
                    removeRoleAppPrefix(role);
                    bulkResponse.getRejectedRoles().add(getRoleInfo(operationStat, role, iter));
                }
            } else {
                bulkResponse.getRejectedRoles().add(getRoleInfo(operationStat, role, iter));
            }
            iter++;
        }
        return bulkResponse;
    }

    /**
     * Add app prefix to role name.
     *
     * @param role role to check.
     */
    private OperationStat addRoleAppPrefix(Role role) {
        if (role.getName() == null || role.getName().isEmpty()) {
            return new OperationStat(false, "Role name is empty!");
        } else if (role.getApplication() == null || role.getApplication().isEmpty()) {
            return new OperationStat(false, "Role application is empty!");
        }
        role.setName(role.getApplication() + delimiter + role.getName());
        return new OperationStat(true, "Role prefix add successfully.");
    }

    /**
     * Remove role app prefix.
     *
     * @param role role to remove prefix from.
     */
    private void removeRoleAppPrefix(Role role){
        role.setName(role.getName().replaceFirst(role.getApplication() + delimiter, ""));
    }

    /**
     * Get {@link RoleInfo} from input data.
     *
     * @param operationStat status of operation.
     * @param role current role.
     * @param iter index of current role.
     * @return role info.
     */
    private RoleInfo getRoleInfo(OperationStat operationStat, Role role, int iter){
        String status = operationStat.isStatus() ? "Accepted" : "Failed";
       return new RoleInfo(iter, status, operationStat.getMessage(), role);
    }
}
