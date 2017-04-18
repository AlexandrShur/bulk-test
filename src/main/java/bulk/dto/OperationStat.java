package bulk.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by alexandr.shchurenkov on 18-Apr-17.
 */
@Getter
@Setter
@AllArgsConstructor
public class OperationStat {
    private boolean status;
    private String message;
}
