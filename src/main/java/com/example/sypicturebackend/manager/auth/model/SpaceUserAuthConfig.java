package com.example.sypicturebackend.manager.auth.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author 诺诺
 */
@Data
public class SpaceUserAuthConfig implements Serializable {
  
    /**  
     * 权限列表  
     */  
    private List<SpaceUserPermission> permissions;
  
    /**  
     * 角色列表  
     */  
    private List<SpaceUserRole> roles;  
  
    private static final long serialVersionUID = 1L;  
}