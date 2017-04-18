package bulk;

import bulk.dto.Rule;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class Database {

    private List<Rule> rules = new ArrayList<>();

    public boolean addRule(Rule rule) {
        for (Rule ruleDb : rules) {
            if (ruleDb.getName().equals(rule.getName())) {
                return false;
            }
        }
        rules.add(rule);
        return true;
    }
}
