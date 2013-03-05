<%@ page import="cinnamon.ObjectType" %>
<table>
    <thead>
    <tr>

        <g:sortableColumn property="id" title="${message(code: 'id')}"/>

        <g:sortableColumn property="name" title="${message(code: 'objectType.name')}"/>

        <th>
        <g:message code="objectType.config"/>
        </th>

    </tr>
    </thead>
    <tbody>
    <g:each in="${objectTypeList}" status="i" var="objectTypeInstance">
        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

            <td><g:link action="show"
                        id="${objectTypeInstance.id}">${fieldValue(bean: objectTypeInstance, field: 'id')}</g:link></td>

            <td>${fieldValue(bean: objectTypeInstance, field: 'name')}</td>

            <td>
                <label for="config_${objectTypeInstance?.id}" style="display:none;">
                    <g:message code="objectType.config" default="Config"/>
                </label>

                <div class="value xml_editor">
                    <g:form>

                        <textarea id="config_${objectTypeInstance?.id}" style="width:100ex;border:1px black solid; "
                                  name="config" cols="120" disabled="disabled"
                                  rows="10">${objectTypeInstance.config ?: '<meta />'}</textarea>
                        <script type="text/javascript">
                            var renderMirror = CodeMirror.fromTextArea($('#config_${objectTypeInstance?.id}').get(0), {
                                mode: 'application/xml',
                                readOnly: true
                            });
                        </script>
                    </g:form>
                </div>
            </td>

        </tr>
    </g:each>
    </tbody>
</table>

<div class="paginateButtons">
    <util:remotePaginate controller="objectType" action="updateList" total="${ObjectType.count()}"
                         update="objectTypeList" max="10" pageSizes="[10, 20, 50, 100, 250, 500, 1000]"/>
</div>