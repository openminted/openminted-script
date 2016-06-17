read 'String' language 'en' params([
  documentText: 'This is a test.'])

apply 'OpenNlpSegmenter'

apply 'OpenNlpPosTagger'

write 'Conll2006'