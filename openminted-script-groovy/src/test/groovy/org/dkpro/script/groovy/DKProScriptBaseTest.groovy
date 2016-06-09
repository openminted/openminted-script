// ****************************************************************************
// Copyright 2015
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
package org.dkpro.script.groovy

import static org.junit.Assert.*
import groovy.io.FileType

import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(value = Parameterized.class)
class DKProScriptBaseTest {
    // Scan all the subdirs in src/test/resources and use them to parameterize the test
    @Parameters(name = "{index}: running script {0}")
    public static Iterable<Object[]> testScripts() {
        def dirs = [];
        new File("src/test/resources/DKProScriptBase").eachDir({ dirs << ([ it.name ] as Object[]) });
        // Uncomment below and enter the name of one or more tests to run these specifically.
        // dirs = [ ["ExplainTextFormat"] as Object[] ];
        return dirs;
    }
    
    private String script;
    
    public DKProScriptBaseTest(String aName)
    {
        script = aName;
    }
    
    private static String oldModelCache;
    private static String oldGrapeCache;
    
    @BeforeClass
    public static void before()
    {
        System.setProperty("groovy.grape.report.downloads", "true");
        oldModelCache = System.setProperty("dkpro.model.repository.cache", 
            "target/test-output/models");
        //oldGrapeCache = System.setProperty("grape.root", "target/test-output/grapes");
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
        runTest(script, new File("src/test/resources/DKProScriptBase/${script}/output.txt").exists());
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
            cc.setScriptBaseClass(DKProCoreScript.name);
    
            def base = new File("src/test/resources/DKProScriptBase/${aName}").toURI().toURL().toString();
    
            // Create a GroovyClassLoader explicitly here so that Grape can work in the script
            GroovyClassLoader gcl = new GroovyClassLoader(this.getClass().getClassLoader(), cc);
            GroovyScriptEngine engine = new GroovyScriptEngine("src/test/resources/DKProScriptBase/${aName}", gcl);
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
            }
            if (error) {
                System.out.println(capturedOut.toString('UTF-8'));
            }
        }
        
        // Compare captured output
        if (aCaptureStdOut) {
            assertEquals(
                new File("src/test/resources/DKProScriptBase/${aName}/output.txt").getText('UTF-8').trim(),
                capturedOut.toString('UTF-8').trim());
        }
        // Compare file-based output
        else {
            File referenceDir = new File("src/test/resources/DKProScriptBase/${aName}/output");
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
