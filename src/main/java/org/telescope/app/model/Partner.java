package org.telescope.app.model;

import org.telescope.javel.framework.model.Model;

public class Partner extends Model {

    public Partner() {
        ignored = "password,salt,token";
        timestamps = true;
    }
}
