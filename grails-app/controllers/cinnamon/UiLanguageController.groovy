package cinnamon

import grails.plugins.springsecurity.Secured
import cinnamon.i18n.UiLanguage
import org.dom4j.Document
import org.dom4j.DocumentHelper
import org.dom4j.Element

@Secured(["hasRole('_superusers')"])
class UiLanguageController extends BaseController{
    
	def create () {
		
	}

    def list () {
         setListParams()
    	[uiLanguageList : UiLanguage.list(params)]
    }
    
    def show () {
    	[language : UiLanguage.get(params.id)]
    }

    def edit () {
    	[language: UiLanguage.get(params.id)]
    }
    
    def update () {
		log.debug("called update with "+params.dump())
    	UiLanguage lang = UiLanguage.get(params.id)
    
		if(params.isoCode?.length() == 0){
			// do not allow empty name.			
			params.isoCode = lang.isoCode
		}
	
		if(UiLanguage.findByIsoCode(params.isoCode)){
			flash.message = message(code:"error.object.already.exits", args:["(?)"])
    		return redirect(action: 'edit', params:[id:params.id])
		}
		
		if(updateUiLanguage(lang, params)){
				flash.message = message(code:"language.update.success", args:[lang.isoCode.encodeAsHTML()])
				return redirect(action: 'show', params:[id:lang.id])
		}
		else{
			flash.message = message(code:"language.update.fail", args:[lang.errors.encodeAsHTML()])
    		return redirect(action: 'edit', params:[id:params.id])
		}
    }
    
	protected Boolean updateUiLanguage(lang,params){
		try{
    		lang.properties = params
    		lang.save(flush:true)
    	}
    	catch (Exception e) {
    		return false
		}
       	return true
	}
	
    def delete () {
		UiLanguage lang = UiLanguage.get(params.id)
		String isoCode = lang.isoCode
		try{
			lang.delete()
		}
		catch(Exception e){
			flash.message = e.getLocalizedMessage()
			return redirect(action: 'list')
		}
		
		flash.message = message(code: 'language.delete.success', args:[isoCode.encodeAsHTML()]) // TODO: add name
		return redirect(action: 'list')
	}

    
    /**
     * Called after the 'save' button in create.gsp is called
     */
    def save () {
    	def language = new UiLanguage(params)
    	try{
    		language.save(failOnError:true, flush : true)
    	}
    	catch(Exception e){
    		flash.message = e.getLocalizedMessage().encodeAsHTML()
    		return redirect(action: 'create')
    	}

    	return redirect(action : 'show', params : [id : language.id])
    }

    def updateList () {
        setListParams()
        render(template: 'uiLanguageList', model:[uiLanguageList:UiLanguage.list(params)])
    }


    //---------------------------------------------------
    // Cinnamon XML Server API
    def listUiLanguages() {
        Document doc = DocumentHelper.createDocument()
        Element root = doc.addElement("languages");
        UiLanguage.list().each {language->
            language.toXmlElement(root)
        }
        return render(contentType: 'application/xml', text: doc.asXML())
    }
}
