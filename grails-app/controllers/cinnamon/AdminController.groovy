package cinnamon

import grails.plugins.springsecurity.Secured

@Secured(["hasRole('_superusers')"])
class AdminController extends BaseController{

    def index() { }
    
}
