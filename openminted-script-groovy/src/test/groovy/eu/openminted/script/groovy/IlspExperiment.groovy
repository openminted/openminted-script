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

import org.junit.Ignore;
import org.junit.Test;
import wslite.soap.SOAPClient
import wslite.soap.SOAPMessageBuilder


class IlspExperiment
{
    @Ignore("Doesn't work yet")
    @Test
    public void test()
    {
        def client = new SOAPClient('http://nlp.ilsp.gr:80/soaplab2-axis/typed/services/getstarted.ilsp_nlp')
        
        def message = new SOAPMessageBuilder().build { 
            body {
                appInputs('xmlns':'http://soaplab.org/ilsp_nlp') {
                    inputType(2011)
                    outputType("txt")
                    input_direct_data("This is a test.")
                    inputEncoding("UTF-8")
                    language("el")
                }
            }
        }
        
        println message;
        
        def response = client.send(SOAPAction:'runAndWaitFor') {
            body {
                appInputs('xmlns':'http://soaplab.org/ilsp_nlp') {
                    inputType(2011)
                    outputType("txt")
                    input_direct_data("This is a test.")
                    inputEncoding("UTF-8")
                    language("el")
                }
            }
        }
        println response;
    }
}
