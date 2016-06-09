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
package eu.openminted.script.groovy.internal

import groovy.grape.Grape;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;
import static org.apache.uima.fit.factory.CollectionReaderFactory.*;
import static org.apache.uima.fit.factory.TypeSystemDescriptionFactory.*;
import static org.apache.uima.fit.pipeline.SimplePipeline.*;

import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_component.AnalysisComponent_ImplBase;

import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;

import java.util.UUID;

class PipelineHelper
{
    PipelineContext context;
    
    def PipelineHelper(PipelineContext aContext) {
        context = aContext;
    }
    
    def version(ver) {
        context.version(ver);
    }

    def inventory() {
        println "\nComponents:"
        context.engines.each {
            println "  ${it.key}"
        }
        println "\nFormats:"
        context.formats.each {
            println "  ${it.key}"
        }
    }

    def apply(@DelegatesTo(WriterHelper) engine) {
        if (engine instanceof String || engine instanceof GString) {
            assert context.engines[engine];

            def component = context.load(engine);
            context.pipeline.add(component);
            return component;
        }
        else if (engine in AnalysisComponent_ImplBase) {
            def component = new Component();
            component.name = engine.name;
            component.impl = engine;
            component.desc = createEngineDescription(component.impl);
            context.pipeline.add(component);
            return component;
        }
        else if (engine in Closure) {
            def result = Helper.closureWrapper(engine, EngineHelper.class)
            def cls = context.findClassLoader().parseClass(result.toString());

            def component = new Component();
            component.name = cls.name;
            component.impl = cls;
            component.desc = createEngineDescription(component.impl);
            context.pipeline.add(component);
            return component;
        }
        else {
            throw new IllegalArgumentException("Cannot apply $engine");
        }
    }

    def explain(componentOrName) {
        if (context.formats[componentOrName]) {
            if (context.formats[componentOrName].readerClass) {
                explain("${componentOrName}Reader");
            }
            if (context.formats[componentOrName].writerClass) {
                explain("${componentOrName}Writer");
            }
            return;
        }
        
        def component = (componentOrName instanceof String || componentOrName instanceof GString) ?
            context.load(componentOrName) : componentOrName;
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

    def read(@DelegatesTo(EngineHelper) format) {
        if (!context.pipeline.isEmpty()) {
            throw new IllegalStateException("Reader must be first and there can only be one");
        }

        if (format instanceof String || format instanceof GString) {
            assert context.formats[format];
            def component = context.load(format+"Reader");
            context.pipeline.add(component);
            return component;
        }
        else if (format in CollectionReader_ImplBase) {
            def component = new Component();
            component.name = format.name;
            component.impl = format;
            component.desc = createReaderDescription(component.impl);
            context.pipeline.add(component);
            return component;
        }
        else if (format in Closure) {
            // Need to load the DKPro Core IO API here
            Grape.grab(group:'de.tudarmstadt.ukp.dkpro.core',
                module:'de.tudarmstadt.ukp.dkpro.core.api.io-asl', version: context.VERSION,
                classLoader: context.findClassLoader());

            def result = Helper.readerClosureWrapper(format, EngineHelper.class)
            def cls = context.findClassLoader().parseClass(result.toString());

            def component = new Component();
            component.name = cls.name;
            component.impl = cls;
            component.desc = createReaderDescription(component.impl);
            context.pipeline.add(component);
            return component;
        }
        else {
            throw new IllegalArgumentException("Cannot apply $engine");
        }
    }

    def write(@DelegatesTo(WriterHelper) format) {
        if (format instanceof String || format instanceof GString) {
            assert context.formats[format];

            def component = context.load(format+"Writer");
            context.pipeline.add(component);
            return component;
        }
        else if (format in Closure) {
            def result = Helper.closureWrapper(format, WriterHelper.class)
            def cls = context.findClassLoader().parseClass(result.toString());

            def component = new Component();
            component.name = cls.name;
            component.impl = cls;
            component.desc = createEngineDescription(component.impl);
            context.pipeline.add(component);
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
