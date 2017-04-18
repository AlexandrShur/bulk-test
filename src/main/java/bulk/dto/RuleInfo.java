package bulk.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RuleInfo {
    private int position;
    private String status;
    private String message;
    private Rule rule;
}
