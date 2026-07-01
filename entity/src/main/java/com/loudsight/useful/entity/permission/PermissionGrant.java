package com.loudsight.useful.entity.permission;

import com.loudsight.meta.annotation.Id;
import com.loudsight.meta.annotation.Introspect;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Introspect(clazz = PermissionGrant.class)
public class PermissionGrant {
    @Id
    private String id;
    private Object target;
    private Permission permission;
    private LocalDateTime grantedAt;
    private Subject grantedBy;
    
    public PermissionGrant() {
        this.id = UUID.randomUUID().toString();
        this.grantedAt = LocalDateTime.now();
    }
    
    public PermissionGrant(Object target, Permission permission, Subject grantedBy) {
        this();
        this.target = target;
        this.permission = permission;
        this.grantedBy = grantedBy;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public Object getTarget() {
        return target;
    }
    
    public void setTarget(Object target) {
        this.target = target;
    }
    
    public Permission getPermission() {
        return permission;
    }
    
    public void setPermission(Permission permission) {
        this.permission = permission;
    }
    
    public LocalDateTime getGrantedAt() {
        return grantedAt;
    }
    
    public void setGrantedAt(LocalDateTime grantedAt) {
        this.grantedAt = grantedAt;
    }
    
    public Subject getGrantedBy() {
        return grantedBy;
    }
    
    public void setGrantedBy(Subject grantedBy) {
        this.grantedBy = grantedBy;
    }
}
