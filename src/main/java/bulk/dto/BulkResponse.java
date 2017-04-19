package bulk.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class BulkResponse {
    List<RoleInfo> rejectedRoles = new ArrayList<>();
    List<Role> acceptedRoles = new ArrayList<>();
}
