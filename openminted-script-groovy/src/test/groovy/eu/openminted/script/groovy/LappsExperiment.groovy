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

import org.junit.Test
import org.lappsgrid.client.ServiceClient
import org.lappsgrid.discriminator.Discriminators.Uri;
import org.lappsgrid.serialization.Data;
import org.lappsgrid.serialization.Serializer;
import org.lappsgrid.serialization.lif.Container;

class LappsExperiment
{
    String base = "http://vassar.lappsgrid.org/invoker/anc";
    String username = "tester";
    String password = "tester";
    String service = "stanford.splitter_2.0.0";

    @Test
    public void test() {
        ServiceClient client = new ServiceClient("${base}:${service}", username, password)
        println client.getMetadata();

        Container container = new Container();
        container.setLanguage("en");
        container.setText("This is a sentence. This is another one.");

        Data<String> data  = new Data<>(Uri.LIF, container);
        //        Data<String> data  = new Data<>(Uri.TEXT, "This is a sentence. This is another one.");

        String result = client.execute(data.asJson());

        println result;
    }
}
