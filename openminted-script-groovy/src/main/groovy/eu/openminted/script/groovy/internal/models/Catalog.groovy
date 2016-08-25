package eu.openminted.script.groovy.internal.models

class Catalog
{    
    String framework;    
    Map catalog = new HashMap();
 
    def of(framework)   {
        this.framework  = framework;
    }
}
