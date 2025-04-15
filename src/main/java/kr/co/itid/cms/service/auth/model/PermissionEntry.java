package kr.co.itid.cms.service.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionEntry implements Serializable {
    private String userId;
    private Integer level;
    private Set<String> permissions;
}
