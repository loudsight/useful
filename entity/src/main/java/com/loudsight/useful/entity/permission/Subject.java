package com.loudsight.useful.entity.permission;

import com.loudsight.meta.annotation.Id;
import org.jspecify.annotations.Nullable;
import com.loudsight.meta.annotation.Introspect;
import java.util.ArrayList;
import java.util.List;

@Introspect(clazz = Subject.class)
public class Subject {

   @Id
   private String id;
   private @Nullable String name;
   private List<PermissionGrant> permissionGrants = new ArrayList<>();

   private static final Subject anonymous = new Subject("anonymous", "Anonymous");
   
   private static final Subject admin = new Subject("admin", "Admin");

   public Subject( String id,  @Nullable String name) {
      this.id = id;
      this.name = name;
   }

   public Subject() {
      this("", "");
   }

   
   public final String getId() {
      return this.id;
   }

   public final void setId( String var1) {
      this.id = var1;
   }

   
   public final @Nullable String getName() {
      return this.name;
   }

   public final void setName(@Nullable String var1) {
      this.name = var1;
   }

   public List<PermissionGrant> getPermissionGrants() {
      return permissionGrants;
   }

   public void setPermissionGrants(List<PermissionGrant> permissionGrants) {
      this.permissionGrants = permissionGrants;
   }

   public final boolean isBuiltinSubject() {
      return id != null && (id.equals(anonymous.id) || id.equals(admin.id));
   }

   public final Subject valueOf( String username) {
      Subject subject;
      if ("anonymous".equals(username)) {
         subject = getAnonymous();
      } else {
         if ("admin".equals(username)) {
            throw new IllegalArgumentException("Attempt to create a reserved user");
         }

         subject = new Subject(username, null);
      }

      return subject;
   }


   public static Subject getAnonymous() {
      return anonymous;
   }


   public static Subject getAdmin() {
      return admin;
   }
}
