package cinnamon

import org.dom4j.Element
import org.dom4j.DocumentHelper

class FolderTypeController extends BaseController{

    def listXml() {
        def doc = DocumentHelper.createDocument()
        Element root = doc.addElement("folderTypes");
        FolderType.list().each{folderType ->
            root.add(FolderType.asElement("folderType", folderType));
        }
        return render(contentType: 'application/xml', text: doc.asXML())
    }

    def create() {
//        FolderType ft = new FolderType(params)
//        return [folderType:null]
    }

    def index() {
        redirect(action: 'list')
    }

    def save() {

        FolderType folderType = new FolderType(
                params.name,
                params.description,
        )

        log.debug("folderType: ${folderType}")

        try {
            folderType.save(failOnError: true)
        }
        catch (Exception e) {
            log.debug("failed to save FolderType:", e)
            flash.message = e.getLocalizedMessage().encodeAsHTML()
            return redirect(action: 'create', controller: 'folderType', params: params) //, params:[folderType:folderType])
        }

        return redirect(action: 'show', params: [id: folderType?.id])
    }

    def list() {
        setListParams()
        [folderTypeList: FolderType.list(params)]
    }

    def show () {
        if (params.id) {
            FolderType ft = FolderType.get(params.id)
            if (ft != null) {
                return [folderType: ft]
            }
        }
        flash.message = message(code: 'error.access.failed')
        return redirect(action: 'list', controller: 'folderType')
    }

    def edit () {
        [folderType: FolderType.get(params.id)]
    }

    def delete () {
        FolderType ft = FolderType.get(params.id)
        try {
            ft.delete(flush: true)
            flash.message = message(code: 'folderType.delete.success', args: [ft.name.encodeAsHTML()])
        }
        catch (Exception e) {
            log.debug("failed to delete FolderType:", e)
            flash.message = message(code: 'folderType.delete.fail', args: [e.getLocalizedMessage()?.encodeAsHTML()])
        }
        return redirect(action: 'list')
    }

    def update () {
        FolderType ft = FolderType.get(params.id)
        ft.properties = params
        try {
            ft.save(flush: true)
        }
        catch (Exception e) {
            log.debug("failed to update FolderType:",e)
            flash.message = e.getLocalizedMessage()
            return redirect(action: 'edit', params: [id: params.id])
        }
        return redirect(action: 'show', params: [id: params.id])
    }

    def updateList () {
        setListParams()
        render(template: 'folderTypeList', model: [folderTypeList: FolderType.list(params)])
    }
    
}
