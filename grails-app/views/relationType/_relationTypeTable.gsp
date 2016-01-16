<%@ page import="cinnamon.relation.RelationResolver" %>
<table>
    <tbody>

    <tr class="prop">
        <td class="name"><g:message code="id"/></td>

        <td class="value">${fieldValue(bean: relationType, field: 'id')}</td>

    </tr>

    <tr class="prop">
        <td class="name"><g:message code="relationType.name"/></td>

        <td class="value">${fieldValue(bean: relationType, field: 'name')}</td>

    </tr>

    <tr class="prop">
        <td class="name"><g:message code="relationType.leftobjectprotected"/></td>

        <td class="value">
            <g:enabledDisabledIcon test="${relationType.leftobjectprotected}"/>
        </td>

    </tr>

    <tr class="prop">
        <td class="name"><g:message code="relationType.rightobjectprotected"/></td>

        <td class="value">
            <g:enabledDisabledIcon test="${relationType.rightobjectprotected}"/>
        </td>
    </tr>

    <tr class="prop">
        <td class="name"><g:message code="relationType.cloneOnLeftCopy"/></td>

        <td class="value">
            <g:enabledDisabledIcon test="${relationType.cloneOnLeftCopy}"/>
        </td>
    </tr>

    <tr class="prop">
        <td class="name"><g:message code="relationType.cloneOnRightCopy"/></td>

        <td class="value">
            <g:enabledDisabledIcon test="${relationType.cloneOnRightCopy}"/>
        </td>
    </tr>
    <tr class="prop">
        <td class="name"><g:message code="relationType.cloneOnLeftVersion"/></td>

        <td class="value">
            <g:enabledDisabledIcon test="${relationType.cloneOnLeftVersion}"/>
        </td>
    </tr>

    <tr class="prop">
        <td class="name"><g:message code="relationType.cloneOnRightVersion"/></td>

        <td class="value">
            <g:enabledDisabledIcon test="${relationType.cloneOnRightVersion}"/>
        </td>
    </tr>

    <tr class="prop">
        <td>
            <g:message code="relationType.leftResolver"/>
        </td>
        <td class="value">
            ${relationType.leftResolver.name}
        </td>
    </tr>
    <tr class="prop">
        <td>
            <g:message code="relationType.rightResolver"/>
        </td>
        <td class="value">
            ${relationType.rightResolver.name}
        </td>
    </tr>

    </tbody>
</table>