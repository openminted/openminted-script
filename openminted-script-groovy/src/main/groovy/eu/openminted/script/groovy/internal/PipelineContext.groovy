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
package eu.openminted.script.groovy.internal

import static org.apache.uima.fit.factory.AnalysisEngineFactory.*
import static org.apache.uima.fit.factory.CollectionReaderFactory.*
import eu.openminted.script.groovy.internal.gate.GateLoader
import eu.openminted.script.groovy.internal.uima.UimaLoader
import groovy.json.*

class PipelineContext
{	
	List<Component> pipeline = [];

    List<ComponentOffer> registry = [];
    
    Map<String, Loader> frameworks = [
        uima: new UimaLoader(this),
        gate: new GateLoader(this)];

	Script scriptContext;

	ClassLoader classLoader;

	def boot() {
		java.util.logging.LogManager.logManager.reset(); // Disable logging
		/*
		 java.util.logging.LogManager.logManager.getLogger("").level = java.util.logging.Level.FINEST;
		 java.util.logging.LogManager.logManager.getLogger("").handlers.each {
		 it.level = java.util.logging.Level.FINEST;
		 }
		 */

//		def engines = new JsonSlurper().parse(new File("src/main/resources/PipelineContextJSON/engines.json"));
		new File("src/main/resources/PipelineContextJSON").listFiles().each{ file->
			if(file.getName().startsWith("engines")){
				def engines = new JsonSlurper().parse(file);				
				engines.each { k, v ->
					ComponentOffer offer = new ComponentOffer();
					def framework = v.groupId.contains("dkpro")?"uima":"gate";
					offer.groupId = v.groupId;
					offer.artifactId = v.artifactId;
					offer.version = v.version;
					offer.name = k;
					offer.implName = v["class"];					
					offer.framework = framework;
					offer.role = ComponentRole.PROCESSOR;
					registry.add(offer);
				}
			}
			if(file.getName().startsWith("formats")){
				def formats  = new JsonSlurper().parse(file);
				formats.each { k, v ->
					if (v.readerClass) {
						ComponentOffer offer = new ComponentOffer();
						def framework = v.groupId.contains("dkpro")?"uima":"gate";						
						offer.groupId = v.groupId;
						offer.artifactId = v.artifactId;
						offer.version = v.version;
						offer.name = k;
						offer.implName = v["readerClass"];
						offer.framework = framework;
						offer.role = ComponentRole.READER;
						registry.add(offer);
					}
		
					if (v.writerClass) {
						ComponentOffer offer = new ComponentOffer();
						def framework = v.groupId.contains("dkpro")?"uima":"gate";
						offer.groupId = v.groupId;
						offer.artifactId = v.artifactId;
						offer.version = v.version;
						offer.name = k;
						offer.implName = v["writerClass"];
						offer.framework = framework;
						offer.role = ComponentRole.WRITER;
						registry.add(offer);
					}
				}
			}
		}
//		def temp = new File("src/main/resources/PipelineContextJSON/temp");
//		registry.each {
//			temp << it.artifactId +"--"+ it.framework +"\n"
//		}
      
        
	}

	def load(component, ComponentRole role) {
		def cl = findClassLoader();
		def impl;

        Component comp;
        
        if (component instanceof String || format instanceof GString) {
            // Look up component in registry
            ComponentOffer offer = registry.find { it ->
                it.role == role && it.name == component;
            }
            
            if (!offer) {
                throw new IllegalArgumentException("Unable to find $component as $role");
            }
            
            Loader loader = frameworks[offer.framework];
            comp = loader.load(offer);
        }
        
		return comp;
	}

	def findClassLoader() {
		def cl;
		if (classLoader) {
			cl = classLoader;
		}
		else if (scriptContext) {
			// When setting the parentClassLoader property, then UIMA no longer has access to the
			// classes defined in the script, e.g. to closures!
			if (scriptContext.binding.variables.get("parentClassLoader")) {
				cl = scriptContext.binding.variables.get("parentClassLoader");
			}
			else {
				cl = scriptContext.metaClass.theClass.classLoader;
			}
		}
		else {
			cl = this.class.classLoader;
		}
		return cl;
	}
}
