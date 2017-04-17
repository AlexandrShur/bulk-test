package bulk;

import java.util.ArrayList;
import java.util.List;

public class BulkRuleManager {

    public static BulkResponse validateRules(List<Rule> rules) {
        BulkResponse bulkResponse = new BulkResponse();
        List<PairIdRule> rulesToAdd = new ArrayList<>();
        int iter = 0;
        for (Rule rule : rules) {
            if(checkRuleAppPrefix(rule)) {
                bulkResponse.addAcceptedRule();
                rulesToAdd.add(new PairIdRule(iter, rule));
            } else {
                bulkResponse.addFailedRule();
                bulkResponse.getFailedRulesList().add(new PairIdRule(iter, rule));
            }
            iter++;
        }
        fillBulkResponseWithDBResponse(bulkResponse, databaseResponse(rulesToAdd));
        return bulkResponse;
    }

    private static boolean checkRuleAppPrefix(Rule rule) {
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

    private static List<PairIdRule> databaseResponse(List<PairIdRule> rulesToAdd){
        Database database = new Database();
        List<PairIdRule> dbResponse = new ArrayList<>();
        return database.addRulesToDB(rulesToAdd);
    }

    private static void fillBulkResponseWithDBResponse(BulkResponse bulkResponse, List<PairIdRule> dbResponse){
        for (PairIdRule pair : dbResponse) {
            bulkResponse.addFailedRule();
            bulkResponse.getFailedRulesList().add(pair);
        }
    }
}
