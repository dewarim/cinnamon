package cinnamon.test.pages.folder

import cinnamon.test.modules.AuthModule
import geb.Page

/**
 *
 * The default Cinnamon start page 
 *
 */
class IndexPage extends Page {

    static url = 'folder/index'

    static at = { $('a.home').text() == 'Start' }

    static content = {
        authMod { module AuthModule }
    }

}
