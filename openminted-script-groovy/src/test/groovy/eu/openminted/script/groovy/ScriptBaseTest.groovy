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

import static org.junit.Assert.*
import groovy.io.FileType

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory;
import org.apache.ivy.util.DefaultMessageLogger
import org.apache.ivy.util.Message
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

import de.tudarmstadt.ukp.dkpro.core.api.resources.internal.ApacheCommonsLoggingAdapter;

@RunWith(value = Parameterized.class)
class ScriptBaseTest {
    // Scan all the subdirs in src/test/resources and use them to parameterize the test
    @Parameters(name = "{index}: running script {0}")
    public static Iterable<Object[]> testScripts() {
        def dirs = [];
        new File("src/test/resources/ScriptBase").eachDir({ dirs << ([ it.name ] as Object[]) });
        // Uncomment below and enter the name of one or more tests to run these specifically.
         dirs = [ ["GateDKPro_CatalogTest"] as Object[] ];
        return dirs;
    }
    
    private static Log LOG = LogFactory.getLog("scriptbase.ivy");
    
    private String script;
    
    public ScriptBaseTest(String aName)
    {
        script = aName;
    }
    
    private static String oldModelCache;
    private static String oldGrapeCache;
    
    @BeforeClass
    public static void before()
    {
        // Yank up ivy debugging for dependency downloading
        //Message.setDefaultLogger(new DefaultMessageLogger(Message.MSG_DEBUG))
//        Message.setDefaultLogger(new ApacheCommonsLoggingAdapter(LOG));
//        
//        System.setProperty("ivy.message.logger.level", String.valueOf(Message.MSG_DEBUG));
//        System.setProperty("groovy.grape.report.downloads", "true");

//        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
//        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
//        
//        // httpclient 4.x
//        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "DEBUG");
//        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "ERROR");
//        
//        // httpclient 3.x
//        System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire.header", "debug");
//        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.commons.httpclient", "debug");
        
        oldModelCache = System.setProperty("dkpro.model.repository.cache",
                "target/test-output/models");
        oldGrapeCache = System.setProperty("grape.root", "target/test-output/grapes");
    }

    @AfterClass
    public static void after()
    {
        if (oldModelCache != null) {
            System.setProperty("dkpro.model.repository.cache", oldModelCache);
        }
        else {
            System.getProperties().remove("dkpro.model.repository.cache");
        }
        if (oldGrapeCache != null) {
            System.setProperty("grape.root", oldGrapeCache);
        }
        else {
            System.getProperties().remove("grape.root");
        }
    }

    @Test
    public void runTest()
    {
        // If we have an "output.txt" file next to the script, we capture stdout and compare
        // it to the file's contents
        runTest(script, new File("src/test/resources/ScriptBase/${script}/output.txt").exists());
    }
    
    public void runTest(String aName, boolean aCaptureStdOut) {
        PrintStream originalOut;
        ByteArrayOutputStream capturedOut;
        if (aCaptureStdOut) {
            // System.err.println "Capturing stdout...";
            originalOut = System.out;
            capturedOut = new ByteArrayOutputStream();
            System.setOut(new PrintStream(capturedOut));
        }
        
        boolean error = true;
        try {
            CompilerConfiguration cc = new CompilerConfiguration();
            cc.setScriptBaseClass(ScriptBase.name);
    
            def base = new File("src/test/resources/ScriptBase/${aName}").toURI().toURL().toString();
    
            // Create a GroovyClassLoader explicitly here so that Grape can work in the script
            GroovyClassLoader gcl = new GroovyClassLoader(this.getClass().getClassLoader(), cc);
            GroovyScriptEngine engine = new GroovyScriptEngine("src/test/resources/ScriptBase/${aName}", gcl);
            engine.setConfig(cc);
    
            Binding binding = new Binding();
            binding.setVariable("testOutputPath", "target/test-output/${aName}".toString());
            Script script = engine.createScript("script.groovy", binding);
            script.run();
            error = false;
        }
        finally {
            if (aCaptureStdOut) {
                // System.err.println "Capturing complete.";
                System.setOut(originalOut);
                
                if (error) {
                    System.out.println(capturedOut.toString('UTF-8'));
                }
            }
        }
        
        // Compare captured output
        if (aCaptureStdOut) {
            assertEquals(
                new File("src/test/resources/ScriptBase/${aName}/output.txt").getText('UTF-8').trim(),
                capturedOut.toString('UTF-8').trim());
        }
        // Compare file-based output
        else {
            File referenceDir = new File("src/test/resources/ScriptBase/${aName}/output");
            referenceDir.eachFileRecurse(FileType.FILES, { referenceFile ->
                String relPath = referenceFile.absolutePath.substring(referenceDir.absolutePath.length());
                File actualFile = new File("target/test-output/${aName}/${relPath}");
                assertEquals(
                    referenceFile.getText("UTF-8").trim(),
                    actualFile.getText("UTF-8").trim())
            });
        }
    }
}
