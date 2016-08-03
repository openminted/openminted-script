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
package eu.openminted.script.groovy.internal.frameworkconversions;

import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Stem;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.FeatureMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;


class ConvertToUIMA
    implements ConvertFramework
{
	@Override
	public JCas convert(Object data) throws UIMAException {

		TypeSystemDescriptionFactory.forceTypeDescriptorsScan();
		JCas jcas = JCasFactory.createJCas();

		if (data instanceof Document) {
		    Document doc = (Document) data;
		    
			jcas.setDocumentText(doc.getContent().toString());
			AnnotationSet annSet = doc.getAnnotations();
			IntOpenHashSet processed = new IntOpenHashSet();

			for(Annotation ann : annSet){

				if (processed.contains(ann.getId())) {
					continue;
				}

				if(ann.getType().equals("Sentence")){
					FeatureMap fm = ann.getFeatures();
					int a = ann.getEndNode().getOffset().intValue();
					Sentence s = new Sentence(jcas, ann.getStartNode().getOffset().intValue(),
					        ann.getEndNode().getOffset().intValue());
					s.addToIndexes(jcas);
				} 
				else if(ann.getType().equals("Token")) {
				    FeatureMap  fm = ann.getFeatures();
					boolean isPos = fm.get("category") != null;
					boolean isLemma = fm.get("lemma") != null;
					boolean isStem = fm.get("stem") != null;
					int startIndex = ann.getStartNode().getOffset().intValue();
					int endIndex = ann.getEndNode().getOffset().intValue();

					Token token  = new Token(jcas, startIndex, endIndex);
					token.addToIndexes(jcas);

					if (isPos) {
						POS pos = new POS(jcas, startIndex, endIndex);
						pos.addToIndexes(jcas);
					}
					if (isLemma) {
						Lemma lemma = new Lemma(jcas, startIndex, endIndex);
						lemma.addToIndexes(jcas);
					}
					if (isStem) {
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
