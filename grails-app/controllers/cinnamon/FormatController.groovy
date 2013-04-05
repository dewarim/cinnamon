package cinnamon

import org.dom4j.Element
import org.dom4j.DocumentHelper
import grails.plugins.springsecurity.Secured

@Secured(["isAuthenticated()"])
class FormatController extends BaseController {

    @Secured(["hasRole('_superusers')"])
    def index () {
        redirect(action:'list', controller:'format')
    }

    @Secured(["hasRole('_superusers')"])
    def list () {
        setListParams()
        return [formatList:Format.list(params)]
    }

    @Secured(["hasRole('_superusers')"])
    def create() {
        
    }

    @Secured(["hasRole('_superusers')"])
    def edit () {
        Format format = Format.get(params.id)
        if(format){
            return [format:format]
        }
        flash.message = message(code:'error.access.failed')
        return redirect(action:'list', controller:'format')
    }

    @Secured(["hasRole('_superusers')"])
    def show () {
        if(params.id){
            Format format = Format.get(params.id)
            if(format != null){
                return [format: format]
            }
        }
        flash.message = message(code:'error.access.failed')
        return redirect(action:'list', controller:'format')
    }

    @Secured(["hasRole('_superusers')"])
    def delete () {
        def format = Format.get(params.id)
        if( ! format){
            flash.message = message(code:'error.access.failed')
            return redirect(action:'list', controller:'format')
        }

        def osdCount = ObjectSystemData.countByFormat(format)
        if (osdCount == 0) {
            flash.message = message(code:'format.delete.success', args:[format.name])
            format.delete()
        }
        else {
            flash.message = message(code: 'format.still.used', args: [format.name])
        }
        return redirect(action: 'list')
    }

    @Secured(["hasRole('_superusers')"])
    def save () {
        if (!params.name || !params.name.trim()) {
            flash.error = message(code: 'format.empty.name')
            return redirect(action:'create', controller:'format', params:params)
        }
        else{
            Format format = new Format()
            // TODO: use bind params
            format.properties = params
            if(! format.save()){
                return redirect(action:'create', controller:'format',  params:params)
            }
            else{

                return redirect(action:'show', controller:'format', id:format.id)
            }
        }
    }

    @Secured(["hasRole('_superusers')"])
    def update () {
        Format format = Format.get(params.id)
        if (!params.name || !params.name.trim()) {
            flash.message = message(code: 'format.empty.name')
            return redirect(action:'edit', controller:'format', id:format.id)
        }
        // TODO: use bind params
        format.properties = params

        if(! format.save()){
            flash.message = message(code:'format.update.failed')
        }
        return redirect(action:'show', controller:'format', id:format.id)
    }

    @Secured(["hasRole('_superusers')"])
    def updateList () {
        setListParams()
        render(template: 'listTable', model:[formatList:Format.list(params)])
    }

    //---------------------------------------------------
    // Cinnamon XML Server API
    
    def listXml(){
        List<Format> formats = new ArrayList<Format>();
        if (params.id) {
            // format by id
            Format f = Format.get(params.id);
            // add format only if no name has been passed or id and name match
            if (f != null) {
                formats.add(f);
            }
        } else if (formats.name) {
            Format format = Format.findByName(params.name);
            if (format != null) {
                formats.add(format);
            }
        } else {
            log.debug("no id or name given");
            formats = Format.list();
        }

        log.debug("formats.size() = " + formats.size());
        def doc = DocumentHelper.createDocument()        
        Element root = doc.addElement("formats");
        formats.each{format ->
            root.add(Format.asElement("format", format));
        }
        render(contentType: 'application/xml', text: doc.asXML())
    }
           
}
