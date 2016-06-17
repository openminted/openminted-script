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
package eu.openminted.script.groovy.internal.dsl;

import org.apache.uima.fit.util.JCasUtil

import eu.openminted.script.groovy.internal.Helper;

import org.apache.uima.fit.util.CasUtil;

/**
 * This class is used by engines/writers that are defined through closures.
 * It redirects common method calls to utility methods in uimaFIT so that the
 * use doesn't have to know where they are defined and doesn't have to import them.
 */
class EngineHelper {
    def jcas;

    def type(String name) {
        def matches = jcas.typeSystem.typeIterator.toList().grep({
            it.name.endsWith('.'+name) || it.name == name
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
