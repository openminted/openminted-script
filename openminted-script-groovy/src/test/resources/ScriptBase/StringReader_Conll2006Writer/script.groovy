version "1.9.0-SNAPSHOT"

read 'String' language 'en' params([
  documentText: 'This is a test.'])

apply 'OpenNlpSegmenter'

apply 'OpenNlpPosTagger'

write 'Conll2006' to testOutputPath params([
    overwrite: true])