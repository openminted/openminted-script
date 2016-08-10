catalog "DKPro":'https://gist.githubusercontent.com/reckart/990d75ee230dbb39c30b/raw/ad29ba37ebb77e3f5f9f47fb32c3def15c63954c/engines.json'
catalog "GATE":'/PipelineContextJSON/engines_gate.json'
catalog "DKPro":'/PipelineContextJSON/formats.json'
catalog "GATE":'/PipelineContextJSON/formats_gate.json'

read "DKPro":'String' language 'en' params([
  documentText: 'This is the first test in gate integration to DSL.'])

apply 'BreakIteratorSegmenter'

apply "GATE":'POSTagger'

write "GATE":'GateXMLExporter' 