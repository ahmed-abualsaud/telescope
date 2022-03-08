package org.telescope.app.model;

import org.telescope.javel.framework.model.Model;

public class Driver extends Model {

    public Driver() {
        ignored = "password,salt,token";
        timestamps = true;
    }
}
