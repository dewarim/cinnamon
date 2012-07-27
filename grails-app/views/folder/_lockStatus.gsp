<g:if test="${osd.locker}">
    <g:if test="${osd.locker.equals(user) || superuserStatus}">
        <span class="unlockLink" title="${message(code: 'osd.locked.by.you')}">
            <g:remoteLink controller="osd" action="unlockOsd" params="[osd:osd.id]"
                          update="[success:'osd_row_lock_'+osd.id, failure:'message']" asynchronous="false">
                <r:img uri="/images/icons/document_locked_self.png" width="16" height="16" border="0"
                     alt="${message(code: 'osd.locked.by.you')}"/>  ${osd.locker.name}
            </g:remoteLink>
        </span>
    </g:if>
    <g:else>
        <r:img uri="/images/icons/document_locked_other.png" border="0"
             width="16" height="16" title="${message(code: 'osd.locked.by.user', args: [osd.locker.name])}"
             alt="${message(code: 'osd.locked.by.user')}"/>  ${osd.locker.name}
    </g:else>
</g:if>
<g:else>
    <span class="lockLink" title="${message(code: 'osd.locked.not')}">
        <g:remoteLink controller="osd" action="lockOsd" params="[osd:osd.id]"
                      update="[success:'osd_row_lock_'+osd.id, failure:'message']" asynchronous="false">
            <r:img uri="/images/icons/document_unlocked.png" border="0"
                 width="16" height="16"
                 alt="${message(code: 'osd.locked.not')}"/>
        </g:remoteLink>
    </span>
</g:else>
