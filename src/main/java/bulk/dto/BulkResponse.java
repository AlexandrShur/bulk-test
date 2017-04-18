package bulk.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BulkResponse {
    int acceptedRules;
    int failedRules;
    List<RuleInfo> rules = new ArrayList<>();
}
