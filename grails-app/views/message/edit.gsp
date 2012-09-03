<!DOCTYPE HTML>
<html>
<head>
	<meta name="layout" content="main"/>

        <title><g:message code="message.edit.title"/></title>
    </head>
    <body>
    
    
        <div class="nav">
			<g:homeButton><g:message code="home"/></g:homeButton>
            <span class="menuButton"><g:link class="list" action="list"><g:message code="message.list"/></g:link></span>
            <span class="menuButton"><g:link class="create" action="create"><g:message code="message.create"/></g:link></span>
        </div>
        <div class="content">
            <h1><g:message code="message.edit.title"/></h1>
			<g:render template="/shared/message"/>
			<g:render template="/shared/errors" bean="${message}"/>
			<div id="error"></div>
         
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="messageId"><g:message code="message.message"/></label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:message,field:'message','errors')}">
                                   <g:form method="post" action="update" controller="message">
                                    <input type="hidden" name="oldMessageId" id="oldMessageId" value="${messageId}"/>
                                    <input type="text" name="messageId" id="messageId" value="${messageId}" />
                      	            <div class="buttons">
                    				<span class="button">
                    					<input type="submit" name="save" value="${message(code:'save.changes')}"/>
				                    </span>
				                    </g:form>
                					</div>
                                </td>
                            </tr> 
     				</tbody>
     			</table>
     		    <table>
                	<thead>
                		<tr>
                			<th><g:message code="message.language"/></th>
                			<th><g:message code="message.translation"/></th>
                		</tr>
                	</thead>
                    <tbody>
                        <g:each in="${languages}" var="language">
                             <tr>
                             	<td valign="top" class="name"><g:message code="lang.${language.isoCode}" default="${language.isoCode}"/></td>
	                             	
                                <td valign="top" class="value">
                                	<g:set var="lang_div_id" value="language_${language.id}"/>
                                	<g:set var="lang_submit_id" value="form_${language.id}"/>
                                	<div id="${lang_div_id}"></div>
                                	<g:formRemote name="${language}" url="[controller:'message', action:'updateTranslationAjax']" onLoading="document.getElementById('${lang_div_id}').style.display='block';" onComplete="getElementById('${lang_submit_id}').style.display = 'none';return true;" update="[success:lang_div_id, failure:'error']">
                                		<input type="hidden" name="language" value="${language.id}"/>
                                		<input type="hidden" name="messageId" value="${messageId}"/>
                                    	<input type="text" onKeypress="document.getElementById('${lang_submit_id}').style.display='block'; document.getElementById('${lang_div_id}').style.display='none';"
                                    	 name="translation" id="translation_${language}" value="${translations[language.isoCode]?.encodeAsHTML()}" />
                                    	<input type="submit" style="display:none;" name="save" id="${lang_submit_id}" value="${message(code:'update.translation')}"/>
                                    </g:formRemote>
                                </td>                                
                            </tr>
                        </g:each>                         
	                        
                    </tbody>
                    </table>
                </div>
        </div>
</body></html>
