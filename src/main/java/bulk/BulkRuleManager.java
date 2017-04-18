package bulk;

import bulk.dto.BulkResponse;
import bulk.dto.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BulkRuleManager {

    @Autowired
    Database database;

    public  BulkResponse validateRules(List<Rule> rules) {
        BulkResponse bulkResponse = new BulkResponse();
        int iter = 0;
        for (Rule rule : rules) {
            if(checkRuleAppPrefix(rule) && database.addRule(rule)) {
                incrementAccpetedRule(bulkResponse);
            } else {
                incrementFailedRule(bulkResponse);
                bulkResponse.getFailedRulesList().add(new PairIdRule(iter, rule));
            }
            iter++;
        }
        return bulkResponse;
    }

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

    private void incrementAccpetedRule(BulkResponse bulkResponse){
        bulkResponse.setAcceptedRules(bulkResponse.getAcceptedRules() + 1);
    }

    private void incrementFailedRule(BulkResponse bulkResponse){
        bulkResponse.setFailedRules(bulkResponse.getFailedRules() + 1);
    }
}
