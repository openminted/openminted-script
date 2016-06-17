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

import eu.openminted.script.groovy.internal.PipelineContext
import eu.openminted.script.groovy.internal.gate.GateComponentInstance;
import groovy.grape.Grape
import eu.openminted.script.groovy.internal.Component
import eu.openminted.script.groovy.internal.ComponentOffer
import eu.openminted.script.groovy.internal.ComponentRole;
import eu.openminted.script.groovy.internal.Loader;


class UimaLoader implements Loader {
    PipelineContext context;

    UimaLoader(PipelineContext aContext) {
        context = aContext;
    }
    
    Component load(ComponentOffer offer) {
        def comp = new Component();
        comp.name = offer.name;
        
        // Load component via Maven
        ClassLoader cl = context.findClassLoader();
        Grape.grab(group:offer.groupId, module:offer.artifactId, version: offer.version,
            classLoader: cl);
        
        // Get implementation
        comp.impl = cl.loadClass(offer.implName, true, false);
        comp.offer = offer;
        comp.framework = offer.framework;
        comp.role = offer.role;

        // Create framework-specific descriptor
        switch (offer.role) {
        case ComponentRole.READER:
            comp.desc = createReaderDescription(comp.impl);
            break;
        case ComponentRole.PROCESSOR:
            // fall-though!
        case ComponentRole.WRITER:
            comp.desc = createEngineDescription(comp.impl);
            break;
        default:
            throw new IllegalArgumentException("Unknown role $offer.role");
        }
        
        return comp;
    }

        
    UimaComponentInstance create(Component component) {
        UimaComponentInstance instance = new UimaComponentInstance();
        instance.init(component);
        return instance;
    }
}
