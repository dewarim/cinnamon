<%@ page import="cinnamon.i18n.Message;" %><?xml version="1.0" encoding="UTF-8"?>
<xliff version='1.2'
       xmlns='urn:oasis:names:tc:xliff:document:1.2 xliff-core-1.2-strict.xsd'>
    <file original='cinnamonUiMessages.xml' source-language='${language.isoCode}'
          target-language='${targetLanguage.isoCode}'
          datatype='xml'>
        <g:if test="${false}">
            // currently, this part is unused.
            <header>
                <skl>
                    <internal-file><![CDATA[<g:render template="exportMessages"
                     model="[language:language, messages:messages, outputService:outputService]"/>]]></internal-file>
                </skl>
            </header>
        </g:if>
        <body>
        <g:each in="${messages}" var="msg">
            <trans-unit id="${msg.id}" resname="${msg.message}">
                <source>${outputService.replaceXmlEntities(msg.translation)}</source>
                <target>${outputService.replaceXmlEntities(Message.findByLanguageAndMessage(targetLanguage, msg.message)?.translation) ?: outputService.replaceXmlEntities(msg.translation)}</target>
            </trans-unit>
        </g:each>
        </body>
    </file>
</xliff>