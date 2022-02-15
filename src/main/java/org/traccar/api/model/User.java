package org.traccar.api.model;

public class User extends Model {

    public User() {
        ignored = "password,salt,token";
        timestamps = true;
    }
}
