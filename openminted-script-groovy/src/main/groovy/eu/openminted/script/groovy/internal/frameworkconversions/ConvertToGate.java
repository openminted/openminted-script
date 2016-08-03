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

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.text.AnnotationFS;
import org.apache.uima.fit.util.CasUtil;
import org.apache.uima.jcas.JCas;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import gate.AnnotationSet;
import gate.Document;
import gate.FeatureMap;
import gate.corpora.DocumentContentImpl;
import gate.corpora.DocumentImpl;
import gate.util.GateException;
import gate.util.SimpleFeatureMapImpl;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public class ConvertToGate implements ConvertFramework{
	@Override
    public Document convert(Object aJCas) throws GateException {
		
		CAS cas = null;
		if (aJCas instanceof CAS) {
			cas = (CAS) aJCas;
		}
		else if (aJCas instanceof JCas) {
			cas = ((JCas) aJCas).getCas();
		} else{
			  throw new IllegalArgumentException("Input in the pipeline is not a UIMA CAS");			
		}
		
		IntOpenHashSet processed = new IntOpenHashSet();

		Document document = new DocumentImpl();
		document.setContent(new DocumentContentImpl(cas.getDocumentText()));

		AnnotationSet annSet = document.getAnnotations();
		
		for (AnnotationFS fs : CasUtil.selectAll(cas)) {
			if (processed.contains(cas.getLowLevelCAS().ll_getFSRef(fs))) {
				continue;
			}

			if (fs instanceof Token) {
				Token t = (Token) fs;
				FeatureMap fm = new SimpleFeatureMapImpl();
				fm.put("length", t.getCoveredText().length());
				fm.put("string", t.getCoveredText());
				if (t.getPos() != null) {
					fm.put("category", t.getPos().getPosValue());
				}
				if (t.getLemma() != null) {
					fm.put("lemma", t.getLemma().getValue());
				}
				if (t.getStem() != null) {
					fm.put("stem", t.getStem().getValue());
				}
				annSet.add(Long.valueOf(t.getBegin()), Long.valueOf(t.getEnd()), "Token", fm);
			}
			else if (fs instanceof Lemma) {
				// Do nothing - handled as part of Token
			}
			else if (fs instanceof POS) {
				// Do nothing - handled as part of Token
			}
			else if (fs instanceof Sentence) {
				Sentence s = (Sentence) fs;
				FeatureMap fm = new SimpleFeatureMapImpl();
				annSet.add(Long.valueOf(s.getBegin()), Long.valueOf(s.getEnd()), "Sentence", fm);
			}
			else {
				
				System.err.printf("Don't know how to handle type: %s%n", fs.getType().getName());
			}

			processed.add(cas.getLowLevelCAS().ll_getFSRef(fs));
		}

		return document;
	}
}

