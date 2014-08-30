package cinnamon

import grails.plugin.springsecurity.annotation.Secured

@Secured(["hasRole('_superusers')"])
class AdminController extends BaseController{

    def index() { }
    
}
