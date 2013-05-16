<%@ page import="cinnamon.relation.RelationResolver" %>
<table>
    <tbody>

    <tr class="prop">
        <td valign="top" class="name">
            <label for="name"><g:message code="relationType.name"/></label>
        </td>
        <td valign="top" class="value ${hasErrors(bean: relationType, field: 'name', 'errors')}">
            <input type="text" name="name" id="name"
                   value="${fieldValue(bean: relationType, field: 'name')}"/>
        </td>
    </tr>

    <tr class="prop">
        <td valign="top" class="name">
            <label for="leftobjectprotected"><g:message code="relationType.leftobjectprotected"/></label>
        </td>
        <td valign="top"
            class="value ${hasErrors(bean: relationType, field: 'leftobjectprotected', 'errors')}">
            <g:checkBox name="leftobjectprotected"
                        value="${relationType?.leftobjectprotected}"/>
        </td>
    </tr>

    <tr class="prop">
        <td valign="top" class="name">
            <label for="rightobjectprotected"><g:message code="relationType.rightobjectprotected"/></label>
        </td>
        <td valign="top"
            class="value ${hasErrors(bean: relationType, field: 'rightobjectprotected', 'errors')}">
            <g:checkBox name="rightobjectprotected"
                        value="${relationType?.rightobjectprotected}"/>
        </td>
    </tr>

    <tr class="prop">
        <td valign="top" class="name">
            <label for="cloneOnLeftCopy"><g:message code="relationType.cloneOnLeftCopy"/></label>
        </td>
        <td valign="top"
            class="value ${hasErrors(bean: relationType, field: 'cloneOnLeftCopy', 'errors')}">
            <g:checkBox name="cloneOnLeftCopy"
                        value="${relationType?.cloneOnLeftCopy}"/>
        </td>
    </tr>
    <tr class="prop">
        <td valign="top" class="name">
            <label for="cloneOnRightCopy"><g:message code="relationType.cloneOnRightCopy"/></label>
        </td>
        <td valign="top"
            class="value ${hasErrors(bean: relationType, field: 'cloneOnRightCopy', 'errors')}">
            <g:checkBox name="cloneOnRightCopy"
                        value="${relationType?.cloneOnRightCopy}"/>
        </td>
    </tr>

    <tr class="prop">
        <td valign="top" class="name">
            <label for="cloneOnLeftVersion"><g:message code="relationType.cloneOnLeftVersion"/></label>
        </td>
        <td valign="top"
            class="value ${hasErrors(bean: relationType, field: 'cloneOnLeftVersion', 'errors')}">
            <g:checkBox name="cloneOnLeftVersion" value="${relationType?.cloneOnLeftVersion}"/>
        </td>
    </tr>
    <tr class="prop">
        <td valign="top" class="name">
            <label for="cloneOnRightVersion"><g:message code="relationType.cloneOnRightVersion"/></label>
        </td>
        <td valign="top"
            class="value ${hasErrors(bean: relationType, field: 'cloneOnRightCopy', 'errors')}">
            <g:checkBox name="cloneOnRightVersion" value="${relationType?.cloneOnRightVersion}"/>
        </td>
    </tr>

    <tr class="prop">
        <td valign="top" class="name">
            <label for="left_resolver_id"><g:message code="relationType.leftResolver"/></label>
        </td>
        <td valign="top" class="value ${hasErrors(bean: relationType, field: 'leftResolver', 'errors')}">
            <g:select name="left_resolver_id" from="${RelationResolver.list()}"
                      value="${relationType?.leftResolver?.id}"
                      optionKey="id"
                      optionValue="name"/>
        </td>
    </tr>
    <tr class="prop">
        <td valign="top" class="name">
            <label for="right_resolver_id"><g:message code="relationType.rightResolver"/></label>
        </td>
        <td valign="top" class="value ${hasErrors(bean: relationType, field: 'rightResolver', 'errors')}">
            <g:select name="right_resolver_id" from="${RelationResolver.list()}"
                      value="${relationType?.rightResolver?.id}"
                      optionKey="id"
                      optionValue="name"/>
        </td>
    </tr>
    </tbody>
</table>