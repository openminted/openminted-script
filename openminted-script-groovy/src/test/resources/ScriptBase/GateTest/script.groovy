catalog "DKPro":'classpath:CatalogBase/engines.json'
catalog "GATE":'classpath:CatalogBase/engines_gate.json'
catalog "DKPro":'classpath:CatalogBase/formats.json'
catalog "GATE":'classpath:CatalogBase/formats_gate.json'

read 'String' language 'en' params([
  documentText: 'This is the first test in gate integration to DSL.'])

apply 'BreakIteratorSegmenter'

apply 'POSTagger'

write 'GateXMLExporter' to "${testOutputPath}/output.txt" params([
    overwrite: true])