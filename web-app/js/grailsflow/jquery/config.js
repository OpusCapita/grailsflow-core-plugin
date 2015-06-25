/**
 * Converts java Date string to JQuery date string
 *
 * @param javaDatePattern
 */
function convertDatePatternFromJavaToJqueryDatePicker(javaDatePattern) {
    if (!javaDatePattern) { return null }
    var quotedLiteral = /'[^']*'/g;
    var fakeLowY = '\u2500';
    var fakeBigM = '\u2502';

    var java2JqMap = [
             { regExp : /D{2,}/g, replacement : 'oo' },
             { regExp : /D/g, replacement : 'o' },
             { regExp : /M{4,}/g, replacement : fakeBigM + fakeBigM},
             { regExp : /M{3,}/g, replacement : fakeBigM},
             { regExp : /M{2,}/g, replacement : 'mm'},
             { regExp : /M/g, replacement : 'm'},
             { regExp : /F{4,}/g, replacement : 'DD'},
             { regExp : /F{1,3}/g, replacement : 'D'},
             { regExp : /d{3,}/g, replacement : 'dd' }
         ];

         var parts = [];
         var lastUnquotedStartIndex = 0;
         var lastUnquotedEndIndex = 0;
         var foundPart = quotedLiteral.exec(javaDatePattern);

         while (foundPart != null) {
             lastUnquotedEndIndex = foundPart.index;
             if (lastUnquotedStartIndex < lastUnquotedEndIndex) {
                 parts.push(javaDatePattern.substring(lastUnquotedStartIndex, lastUnquotedEndIndex));
             }

             parts.push(foundPart[0]);
             lastUnquotedStartIndex = quotedLiteral.lastIndex;

             foundPart = quotedLiteral.exec(javaDatePattern);
         }

         lastUnquotedEndIndex = javaDatePattern.length;
         if (lastUnquotedStartIndex < lastUnquotedEndIndex) {
             parts.push(javaDatePattern.substring(lastUnquotedStartIndex, lastUnquotedEndIndex));
         }

         for (var index = 0; index < parts.length; index++) {
             if (parts[index].indexOf("'") == -1) {
                 for (var ind = 0; ind < java2JqMap.length; ind++) {
                     parts[index] = parts[index].replace(java2JqMap[ind].regExp, java2JqMap[ind].replacement);
                 }
             }

         }

         return parts.join('').replace(new RegExp(fakeLowY, 'g'), 'y').replace(new RegExp(fakeBigM, 'g'), 'M');
     }
