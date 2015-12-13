// ******************************************************************************
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
// ****************************************************************************/
package org.dkpro.script.groovy;

import groovy.grape.Grape;
import groovy.json.*;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;
import static org.apache.uima.fit.factory.CollectionReaderFactory.*;
import static org.apache.uima.fit.factory.TypeSystemDescriptionFactory.*;
import static org.apache.uima.fit.pipeline.SimplePipeline.*;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.fit.factory.ConfigurationParameterFactory;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.analysis_component.AnalysisComponent_ImplBase;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import java.util.UUID;

abstract class DKProCoreScript extends Script {
    def VERSION = '1.7.0';

    def engines;

    def formats;

    def pipeline = [];
    
    abstract void scriptBody()

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

    def run() {
        // We set the thread context classloader such that UIMA has access to all the classes
        // defined in the script and loaded via grab.
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(findClassLoader());
        try {
            boot();
            scriptBody();
            // Force re-scan of type systems because we dynamically add JARs to the
            // classpath using grape - failure to do so will cause some types not to
            // be detected when the pipeline is actually run
            forceTypeDescriptorsScan();
            def ts = createTypeSystemDescription();
            // runpipeline constructs the type system from the descriptors passed to
            // it - make sure at least one of the components actually has the full
            // type system
            pipeline[0].desc.collectionReaderMetaData.typeSystem = ts;
            runPipeline(
                pipeline[0].desc as CollectionReaderDescription, 
                pipeline[1..-1].collect { it.desc } as AnalysisEngineDescription[]);
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldLoader);
        }

    }

    class Component {
        def name;
        def impl;
        def desc;

        def params(map) {
            map.each { k, v ->
                try {
                    ConfigurationParameterFactory.setParameter(desc, k, v)
                }
                catch (IllegalArgumentException e) {
                    explain(this);
                    throw e;
                }
            }
            return this;
        }

        def language(lang) {
            params(['language': lang]);         
        }

        def from(location) {
            params(['sourceLocation': location]);           
        }

        def to(location) {
            params(['targetLocation': location]);           
        }
    }

    def load(component) {
        lazyBoot();

        def desc;
        def impl;
        if (component.endsWith("Reader")) {
            def format = formats[component[0..-7]];
            Grape.grab(group:format.groupId, module:format.artifactId, version: VERSION,
                classLoader: findClassLoader());
            impl = this.class.classLoader.loadClass(format.readerClass, true, false);
            desc = createReaderDescription(impl);
        }
        else if (component.endsWith("Writer")) {
            def format = formats[component[0..-7]];
            Grape.grab(group:format.groupId, module:format.artifactId, version: VERSION,
                classLoader: findClassLoader());
            impl = this.class.classLoader.loadClass(format.writerClass, true, false);
            desc = createEngineDescription(impl);
        }
        else {
            def engine = engines[component];
            Grape.grab(group:engine.groupId, module:engine.artifactId, version: VERSION,
                classLoader: findClassLoader());
            impl = this.class.classLoader.loadClass(engine.class, true, false);
            desc = createEngineDescription(impl);
        }

        def comp = new Component();
        comp.name = component;
        comp.desc = desc;
        comp.impl = impl;
        return comp;
    }

    def explain(componentOrName) {
        def component = componentOrName instanceof String ? load(componentOrName) : componentOrName;
        def desc = component.desc;

        def paramDecls;
        if (desc instanceof CollectionReaderDescription) {
            paramDecls = desc.metaData.configurationParameterDeclarations.configurationParameters;
        }
        else {
            paramDecls = desc.analysisEngineMetaData.configurationParameterDeclarations.configurationParameters;
        }

        println component.name;
        println '=' * component.name.length();
        println component.desc.metaData.description;
        println '';
        println 'Parameters';
        println '----------';
        println '';
        paramDecls.each {
            printf("* %s (%s) %s%n", it.name, it.type, it.isMultiValued() ? "MULTI" : "");
            printf("  %s%n%n", it.description.replaceAll("\\s+"," "));
        }
    }

    def inventory() {
        println "\nComponents:"
        engines.each {
            println "  ${it.key}"
        }
        println "\nFormats:"
        formats.each {
            println "  ${it.key}"
        }   
    }

    def read(format) {
        if (!pipeline.isEmpty()) {
            throw new IllegalStateException("Reader must be first and there can only be one");
        }

        if (format instanceof String) {
            assert formats[format];
            def component = load(format+"Reader");
            pipeline.add(component);
            return component;
        }
        else if (format in CollectionReader_ImplBase) {
            def component = new Component();
            component.name = format.name;
            component.impl = format;
            component.desc = createReaderDescription(component.impl);
            pipeline.add(component);
            return component;
        }
        else if (format in Closure) {
            // Need to load the DKPro Core IO API here
            Grape.grab(group:'de.tudarmstadt.ukp.dkpro.core', 
                module:'de.tudarmstadt.ukp.dkpro.core.api.io-asl', version: VERSION,
                classLoader: findClassLoader());;

            def templateText = '''
            import $helper;

            class $name extends de.tudarmstadt.ukp.dkpro.core.api.io.JCasResourceCollectionReader_ImplBase
            {
                void getNext(org.apache.uima.jcas.JCas jcas) {
                    $helper helper = new $helper();
                    helper.jcas = jcas;
                    def closure = Class.forName('$closure').newInstance(this, (Object) null);
                    closure.delegate = helper;
                    closure.resolveStrategy = Closure.DELEGATE_FIRST;
                    closure.call(jcas);
                }
            }
            '''
            def data = [
                name: 'ClosureWrapper_' + (UUID.randomUUID() as String).replace('-', ''),
                closure: format.class.name,
                helper: EngineHelper.class.name
            ];

            def te = new groovy.text.SimpleTemplateEngine();
            def template = te.createTemplate(templateText);
            def result = template.make(data);
            def cls = findClassLoader().parseClass(result.toString());

            def component = new Component();
            component.name = cls.name;
            component.impl = cls;
            component.desc = createReaderDescription(component.impl);
            pipeline.add(component);
            return component;
        }
        else {
            throw new IllegalArgumentException("Cannot apply $engine");
        }
    }

    def apply(engine) {
        if (engine instanceof String) {
            assert engines[engine];

            def component = load(engine);
            pipeline.add(component);
            return component;
        }
        else if (engine in AnalysisComponent_ImplBase) {
            def component = new Component();
            component.name = engine.name;
            component.impl = engine;
            component.desc = createEngineDescription(component.impl);
            pipeline.add(component);
            return component;
        }
        else if (engine in Closure) {
            def result = Helper.closureWrapper(engine, EngineHelper.class)
            def cls = findClassLoader().parseClass(result.toString());

            def component = new Component();
            component.name = cls.name;
            component.impl = cls;
            component.desc = createEngineDescription(component.impl);
            pipeline.add(component);
            return component;
        }
        else {
            throw new IllegalArgumentException("Cannot apply $engine");
        }
    }

    /**
     * This class is used by engines/writers that are defined through closures.
     * It redirecty common method calls to utility methods in uimaFIT so that the
     * use doesn't have to know where they are defined and doesn't have to import them.
     */
    class EngineHelper {
        def jcas;

        def type(String name) {
            def matches = jcas.typeSystem.typeIterator.toList().grep({
                it.name.endsWith('.'+name) && it.name == name
            });
            switch (matches.size) {
                case 0: throw new IllegalArgumentException("No type matches '$name'");
                case 1: return matches[0];
                default: throw new IllegalArgumentException("More than one type matches '$name': " + matches);
            }
        }

        def invokeMethod(String name, args) {
            if (name.startsWith("select")) {
                if (args.any { it instanceof org.apache.uima.cas.Type}) {
                    // Assuming CAS-based access
                    // This allows the user to omit the "CAS" argument which is always
                    // the first argument in the select methods.
                    args = [jcas.cas, *args]
                    def argClasses = Helper.argsToClassses(args);
                    def m = CasUtil.metaClass.getStaticMetaMethod(name, argClasses);
                    def result = (m ? m.invoke(null, *args) : 
                        this.metaClass.invokeMissingMethod(null, name, args))
                    return result;
                }
                else {
                    // Assuming JCas-based access
                    // This allows the user to omit the "JCas" argument which is always
                    // the first argument in the select methods.
                    args = [jcas, *args]
                    def argClasses = Helper.argsToClassses(args);
                    def m = JCasUtil.metaClass.getStaticMetaMethod(name, argClasses);
                    def result = (m ? m.invoke(null, *args) : 
                        this.metaClass.invokeMissingMethod(null, name, args))
                    return result;
                }
            } else {
                throw new MissingMethodException(name, this.class, args)
            }
        }
    }
    
    class WriterHelper extends EngineHelper {
        
    }

    static class Helper {
        static def argsToClassses(java.util.Collection args) {
            return args.collect {
                    if (it == null) {
                        return null;
                    }
                    else if (it instanceof Class) {
                        return Class;
                    }
                    else {
                        return it.class;
                    }
                } as Object[];
        }
        
        static def closureWrapper(closure, helper) {
            def templateText = '''
            import $helper;

            class $name extends org.apache.uima.fit.component.JCasAnnotator_ImplBase
            {
                void process(org.apache.uima.jcas.JCas jcas) {
                    $helper helper = new $helper();
                    helper.jcas = jcas;
                    def closure = Class.forName('$closure').newInstance(this, (Object) null);
                    closure.delegate = helper;
                    closure.resolveStrategy = Closure.DELEGATE_FIRST;
                    closure.call(jcas);
                }
            }
            '''
            def data = [
                name: 'ClosureWrapper_' + (UUID.randomUUID() as String).replace('-', ''),
                closure: closure.class.name,
                helper: helper.name
            ];

            def te = new groovy.text.SimpleTemplateEngine();
            def template = te.createTemplate(templateText);
            def result = template.make(data);
        }
    }

    def findClassLoader() {
        def classLoader;
        // When setting the parentClassLoader property, then UIMA no longer has access to the
        // classes defined in the script, e.g. to closures!
        if (binding.variables.get("parentClassLoader")) {
            classLoader = binding.variables.get("parentClassLoader");
        }
        else {
            classLoader = this.class.classLoader;
        }
        return classLoader;
    }

    def write(format) {
        if (format instanceof String) {
            assert formats[format];

            def component = load(format+"Writer");
            pipeline.add(component);
            return component;
        }
        else if (format in Closure) {
            def result = Helper.closureWrapper(format, WriterHelper.class)
            def cls = findClassLoader().parseClass(result.toString());

            def component = new Component();
            component.name = cls.name;
            component.impl = cls;
            component.desc = createEngineDescription(component.impl);
            pipeline.add(component);
            return component;
        }
        else {
            throw new IllegalArgumentException("Cannot write $format");
        }
    }

    def typeSystem() {
        def ts = createTypeSystemDescription();
        ts.types.each {
            println it.name;
        }
    }
}