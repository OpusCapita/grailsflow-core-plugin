<!--
Template parameters:
  name   -- parameter name
  value  -- parameter value

-->

<g:select name='${name}' value="${value}"
   from="${processClasses}"
   optionKey="${{it.processType}}"
   optionValue="${{gf.translatedValue(translations: it.label, default: it.processType)}}"
   noSelection="['':'']"></g:select>
