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
package eu.openminted.script.groovy.internal

import eu.openminted.script.groovy.internal.dsl.EngineHelper;

class Helper {
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
    
    static def readerClosureWrapper(closure, helper) {
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
            closure: closure.class.name,
            helper: EngineHelper.class.name
        ];

        def te = new groovy.text.SimpleTemplateEngine();
        def template = te.createTemplate(templateText);
        def result = template.make(data);
    }
}
