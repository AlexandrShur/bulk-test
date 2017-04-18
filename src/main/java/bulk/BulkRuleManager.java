package bulk;

import bulk.dto.BulkResponse;
import bulk.dto.PairIdRule;
import bulk.dto.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BulkRuleManager {

    @Autowired
    Database database;

    /**
     * Validate rules list.
     * @param rules list of rules to validate.
     * @return validation response.
     */
    public  BulkResponse validateRules(List<Rule> rules) {
        BulkResponse bulkResponse = new BulkResponse();
        int iter = 0;
        for (Rule rule : rules) {
            if(checkRuleAppPrefix(rule) && database.addRule(rule)) {
                incrementAccptedRule(bulkResponse);
            } else {
                incrementFailedRule(bulkResponse);
                bulkResponse.getFailedRulesList().add(new PairIdRule(iter, rule));
            }
            iter++;
        }
        return bulkResponse;
    }

    /**
     * Check rule name prefix.
     *
     * @param rule rule to check.
     * @return <code>true</code> if name contains application prefix.
     */
    private  boolean checkRuleAppPrefix(Rule rule) {
        String app = rule.getApplication();
        String name = rule.getName();
        if (app != null && app.length() > 0) {
            if (name != null && name.length() >= app.length()) {
                if (app.equals(name.substring(0, app.length()))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Increment counter of accepted rules.
     *
     * @param bulkResponse DTO to set counter.
     */
    private void incrementAccptedRule(BulkResponse bulkResponse){
        bulkResponse.setAcceptedRules(bulkResponse.getAcceptedRules() + 1);
    }

    /**
     * Increment counter of failed rules.
     *
     * @param bulkResponse DTO to set counter.
     */
    private void incrementFailedRule(BulkResponse bulkResponse){
        bulkResponse.setFailedRules(bulkResponse.getFailedRules() + 1);
    }
}
