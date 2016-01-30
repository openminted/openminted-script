// ****************************************************************************
// Copyright 2016
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
package org.dkpro.script.groovy.internal

import groovy.grape.Grape;
import groovy.json.*;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;
import static org.apache.uima.fit.factory.CollectionReaderFactory.*;

class PipelineContext
{
    def pipeline = [];
    
    def engines;

    def formats;

    def VERSION = '1.7.0';
    
    Script scriptContext;
    
    ClassLoader classLoader;
    
    def version(ver) {
        VERSION = ver;
    }
    
    def boot() {
        java.util.logging.LogManager.logManager.reset(); // Disable logging
        /*
        java.util.logging.LogManager.logManager.getLogger("").level = java.util.logging.Level.FINEST;
        java.util.logging.LogManager.logManager.getLogger("").handlers.each {
            it.level = java.util.logging.Level.FINEST;
        }
        */

        engines = new JsonSlurper().parseText(new URL(
    'https://gist.githubusercontent.com/reckart/990d75ee230dbb39c30b/raw/ad29ba37ebb77e3f5f9f47fb32c3def15c63954c/engines.json').text);

        formats = new JsonSlurper().parseText(new URL(
    'https://gist.githubusercontent.com/reckart/990d75ee230dbb39c30b/raw/75b9c10c74454c18b8507aed041b20b6e1322a84/formats.json').text);
    }

    def lazyBootComplete = false;

    // Thinks to initialize while the script is running, e.g. if we want to change the versio in
    // the script, then we cannot configure the resolved before
    def lazyBoot() {
        if (!lazyBootComplete) {
            if (VERSION.endsWith('-SNAPSHOT')) {
                Grape.addResolver(
                    name:'ukp-oss-snapshots', 
                    root:'http://zoidberg.ukp.informatik.tu-darmstadt.de/artifactory/public-snapshots')
            }

            lazyBootComplete = true;
        }
    }

    def load(component) {
        lazyBoot();

        def cl = findClassLoader();
        def desc;
        def impl;
        if (component.endsWith("Reader")) {
            def format = formats[component[0..-7]];
            Grape.grab(group:format.groupId, module:format.artifactId, version: VERSION,
                classLoader: cl);
            impl = cl.loadClass(format.readerClass, true, false);
            desc = createReaderDescription(impl);
        }
        else if (component.endsWith("Writer")) {
            def format = formats[component[0..-7]];
            Grape.grab(group:format.groupId, module:format.artifactId, version: VERSION,
                classLoader: cl);
            impl = cl.loadClass(format.writerClass, true, false);
            desc = createEngineDescription(impl);
        }
        else {
            def engine = engines[component];
            Grape.grab(group:engine.groupId, module:engine.artifactId, version: VERSION,
                classLoader: findClassLoader());
            impl = cl.loadClass(engine.class, true, false);
            desc = createEngineDescription(impl);
        }

        def comp = new Component();
        comp.name = component;
        comp.desc = desc;
        comp.impl = impl;
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
