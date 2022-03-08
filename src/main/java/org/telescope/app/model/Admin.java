package org.telescope.app.model;

import org.telescope.javel.framework.model.Model;

public class Admin extends Model {

    public Admin() {
        ignored = "password,salt,token";
        timestamps = true;
    }
}
