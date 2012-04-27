<g:if test="${osd.locked_by}">
    <g:if test="${osd.locked_by.equals(user) || superuserStatus}">
        <span class="unlockLink" title="${message(code: 'osd.locked.by.you')}">
            <g:remoteLink controller="osd" action="unlockOsd" params="[osd:osd.id]"
                          update="[success:'folderContent', failure:'message']" asynchronous="false">
                <r:img uri="/images/icons/document_locked_self.png" plugin="humulus"
                     width="16" height="16" border="0"
                     alt="${message(code: 'osd.locked.by.you')}"/>  ${osd.locked_by.name}
            </g:remoteLink>
        </span>
    </g:if>
    <g:else>
        <r:img uri="/images/icons/document_locked_other.png" plugin="humulus" border="0"
             width="16" height="16" title="${message(code: 'osd.locked.by.user', args: [osd.locked_by.name])}"
             alt="${message(code: 'osd.locked.by.user')}"/>  ${osd.locked_by.name}
    </g:else>
</g:if>
<g:else>
    <span class="lockLink" title="${message(code: 'osd.locked.not')}">
        <g:remoteLink controller="osd" action="lockOsd" params="[osd:osd.id]"
                      update="[success:'folderContent', failure:'message']" asynchronous="false">
            <r:img uri="/images/icons/document_unlocked.png" plugin='humulus' border="0"
                 width="16" height="16"
                 alt="${message(code: 'osd.locked.not')}"/>
        </g:remoteLink>
    </span>
</g:else>
