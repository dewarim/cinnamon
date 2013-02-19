<label for="relationLeftOsd">
    <g:message code="relation.from"/>
</label>
<div id="relation-left-osd">
    <g:select id="relationLeftOsd" name="leftOsd" from="${osds}"
              optionKey="id"
              optionValue="${{it.id + ': '+it.name}}"
    />
</div>
<label for="relationRightOsd">
    <g:message code="relation.to"/>
</label>
<div id="relation-right-osd">

    <g:select id="relationRightOsd" name="rightOsd" from="${osds}"
              optionKey="id"
              optionValue="${{it.id + ': '+it.name}}"
    />
</div>