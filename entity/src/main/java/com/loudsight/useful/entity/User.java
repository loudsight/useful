package com.loudsight.useful.entity;

import com.loudsight.meta.annotation.Introspect;
import com.loudsight.useful.entity.permission.Subject;

@Introspect(clazz = User.class)
public class User extends Subject {

    public User(String name, String id) {
        super(id, name);
    }

    public static boolean isAnonymous(User user) {
        if (user == null) {
            return true;
        }
        return "".equals(user.getId()) && "".equals(user.getName());
    }
}