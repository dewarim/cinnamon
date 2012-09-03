package cinnamon

import java.math.RoundingMode

/**
 */
class Pagination {

    Integer max
    Integer itemCount
    Integer offset
    String remoteAction
    String remoteController = 'lifecycleLog'
    String aliasAction
    String updateId
    String filter

    Pagination(Map params){
        max = params.max
        offset = params.offset
        filter = params.filter
        remoteAction = params.remoteAction?.encodeAsHTML()
        aliasAction = params.aliasAction?.encodeAsHTML()
        updateId = params.updateId?.encodeAsHTML()
    }

    Integer getPageCount(){
        return new BigDecimal(itemCount).divide(new BigDecimal(max), 0, RoundingMode.UP)
    }

    void setFilter(String filter){
        this.filter = filter.replaceAll('%', '')
    }
}
