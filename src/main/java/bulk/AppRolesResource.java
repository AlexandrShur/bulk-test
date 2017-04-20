package bulk;

import bulk.dto.BulkResponse;
import bulk.dto.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(AppRolesResource.CONTRACT_BASE_URI)

public class AppRolesResource {

    /**
     * Contract base URI for resource of application specific roles.
     */
    public static final String CONTRACT_BASE_URI = "/{tenant}/api/v1/cds/app-roles";

    @Autowired
    BulkRoleManager bulkRuleManager;

    @PostMapping(value = "_bulk",
            consumes = "application/json",
            headers = "Accept=application/vnd.intapp+json;version=1;",
            produces = MediaType.APPLICATION_JSON_VALUE )
    public BulkResponse bulkRolesUpload(@PathVariable String tenant, @RequestBody  List<Role> roles) throws IOException {
        return bulkRuleManager.validateAndSaveRoles(roles);
    }
}
