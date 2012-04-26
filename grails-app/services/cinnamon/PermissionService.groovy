package cinnamon

import cinnamon.exceptions.CinnamonException

class PermissionService {

    Permission fetch(String name) {
        Permission p = Permission.findByName(name)
        if(! p){
            throw new CinnamonException("error.permission.not_found", name);
        }
    }
}
