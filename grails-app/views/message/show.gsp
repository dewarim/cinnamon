<!DOCTYPE HTML>
<html>
<head>
	<meta name="layout" content="main"/>

        <title><g:message code="message.show.title"  args="[messageId]"/></title>
    </head>
    <body>
   
  
        <div class="nav">
			<g:homeButton><g:message code="home"/></g:homeButton>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="message.list"/></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="message.create"/></g:link></span>
        </div>
        <div class="content">
            <h1><g:message code="message.show.title" args="[messageId.encodeAsHTML()]"/></h1>
            
			<g:render template="/shared/message"/>

            <div class="dialog">
                <table>
                	<thead>
                		<tr>
                			<th><g:message code="message.language"/></th>
                			<th><g:message code="message.translation"/></th>
                		</tr>
                	</thead>
                    <tbody>
					                                           
                    <g:each in="${languages}" var="language">                        
	                       <tr class="prop">
        					<td valign="top" class="name">${language.isoCode.encodeAsHTML()}</td>                
                       	 	<td valign="top" class="value">${translations[language.isoCode]?.encodeAsHTML() ?:'---'}</td>
    	                   </tr>                        
                    </g:each>
           
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${msg.id}"
                    <span class="button"><g:actionSubmit class="edit" value="${message(code:'edit')}" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="${message(code:'delete')}" /></span>
                </g:form>
            </div>
        </div>


</body></html>
