// ****************************************************************************
// Copyright 2015
// Ubiquitous Knowledge Processing (UKP) Lab
// Technische Universität Darmstadt
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

import eu.openminted.script.groovy.internal.EngineHelper;
import eu.openminted.script.groovy.internal.PipelineContext;
import eu.openminted.script.groovy.internal.PipelineHelper;
import eu.openminted.script.groovy.internal.WriterHelper;
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
                // Force re-scan of type systems because we dynamically add JARs to the
                // classpath using grape - failure to do so will cause some types not to
                // be detected when the pipeline is actually run
                forceTypeDescriptorsScan();
                def ts = createTypeSystemDescription();
                
                // runpipeline constructs the type system from the descriptors passed to
                // it - make sure at least one of the components actually has the full
                // type system
                context.pipeline[0].desc.collectionReaderMetaData.typeSystem = ts;
                runPipeline(
                    context.pipeline[0].desc as CollectionReaderDescription, 
                    context.pipeline[1..-1].collect { it.desc } as AnalysisEngineDescription[]);
            }
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
    }
}