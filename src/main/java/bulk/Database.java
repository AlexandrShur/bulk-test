package bulk;

import bulk.dto.OperationStat;
import bulk.dto.Rule;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class Database {

    private List<Rule> rules = new ArrayList<>();

    private int primaryKey = 1;

    /**
     * Add rule to database.
     *
     * @param rule rule to add.
     * @return <code>true</code> if rule unique, in other way returns <code>false</code>
     */
    public OperationStat addRule(Rule rule) {
        for (Rule ruleDb : rules) {
            if (ruleDb.getName().equals(rule.getName())) {
                return new OperationStat(false,"Duplicate role error!");
            }
        }
        rule.setId(this.primaryKey++);
        this.rules.add(rule);
        return new OperationStat(true,"Rule add!");
    }

    /**
     * Reset database data.
     */
    public void resetDatabase() {
        this.rules.clear();
        this.primaryKey = 1;
    }
}
