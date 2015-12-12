---
#
# Use the widgets beneath and the content will be
# inserted automagically in the webpage. To make
# this work, you have to use › layout: frontpage
#
layout: frontpage
title: "DKPro Script"
#header:
#	title: DKPro Core
#   image_fullwidth: "header_unsplash_12.jpg"
header-1:
    title: Building pipelines with DKPro Core as easy as it can get.
    text: DKPro Script provides a DSL to build DKPro Core pipelines with a clean syntax and minimal effort.
---

This example is using an unrelease version of DKPro Core. Once DKPro Core 1.8.0 is released, it will become even shorter.

{% highlight groovy %}
#!/usr/bin/env groovy
@GrabResolver(name='apache-snapshots', 
    root='http://repository.apache.org/snapshots')
@GrabResolver(name='ukp-oss-snapshots',
    root='http://zoidberg.ukp.informatik.tu-darmstadt.de/artifactory/public-snapshots')
@Grab('org.dkpro.script:dkpro-script-groovy:0.0.1-SNAPSHOT')
import groovy.transform.BaseScript
import org.dkpro.script.groovy.DKProCoreScript;
@BaseScript DKProCoreScript baseScript

version '1.8.0-SNAPSHOT'

read 'String' language 'de' params([
    documentText: 'This is a test.'])
apply 'OpenNlpSegmenter'
apply 'OpenNlpPosTagger'
write 'Conll2006'

{% endhighlight %}

How to cite
-----------

There is no dedicated publication on DKPro Script. Please cite the components that you use and DKPro Core as indicated below.

Many of the wrapped third-party components and the models used by them should be cited individually. We currently do not provide a comprehensive overview over citable publications. We encourage you to track down citable publications for these dependencies. However, you might find pointers to some relevant publications in the Model overview of the DKPro Core release you are using or in the JavaDoc of individual components.

Please cite DKPro Core itself as:

> Eckart de Castilho, R. and Gurevych, I. (2014). **A broad-coverage collection of portable NLP components for building shareable analysis pipelines**. In Proceedings of the Workshop on Open Infrastructures and Analysis Frameworks for HLT (OIAF4HLT) at COLING 2014, to be published, Dublin, Ireland.
[(pdf)][1] [(bib)][2]

License
-------

DKPro Script itself is licensed under the [Apache Software License (ASL) version 2][3]. Please respect the licenses of DKPro Core and the integrated components as stated below:

All components in DKPro Core ASL are licensed under the [Apache Software License (ASL) version 2][3] - but their dependencies may not be:

**IMPORTANT LICENSE NOTE** - It must be pointed out that while the component's source code itself is licensed under the ASL, individual components might make use of third-party libraries or products that are not licensed under the ASL, such as LGPL libraries or libraries which are free for research but may not be used in commercial scenarios. Please be aware of the third party licenses and respect them.

[1]: https://www.ukp.tu-darmstadt.de/fileadmin/user_upload/Group_UKP/OIAF4HLT2014DKProCore_cameraready.pdf
[2]: https://www.ukp.tu-darmstadt.de/publications/details/?no_cache=1&tx_bibtex_pi1%5Bpub_id%5D=TUD-CS-2014-0864&type=99&tx_bibtex_pi1%5Bbibtex%5D=yes
[3]: http://www.apache.org/licenses/LICENSE-2.0
