package org.telescope.app.model;

import org.telescope.javel.framework.model.Model;

public class User extends Model {

    public User() {
        ignored = "password,salt,token";
        timestamps = true;
    }
}
