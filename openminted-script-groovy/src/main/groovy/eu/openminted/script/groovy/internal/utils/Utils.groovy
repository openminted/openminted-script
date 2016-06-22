package eu.openminted.script.groovy.internal.utils

import eu.openminted.script.groovy.internal.ComponentInstance
import eu.openminted.script.groovy.internal.ConvertToGate;
import eu.openminted.script.groovy.internal.Document
import eu.openminted.script.groovy.internal.gate.GateComponentInstance;
import eu.openminted.script.groovy.internal.uima.UimaComponentInstance;;

class Utils {

	static def fromFramework;
	static def toFramework;
	static def frameworks = ['uima':UimaComponentInstance.class,'gate':GateComponentInstance.class];
	static def convertionMethod = ['uima-gate':ConvertToGate.class,'gate-uima':ConvertToGate.class];
	
	static Document convertDocumentFramework(Document doc,def previousInstance, def currentInstance){
		if(previousInstance.getClass().equals(currentInstance.getClass())){
			return doc
		}else{
				fromFramework = findFramework(previousInstance);
				toFramework = findFramework(currentInstance);			
				def className = convertionMethod[fromFramework+'-'+ toFramework];				
				Object convertionObj = className.newInstance();
				doc.data = convertionObj.convert(doc.data)
				return doc;
		}
	}
	static def findFramework(def instance){
		def returnVal;
		frameworks.each {key,val ->
			if(instance.getClass()==val){
				returnVal = key;
			}
		}		
		if(!returnVal){
			System.err.printf("Not able to find framework for %s",instance.getClass());
		}	
		return returnVal!=null?returnVal:null;
	}
}
