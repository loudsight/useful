package com.loudsight.useful.entity.permission;

//import com.loudsight.meta.annotation.Id;
//import com.loudsight.meta.annotation.Introspect;

//@Introspect(clazz = Subject.class)
public class Subject {

//   @Id
   private String id;
//   @Id
   private String name;
   
   private static final Subject anonymous = new Subject();
   
   private static final Subject admin = new Subject();

   public Subject( String id,  String name) {
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

   
   public final String getName() {
      return this.name;
   }

   public final void setName( String var1) {
      this.name = var1;
   }

   public final boolean isBuiltinSubject() {
      return this == anonymous || this == admin;
   }

   public final Subject valueOf( String username) {
      Subject subject;
      if ("anonymous".equals(username)) {
         subject = this.getAnonymous();
      } else {
         if ("admin".equals(username)) {
            throw new IllegalArgumentException("Attempt to create a reserved user");
         }

         subject = new Subject(username, null);
      }

      return subject;
   }


   public static Subject getAnonymous() {
      return Subject.anonymous;
   }


   public static Subject getAdmin() {
      return Subject.admin;
   }
}
