// ****************************************************************************
// See the NOTICE.txt file distributed with this work for additional information
// regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ****************************************************************************
package eu.openminted.script.groovy.internal.utils

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import eu.openminted.script.groovy.internal.ComponentInstance
import eu.openminted.script.groovy.internal.Document
import eu.openminted.script.groovy.internal.frameworkconversions.ConvertToGate
import eu.openminted.script.groovy.internal.frameworkconversions.ConvertToUIMA;
import eu.openminted.script.groovy.internal.gate.GateComponentInstance;
import eu.openminted.script.groovy.internal.uima.UimaComponentInstance;;

class Utils {

	static def fromFramework;
	static def toFramework;
	static def frameworks = ['uima':UimaComponentInstance.class,'gate':GateComponentInstance.class];
	static def convertionMethod = ['uima-gate':ConvertToGate.class,'gate-uima':ConvertToUIMA.class];

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
			throw new IllegalStateException("Unable to find framework for a component");
		}
		return returnVal!=null?returnVal:null;
	}

}
