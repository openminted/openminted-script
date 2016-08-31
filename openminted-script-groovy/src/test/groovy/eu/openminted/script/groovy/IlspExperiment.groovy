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
import org.junit.Test

import wslite.rest.Response;
import wslite.soap.SOAPClient
import wslite.soap.SOAPMessageBuilder


class IlspExperiment
{

    @Test
    public void test()
    {
        def client = new SOAPClient('http://nlp.ilsp.gr:80/soaplab2-axis/typed/services/getstarted.ilsp_nlp')

        def reqMsg = new SOAPMessageBuilder().build {
            body {
                'ns2:run'('xmlns:ns2':'http://soaplab.org/ilsp_nlp','xmlns:ns3' : 'http://soaplab.org/typedws') {
                    InputType("txt")
                    OutputType("xceslemma")
                    input_direct_data("Η Αττική είναι ιστορική περιοχή της Ελλάδας που σήμερα.")
                    input_url("")
                    InputEncoding("UTF-8")
                    language("el")
                    inputIsURLlist("false")
                }
            }
        }
        def response = client.send(reqMsg.toString());
        def jobIdObj =  response.envelope;
        def waitForMsg =  new SOAPMessageBuilder().build {
            body {
                'ns3:waitfor'('xmlns:ns2':'http://soaplab.org/ilsp_nlp','xmlns:ns3' : 'http://soaplab.org/typedws'){
                    jobId{ jobId(jobIdObj) }
                }
            }
        }
        def waitForJob = client.send(waitForMsg.toString())

        def statusMsg = new SOAPMessageBuilder().build {
            body {
                'ns3:getResults'('xmlns:ns2':'http://soaplab.org/ilsp_nlp','xmlns:ns3' : 'http://soaplab.org/typedws'){
                    jobId{ jobId(jobIdObj) }
                }
            }            
        }
        def statusResponse = client.send(statusMsg.toString());
        
        // TODO: check if complete then only move forward
        
        def resultMsg = new SOAPMessageBuilder().build {
            body {
                'ns3:getResults'('xmlns:ns2':'http://soaplab.org/ilsp_nlp','xmlns:ns3' : 'http://soaplab.org/typedws'){
                    jobId{ jobId(jobIdObj) }
                }
            }
        }
        
        def resultResponse = client.send(resultMsg.toString());
        
        println resultResponse.text;
    }
}
