package bulk;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alexandr.shchurenkov on 17-Apr-17.
 */
@Getter
@Setter
public class Database {

    private List<Rule> rules = new ArrayList<>();

    public Database() {
        this.rules.add(new Rule(1,"SomeName","Some description", "Some"));
    }

    public List<PairIdRule> addRulesToDB(List<PairIdRule> pairs) {
        List<PairIdRule> failedRules = new ArrayList<>();
        for (Rule rule : rules) {
            for (PairIdRule pair : pairs) {
                if (rule.getName().equals(pair.getRule().getName())) {
                    failedRules.add(pair);
                }
            }
        }
        return failedRules;
    }
}
