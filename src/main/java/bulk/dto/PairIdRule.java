package bulk.dto;

import bulk.dto.Rule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PairIdRule {
    int position;
    Rule rule;
}
