read 'String' language 'en' params([
  documentText: 'This is a test.'])

write {
    println jcas.documentText
}