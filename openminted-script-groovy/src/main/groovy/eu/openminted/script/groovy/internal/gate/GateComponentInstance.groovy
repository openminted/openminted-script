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
package eu.openminted.script.groovy.internal.gate

import org.apache.uima.fit.factory.ConfigurationParameterFactory

import eu.openminted.script.groovy.internal.Component
import eu.openminted.script.groovy.internal.ComponentInstance
import eu.openminted.script.groovy.internal.Document;
import gate.Factory
import gate.FeatureMap;
import gate.Gate;
import gate.LanguageAnalyser
import gate.Plugin
import gate.util.SimpleFeatureMapImpl;;

class GateComponentInstance implements ComponentInstance {
    private LanguageAnalyser delegate;
    
    def init(Component decl) {
        FeatureMap fm = new SimpleFeatureMapImpl();
        fm.putAll(decl.parameters);
        delegate = (LanguageAnalyser) Factory.createResource(decl.impl, fm);
    }

    @Override
    def process(Document document)
    {
        if (!(document.data instanceof gate.Document)) {
            throw new IllegalArgumentException("Cannot process $document");
        }
        
        delegate.setDocument(document.data);
        delegate.execute();
    }

    @Override
    def destroy()
    {
        Factory.deleteResource(delegate);
    }
}