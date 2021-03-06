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

import org.apache.uima.fit.factory.ConfigurationParameterFactory;

class Component {
    String name;
    String framework;
    def offer;
    def impl;
    def desc;
    ComponentRole role;
    Map parameters = [:];

    def params(map) {
        map.each { k, v ->
            parameters[k] = v;
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