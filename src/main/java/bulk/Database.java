package bulk;

import bulk.dto.Rule;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class Database {

    private List<Rule> rules = new ArrayList<>();

    /**
     * Add rule to database.
     *
     * @param rule rule to add.
     * @return <code>true</code> if rule unique, in other way returns <code>false</code>
     */
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
