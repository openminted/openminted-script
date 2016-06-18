read 'String' language 'en' params([
  documentText: 'This is the first test in gate integration to DSL.'])

apply 'BreakIteratorSegmenter'

apply 'POSTagger'
write 'Text' 