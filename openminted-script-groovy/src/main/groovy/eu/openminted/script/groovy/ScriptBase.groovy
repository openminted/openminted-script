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
package eu.openminted.script.groovy;

import eu.openminted.script.groovy.internal.PipelineContext
import eu.openminted.script.groovy.internal.dsl.EngineHelper;
import eu.openminted.script.groovy.internal.dsl.PipelineHelper;
import eu.openminted.script.groovy.internal.dsl.WriterHelper
import eu.openminted.script.groovy.internal.ComponentInstance
import eu.openminted.script.groovy.internal.ComponentRole;
import eu.openminted.script.groovy.internal.Document;
import eu.openminted.script.groovy.internal.Helper;

import static org.apache.uima.fit.factory.TypeSystemDescriptionFactory.*;
import static org.apache.uima.fit.pipeline.SimplePipeline.*;

import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;

abstract class ScriptBase extends DelegatingScript {
    abstract void scriptBody()

    def run() {
        // Avoid an NPE while calling context.findClassLoader() which calls back into this class
        setDelegate(this);

        PipelineContext context = new PipelineContext();
        context.scriptContext = this;
        context.classLoader = this.class.classLoader;
        
        // We set the thread context classloader such that UIMA/uimaFIT has access to all the 
        // classes defined in the script and loaded via grab.
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(context.findClassLoader());
        try {
            context.boot();
            
            // Now set the true DSL delegate
            PipelineHelper pipelineHelper = new PipelineHelper(context);
            setDelegate(pipelineHelper);
            
            scriptBody();
            
            if (!context.pipeline.empty) {
                assert context.pipeline.get(0).role == ComponentRole.READER;
                
                List<ComponentInstance> componentInstances = [];
                
                // Create/initialized component instances
                context.pipeline.each { component ->
                    componentInstances.add(context.frameworks[component.framework].create(component));
                } 
                
                // Create document
                Document doc = new Document();
                
                // First document
                componentInstances[0].process(doc);
                while (doc.data != null) {
                    // Process current document
                    componentInstances[1..-1].each {
                        it.process(doc);
                    }
                    
                    // Next document
                    componentInstances[0].process(doc);
                }
                
                // Destroy instances
                componentInstances.each {
                    it.destroy();
                }
            }
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
    }
}