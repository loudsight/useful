package com.loudsight.useful.entity.permission;

import com.loudsight.meta.annotation.Id;
import com.loudsight.meta.annotation.Introspect;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Introspect(clazz = PermissionGrant.class)
public class PermissionGrant {
    @Id
    private String id;
    private Object target;
    private Permission permission;
    private LocalDateTime grantedAt;
    private Subject grantedBy;
    // Denormalized against the "grantee is implicit in whose Subject.permissionGrants list holds
    // it" original design: without it, "who is X shared with" and "what's shared with me" have no
    // way to query PermissionGrant directly (there is no raw-Cypher escape hatch - see
    // Neo4JPersistenceApi.executeNative). Cheap to add now, before this entity has real production
    // data. Always the same Subject reference the grant is embedded under.
    private Subject grantee;

    public PermissionGrant() {
        this.id = UUID.randomUUID().toString();
        // Stored timestamps are UTC instants - see TimeProvider for why the zone is explicit.
        this.grantedAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public PermissionGrant(Object target, Permission permission, Subject grantedBy) {
        this();
        this.target = target;
        this.permission = permission;
        this.grantedBy = grantedBy;
    }

    public PermissionGrant(Object target, Permission permission, Subject grantedBy, Subject grantee) {
        this(target, permission, grantedBy);
        this.grantee = grantee;
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

    public Subject getGrantee() {
        return grantee;
    }

    public void setGrantee(Subject grantee) {
        this.grantee = grantee;
    }
}
