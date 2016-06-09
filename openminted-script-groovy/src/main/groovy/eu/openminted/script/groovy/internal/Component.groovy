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

import org.apache.uima.fit.factory.ConfigurationParameterFactory;

class Component {
    def name;
    def impl;
    def desc;

    def params(map) {
        map.each { k, v ->
//            try {
                ConfigurationParameterFactory.setParameter(desc, k, v)
            }
        // FIXME find some way to restore auto-explain
//            catch (IllegalArgumentException e) {
//                explain(this);
//                throw e;
//            }
//        }
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