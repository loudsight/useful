package com.loudsight.useful.entity;

import com.loudsight.meta.annotation.Introspect;
import org.jspecify.annotations.Nullable;
import com.loudsight.useful.entity.permission.Role;
import com.loudsight.useful.entity.permission.Subject;

import java.util.ArrayList;
import java.util.List;

@Introspect(clazz = User.class)
public class User extends Subject {
    private List<Role> roles = new ArrayList<>();

    public User() {
        super();
    }

    public User(@Nullable String name, String id) {
        super(id, name);
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public static boolean isAnonymous(User user) {
        return user == null || ("".equals(user.getId()) && "".equals(user.getName()));
    }
}
