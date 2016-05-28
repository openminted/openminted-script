package org.dkpro.script.groovy.external
import static groovy.io.FileType.FILES;
import groovy.json.*;
import groovy.transform.Field;

import static org.apache.uima.fit.factory.ResourceCreationSpecifierFactory.*;

import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;

import org.apache.uima.collection.CollectionReaderDescription
import org.apache.uima.resource.ResourceSpecifier
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.util.XMLInputSource
import org.apache.uima.util.XMLParser;;;;

class GenerateEngineAndFormat {
	static String dkproCorePath="/home/anshul/dkpro-core/";
	
	
	static def engines = [:];
	static File enginesJSONFile = new File("src/main/resources/PipelineContextJSON/engines.json");
	
	static def formats = [:];
	static File formatsJSONFile = new File("src/main/resources/PipelineContextJSON/formats.json");
	
	def static locatePom(File path) {
		def pom = new File(path, "pom.xml");
		if (pom.exists()) {
			return pom;
		}
		else if (path.getParentFile() != null) {
			return locatePom(path.getParentFile());
		}
		else {
			return null;
		}
	}

	def static addFormat(format, kind, pom, clazz) {
		if (!formats[format]) {
			formats[format] = [
				groupId: pom.groupId ? pom.groupId.text() : pom.parent.groupId.text(),
				artifactId: pom.artifactId.text(),
				version: pom.version ? pom.version.text() : pom.parent.version.text(),
			];
		}
		formats[format][kind] = clazz;
	}
	static void main(String... args){
		new File(dkproCorePath).eachFileRecurse(FILES) {

			if(it.name.endsWith('.xml') && !it.path.contains("src/test/java") && it.path.contains('/target/classes/')) {

				try {
//					println it.name
					def spec = createResourceCreationSpecifier(it.path, null);
					if (spec instanceof AnalysisEngineDescription) {
//						 println "AE " + it;
						
						def implName = spec.annotatorImplementationName;
						def uniqueName = implName.substring(implName.lastIndexOf('.')+1);
						def pomFile =  locatePom(it.absoluteFile);
						def pom = new XmlParser().parse(pomFile);

						if (!implName.contains('$')) {
							if (implName.endsWith('Writer')) {
								def format = uniqueName[0..-7];
								addFormat(format, 'writerClass', pom, spec.annotatorImplementationName);
							}
							else {
								engines[uniqueName] = [
									name: uniqueName,
									groupId: pom.groupId ? pom.groupId.text() : pom.parent.groupId.text(),
									artifactId: pom.artifactId.text(),
									version: pom.version ? pom.version.text() : pom.parent.version.text(),
									class:  spec.annotatorImplementationName
								];
							}
						}
					}
					else if (spec instanceof CollectionReaderDescription) {
//						 println "CR " + it;
						def implName = spec.implementationName;
						if (implName.endsWith('Reader') && !implName.contains('$')) {
							def uniqueName = implName.substring(implName.lastIndexOf('.')+1);
							def pomFile = locatePom(it);
							def pom = new XmlParser().parse(pomFile);
							def format = uniqueName[0..-7];
							addFormat(format, 'readerClass', pom, implName);
						}
					}
					else {
						// println "?? " + it;
					}
				}
				catch (org.apache.uima.util.InvalidXMLException e) {
					// Ignore
//					println e
				}
			}
		}
		if(enginesJSONFile.exists())
			enginesJSONFile.delete();
		if(formatsJSONFile.exists())
			formatsJSONFile.delete();
		if(!enginesJSONFile.parentFile.exists())
			enginesJSONFile.parentFile.mkdirs();
		
		enginesJSONFile.createNewFile();
		enginesJSONFile << JsonOutput.prettyPrint(JsonOutput.toJson(engines));
		formatsJSONFile.createNewFile();
		formatsJSONFile << JsonOutput.prettyPrint(JsonOutput.toJson(formats));
		
	}
}
