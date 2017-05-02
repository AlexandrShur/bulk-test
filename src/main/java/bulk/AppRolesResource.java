package bulk;

import bulk.dao.service.UserService;
import bulk.model.User;
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
    private UserService userService;

    @PostMapping(value = "_bulk2",
            consumes = "application/json",
            headers = "Accept=application/vnd.intapp+json;version=1",
            produces = MediaType.APPLICATION_JSON_VALUE )
    public User bulkRolesUpload2(@PathVariable String tenant, @RequestBody User user) throws IOException {
        return userService.addUser(user);
    }
}
