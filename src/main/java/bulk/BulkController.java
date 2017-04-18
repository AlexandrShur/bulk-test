package bulk;

import bulk.dto.BulkResponse;
import bulk.dto.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(BulkController.CONTRACT_BASE_URI)
public class BulkController {

    /**
     * Contract base URI for resource of application specific roles.
     */
    public static final String CONTRACT_BASE_URI = "/api/v1/cds/app-roles";

    @Autowired
    BulkRuleManager bulkRuleManager;

    @PostMapping(value = "bulk",
            consumes = "application/json",
            headers = "Accept=application/vnd.intapp+json;version=1",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public BulkResponse bulkRolesUpload(@RequestBody  List<Rule> rules) throws IOException {
        return bulkRuleManager.validateRules(rules);
    }
}
