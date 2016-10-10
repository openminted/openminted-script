// catalog "DKPro":'https://gist.githubusercontent.com/reckart/990d75ee230dbb39c30b/raw/ad29ba37ebb77e3f5f9f47fb32c3def15c63954c/engines.json'
catalog "DKPro":'classpath:CatalogBase/engines.json'
catalog "GATE":'classpath:CatalogBase/engines_gate.json'
catalog "DKPro":'classpath:CatalogBase/formats.json'
catalog "GATE":'classpath:CatalogBase/formats_gate.json'

read "DKPro":'String' language 'en' params([
  documentText: 'This is the first test in gate integration to DSL.'])

apply 'BreakIteratorSegmenter'

apply "GATE":'POSTagger'

write "GATE":'GateXMLExporter' to "${testOutputPath}/output.txt" params([
    overwrite: true])
