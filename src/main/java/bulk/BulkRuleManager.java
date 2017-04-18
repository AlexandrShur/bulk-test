package bulk;

import bulk.dto.BulkResponse;
import bulk.dto.OperationStat;
import bulk.dto.RuleInfo;
import bulk.dto.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BulkRuleManager {

    @Autowired
    Database database;

    /**
     * Validate and save rules list.
     * 
     * @param rules list of rules to validate.
     * @return validation response.
     */
    public  BulkResponse validateAndSaveRules(List<Rule> rules) {
        BulkResponse bulkResponse = new BulkResponse();
        int iter = 0;
        for (Rule rule : rules) {
            OperationStat operationStat = addRuleAppPrefix(rule);
            if (operationStat.isStatus()){
                operationStat = database.addRule(rule);
                if(operationStat.isStatus()) {
                    incrementAccptedRule(bulkResponse);
                } else {
                    removeRuleAppPrefix(rule);
                    incrementFailedRule(bulkResponse);
                }
            } else {
                incrementFailedRule(bulkResponse);
            }
            String status = operationStat.isStatus() ? "Accepted" : "Failed";
            bulkResponse.getRules().add(new RuleInfo(iter, status, operationStat.getMessage(), rule));
            iter++;
        }
        return bulkResponse;
    }

    /**
     * Add app prefix to rule name.
     *
     * @param rule rule to check.
     */
    private OperationStat addRuleAppPrefix(Rule rule) {
        if (rule.getName() == null || rule.getName().isEmpty()) {
            return new OperationStat(false, "Rule name is empty!");
        } else if (rule.getApplication() == null || rule.getApplication().isEmpty()) {
            return new OperationStat(false, "Rule application is empty!");
        }
        rule.setName(rule.getApplication() + "-" + rule.getName());
        return new OperationStat(true, "Rule prefix add successfully.");
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

    /**
     * Remove rule app prefix.
     *
     * @param rule rule to remove prefix from.
     */
    private void removeRuleAppPrefix(Rule rule){
        rule.setName(rule.getName().replaceFirst(rule.getApplication() + "-", ""));
    }
}
