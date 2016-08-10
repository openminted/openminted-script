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

import org.apache.ivy.util.DefaultMessageLogger;
import org.apache.ivy.util.Message
import org.apache.ivy.util.MessageLogger;

import eu.openminted.script.groovy.internal.Component
import eu.openminted.script.groovy.internal.ComponentOffer
import eu.openminted.script.groovy.internal.Loader
import eu.openminted.script.groovy.internal.PipelineContext
import gate.Gate
import gate.Plugin
import groovy.grape.Grape

class GateLoader implements Loader {
	PipelineContext context;

	GateLoader(PipelineContext aContext) {
		context = aContext;
		if (!Gate.isInitialised()) {
			// Save logger because Gate replaces it
			MessageLogger logger = Message.getDefaultLogger();

			// must be called before you can do anything with the GATE API
			Gate.init();

			// Restore logger
			Message.setDefaultLogger(logger);
		}
	}

	Component load(ComponentOffer offer) {
		ClassLoader cl = context.findClassLoader();

		// load the plugin via Maven
		if(!offer.artifactId.equals("gate-core"))
		{
			Plugin plugin = new Plugin.Maven(offer.groupId, offer.artifactId, offer.version);
			Gate.getCreoleRegister().registerPlugin(plugin);
		}
		def comp = new Component();
		comp.name = offer.name;
		comp.desc = null;
		comp.impl = offer.implName;
		comp.offer = offer;
		comp.role = offer.role;
		comp.framework = offer.framework;
		return comp;
	}

	GateComponentInstance create(Component component) {
		GateComponentInstance instance = new GateComponentInstance();
		instance.init(component);
		return instance;
	}
}
