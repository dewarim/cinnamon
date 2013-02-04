package cinnamon

import grails.plugins.springsecurity.Secured

@Secured(["hasRole('_superusers')"])
class LifecycleLogController extends BaseController{


    def index() {
        try {
            setEntryListParams()
            Pagination pagination = fetchEntryListPagination()
            pagination.itemCount = LifecycleLog.countByRepository(session.repositoryName)
            return [
                    pagination: pagination,
                    selectedEventType: null,
                    logEntries: LifecycleLog.findAllByRepository(session.repositoryName, params)                    
            ]
        }
        catch (Exception e) {
            log.debug("failed to show log entries: ", e)
            flash.message = message(code: 'error.log.list', args: [message(code: e.message)])
        }
    }

    def updateEntryList () {
        try {
            setEntryListParams()
            Pagination pagination = fetchEntryListPagination()
//            log.debug("pagination: ${pagination.dump()}")
            def filter = "%${params.filter ? params.filter + '%' : ''}"
            log.debug("Filter: '${filter}'")
            def query = """from cinnamon.LifecycleLog as l where 
            l.repository =:repository and
            (
            lower(l.userName) like lower(:f1)
            or lower(l.lifecycleName) like lower(:f2) 
            or lower(l.oldStateName) like lower(:f3) 
            or lower(l.newStateName) like lower(:f4) 
            or lower(l.folderPath) like lower(:f5) 
            or lower(l.name) like lower(:f6) 
            ) """.replaceAll('\n', ' ')
//            log.debug("query: $query order by e.id")
            def filterParams = [
                    repository:session.repositoryName,
                    f1: filter,
                    f2: filter, f3: filter, f4: filter, f5: filter, f6: filter
            ]


            def logEntries = LifecycleLog.findAll(query + " order by l.id", filterParams, params)
            pagination.filter = params.filter.encodeAsHTML()
            pagination.itemCount = (int) LifecycleLog.executeQuery('select count(*) ' + query, filterParams,)[0]
            log.debug("itemCount in update:${pagination.itemCount}")
            return render(template: 'logTable', model: [
                    pagination: pagination,
                    logEntries: logEntries,
            ])
        }
        catch (Exception e) {
            log.debug("failed to update entry list:", e)
            return render(status: 500, text: message(code: 'error.log.list', args: [message(code: e.message)]))
        }
    }

    protected void setEntryListParams() {
        params.offset = params.offset ? inputValidationService.checkAndEncodeInteger(params, "offset", "offset") : 0
        params.max = params.max ? inputValidationService.checkAndEncodeInteger(params, 'max', 'paginate.max') : 20
    }

    protected Pagination fetchEntryListPagination() {
        def pag = new Pagination(params)
//        pag.itemCount = (int) LifecycleLog.executeQuery("select count(l) from LifecycleLog l", [:])[0]
        pag.itemCount = LifecycleLog.count()
        log.debug("itemCount: ${pag.itemCount}")
        pag.updateId = 'logTable'
        pag.remoteAction = 'updateEntryList'
        return pag
    }
    
}
