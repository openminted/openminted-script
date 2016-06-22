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
package eu.openminted.script.groovy.internal.uima

import static org.apache.uima.fit.factory.AnalysisEngineFactory.*
import static org.apache.uima.fit.factory.CollectionReaderFactory.*

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription
import org.apache.uima.cas.CAS
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.fit.factory.ConfigurationParameterFactory
import org.apache.uima.fit.factory.JCasFactory
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.jcas.JCas;

import eu.openminted.script.groovy.internal.Component
import eu.openminted.script.groovy.internal.ComponentInstance
import eu.openminted.script.groovy.internal.Document;

class UimaComponentInstance implements ComponentInstance {
    private def delegate;
    
    def init(Component decl) {
        decl.parameters.each { k, v ->
            ConfigurationParameterFactory.setParameter(decl.desc, k, v)
        }
        
        if (decl.desc instanceof AnalysisEngineDescription) {
            delegate = createEngine(decl.desc);
        }
        else {
            delegate = createReader(decl.desc);
        }
    }

    @Override
    def process(Document document)
    {
        CAS cas = null;
        
        if (document.data instanceof CAS) {
            cas = document.data;
        }
        else if (document.data instanceof JCas) {
            cas = ((JCas) document.data).getCas();
        }
        else if (document.data == null || delegate instanceof CollectionReader) {
            // Force re-scan of type systems because we dynamically add JARs to the
            // classpath using grape - failure to do so will cause some types not to
            // be detected when the pipeline is actually run
            TypeSystemDescriptionFactory.forceTypeDescriptorsScan();
            cas = JCasFactory.createJCas().getCas();
        }
        else {
            throw new IllegalArgumentException("Cannot process $document");
        }
        
        if (delegate instanceof CollectionReader) {
            cas.reset();
            if (delegate.hasNext()) {
                delegate.getNext(cas);
                document.data = cas;
            }
            else {
                // Signal end of processing
                document.data = null;
            }
        }
        else if (delegate instanceof AnalysisEngine) {
            delegate.process(cas);
        }
    }

    @Override
    def destroy()
    {
        if (delegate instanceof AnalysisEngine) {
            delegate.collectionProcessComplete();
            delegate.destroy();
        }
    }
}