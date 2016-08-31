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
package eu.openminted.script.groovy

import eu.openminted.script.groovy.internal.frameworkconversions.DKPro2Lif
import eu.openminted.script.groovy.internal.frameworkconversions.Lif2DKPro

import org.apache.commons.io.IOUtils;
import org.apache.uima.fit.factory.JCasFactory
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas
import org.junit.Test
import org.lappsgrid.client.ServiceClient
import org.lappsgrid.discriminator.Discriminators.Uri
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.DataContainer
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;;

class LappsExperiment
{
    String base = "http://vassar.lappsgrid.org/invoker/anc";
    String username = "tester";
    String password = "tester";
    String service = "stanford.splitter_2.0.0";

    @Test
    public void test() {
        // Data<String> data  = new Data<>(Uri.TEXT, "This is a sentence. This is another one.");
        
        
        JCas source = JCasFactory.createText("This is a sentence. This is another one.", "en");

        Container container = new Container();
        new DKPro2Lif().convert(source, container);
        
        DataContainer whiteboard;
        
        ServiceClient tokenizer = new ServiceClient("${base}:stanford.tokenizer_2.0.0", username, password)
        whiteboard = Serializer.parse(
            tokenizer.execute(new Data<>(Uri.LIF, container).asJson()),
            DataContainer);
        println "- SPLITTER ---------------------------------------------------------------------";
        println whiteboard.asPrettyJson();

        ServiceClient splitter = new ServiceClient("${base}:stanford.splitter_2.0.0", username, password)
        whiteboard = Serializer.parse(
            splitter.execute(new Data<>(Uri.LIF, whiteboard.getPayload()).asJson()), 
            DataContainer);
        println "- SPLITTER ---------------------------------------------------------------------";
        println whiteboard.asPrettyJson();
        
        ServiceClient tagger = new ServiceClient("${base}:stanford.tagger_2.0.0", username, password)
        whiteboard = Serializer.parse(
            tagger.execute(new Data<>(Uri.LIF, whiteboard.getPayload()).asJson()), 
            DataContainer);
        println "- TAGGER -----------------------------------------------------------------------";
        println whiteboard.asPrettyJson();
        
        JCas target = JCasFactory.createJCas();
        new Lif2DKPro().convert(whiteboard.getPayload(), target);
        
        println JCasUtil.select(target, Sentence).size();
        println JCasUtil.select(target, Token).size();
    }
    
    private Data run(String aService)
    {
        Container container = new Container();
        ServiceClient service = new ServiceClient("${base}:${aService}", username, password)
        DataContainer result1 = Serializer.parse(
            service.execute(new Data<>(Uri.LIF, container).asJson()),
            DataContainer);
    } 
}
