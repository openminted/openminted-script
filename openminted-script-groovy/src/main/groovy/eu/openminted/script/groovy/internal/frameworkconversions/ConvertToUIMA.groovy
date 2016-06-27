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
package eu.openminted.script.groovy.internal.frameworkconversions

import gate.Annotation
import gate.AnnotationSet
import it.unimi.dsi.fastutil.ints.IntOpenHashSet

import org.apache.uima.UIMAException
import org.apache.uima.fit.factory.JCasFactory
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory
import org.apache.uima.jcas.JCas

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token


class ConvertToUIMA  implements ConvertFramework{

	@Override
	public JCas convert(def data) throws UIMAException {

		TypeSystemDescriptionFactory.forceTypeDescriptorsScan();
		JCas jcas = JCasFactory.createJCas();

		if(data instanceof gate.Document) {

			jcas.setDocumentText(data.getContent().toString());
			AnnotationSet annSet = data.getAnnotations();
			IntOpenHashSet processed = new IntOpenHashSet();

			for(Annotation ann in annSet){

				if (processed.contains(ann.getId())) {
					continue;
				}

				if(ann.getType().equals("Sentence")){
					def fm= ann.getFeatures();
					def a = ann.endNode.offset.intValue()
					Sentence s = new Sentence(jcas, ann.startNode.offset.intValue(),ann.endNode.offset.intValue())
					s.addToIndexes(jcas);
				}else if(ann.getType().equals("Token")){
					def fm = ann.getFeatures();
					def isPos = fm.get("category")!=null?true:false;
					def isLemma = fm.get("lemma")!=null?true:false;
					def isStem = fm.get("stem")!=null?true:false;
					def startIndex = ann.startNode.offset.intValue();
					def endIndex = ann.endNode.offset.intValue();

					Token token  = new Token(jcas, startIndex, endIndex);
					token.addToIndexes(jcas);

					if(isPos){
						POS pos = new POS(jcas, startIndex, endIndex)
						pos.addToIndexes(jcas);
					}
					if(isLemma){
						Lemma lemma = new Lemma(jcas, startIndex, endIndex);
						lemma.addToIndexes(jcas);
					}
					if(isStem){
						Stem stem = new Stem(jcas, startIndex, endIndex);
						stem.addToIndexes(jcas);
					}
				}else {
					System.err.printf("Don't know how to handle type: %s%n", ann.getType());
				}
				processed.add(ann.getId());
			}

		}else{
			throw new IllegalArgumentException("Input in the pipeline is not a GATE Document");
		}
		return jcas;
	}
}
