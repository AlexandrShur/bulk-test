package bulk;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BulkResponse {
    int acceptedRules;
    int failedRules;
    List<PairIdRule> failedRulesList = new ArrayList<>();

    public void addAcceptedRule(){
        acceptedRules++;
    }

    public void addFailedRule(){
        failedRules++;
    }
}
