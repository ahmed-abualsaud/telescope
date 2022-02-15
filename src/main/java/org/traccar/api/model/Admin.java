package org.traccar.api.model;

public class Admin extends Model {

    public Admin() {
        ignored = "password,salt,token";
        timestamps = true;
    }
}
