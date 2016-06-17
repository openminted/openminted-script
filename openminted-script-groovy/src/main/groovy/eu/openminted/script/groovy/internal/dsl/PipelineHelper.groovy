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
package eu.openminted.script.groovy.internal.dsl

import groovy.grape.Grape;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.*;
import static org.apache.uima.fit.factory.CollectionReaderFactory.*;
import static org.apache.uima.fit.factory.TypeSystemDescriptionFactory.*;
import static org.apache.uima.fit.pipeline.SimplePipeline.*;

import org.apache.uima.collection.CollectionReader_ImplBase;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_component.AnalysisComponent_ImplBase;

import org.apache.uima.fit.factory.TypeSystemDescriptionFactory

import eu.openminted.script.groovy.internal.Component
import eu.openminted.script.groovy.internal.ComponentRole;
import eu.openminted.script.groovy.internal.PipelineContext;

import org.apache.uima.fit.component.JCasAnnotator_ImplBase

import java.util.UUID;

class PipelineHelper
{
    PipelineContext context;
    
    def PipelineHelper(PipelineContext aContext) {
        context = aContext;
    }
    
    def apply(@DelegatesTo(WriterHelper) engine) {
        if (engine instanceof String || engine instanceof GString) {
            def component = context.load(engine, ComponentRole.PROCESSOR);
            context.pipeline.add(component);
            return component;
        }
        else {
            throw new IllegalArgumentException("Cannot apply $engine");
        }
    }

    def read(@DelegatesTo(EngineHelper) format) {
        if (!context.pipeline.isEmpty()) {
            throw new IllegalStateException("Reader must be first and there can only be one");
        }

        if (format instanceof String || format instanceof GString) {
            def component = context.load(format, ComponentRole.READER);
            context.pipeline.add(component);
            return component;
        }
        else {
            throw new IllegalArgumentException("Cannot read $format");
        }
    }

    def write(@DelegatesTo(WriterHelper) format) {
        if (format instanceof String || format instanceof GString) {
            def component = context.load(format, ComponentRole.WRITER);
            context.pipeline.add(component);
            return component;
        }
        else {
            throw new IllegalArgumentException("Cannot write $format");
        }
    }
}
