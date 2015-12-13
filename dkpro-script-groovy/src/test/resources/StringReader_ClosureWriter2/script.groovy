version "1.8.0-SNAPSHOT"

read 'String' language 'en' params([
  documentText: 'This is a test.'])

apply 'OpenNlpSegmenter'

apply 'OpenNlpPosTagger'

write {
    select type('Token') each { println "${it.coveredText} ${it.pos.posValue}" }
}
