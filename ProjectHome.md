**NEWS: JATE 2.0 Alpha (UPDATE ON 09 MARCH 2014) - A major update of JATE (ver 2.0Alpha) is available at https://jatetoolkit.googlecode.com/svn/trunk/2.0Alpha. This is still under testing and users have to use them at your own risk. The previous 1.11 stable version is available at https://jatetoolkit.googlecode.com/svn/trunk/1.11**

New algorithms implemented:
  * Justeson & Katz Algorithm. Algorithm referred from: Justeson, J. S., & Katz, S. M. (1995). Technical terminology: some linguistic properties and an algorithm for identification in text. Natural language engineering, 1(1), 9-27
  * NC – Value Algorithm. Referred from: Frantzi, K., Ananiadou, S., & Mima, H. (2000). Automatic recognition of multi-word terms:. the C-value/NC-value method. International Journal on Digital Libraries, 3(2), 115-130.
  * RAKE Algorithm. Refered from: Rose, S., Engel, D., Cramer, N., & Cowley, W. (2010). Automatic keyword extraction from individual documents. Text Mining, 1-20.
  * Chi-squared Algorithm. Referred from: Matsuo, Y., & Ishizuka, M. (2004). Keyword extraction from a single document using word co-occurrence statistical information. International Journal on Artificial Intelligence Tools, 13(01), 157-169.

**Many thanks to the following contributors from Accenture who have contributed to the implementation of the above algorithms:
  * Anurag Dwarakanath
  * Aniya Aggarwal
  * Roshni R Ramnani
  * Ankit Verma
  * priyatechit**


JATE (Java Automatic Term Extraction) toolkit is a library with implementation of several state-of-the-art term extraction algorithms. It also provides a generic development and evaluation framework for implementing new term extraction algorithms. JATE is built based on the previous JATR library described in this paper:

Zhang, Z., Iria, J., Brewster, C., and Ciravegna, F. (2008). A Comparative Evaluation of Term Recognition Algorithms. In Proceedings of The sixth international conference on Language Resources and Evaluation, (LREC 2008), May 28-31, 2008, Marrakech, Morocco.

The extension of the tool described in the above paper and its release is funded under the EC 7th Framework Program, in the context of the SmartProducts project (231204).

Term extraction deals with identifying statistically representative terms from a domain specific corpus, but without classifying them. It is a technique used often for content summarization, indexing, and information extraction.

Currently the v1.1 library has implemented 8 state-of-the-art algorithms:
  * basic term frequency
  * Average term frequency in the corpus (term frequency/ document frequency)
  * TF-IDF
  * RIDF (see Church, K. and Gale, W. 1995a. Inverse Document Frequency (IDF): A Measure of Deviation from Poisson. In Proceedings of the 3rd Workshop on Very Large Corpora. Cambridge, Massachusetts, USA, pp.121-30.)
  * Weirdness (see Ahmad, K.; Gillam, L.; and Tostevin, L. 1999.University of surrey participation in trec8: Weirdness indexing for logical document extrapolation andretrieval (wilder). In The Eighth Text REtrieval Conference)
  * C-value (see Ananiadou, S. 1994. A methodology for automatic term recognition. In COLING 15th International Conference on Computational Linguistics, 1034–1038.)
  * GlossEx (see Kozakov, L.; Park, Y.; Fin, T.-H.; Drissi, Y.; Doganata,Y. N.; and Cofino, T. 2004. Glossary extraction and knowledge in large organisations via semantic web technologies. In Proceedings of the 6th International Semantic Web Conference and the 2nd Asian Semantic Web Conference).
  * TermEx (see Sclano, F., and Velardi, P. 2007. Termextractor: a web application to learn the shared terminology of emergentweb communities. In Proceedings of the 3rd International Conference on Interoperability for Enterprise Software and Applications).

The 2.0 Alpha version currently under test adds four more algorithms:

  * Justeson & Katz Algorithm. Algorithm referred from: Justeson, J. S., & Katz, S. M. (1995). Technical terminology: some linguistic properties and an algorithm for identification in text. Natural language engineering, 1(1), 9-27
  * NC – Value Algorithm. Referred from: Frantzi, K., Ananiadou, S., & Mima, H. (2000). Automatic recognition of multi-word terms:. the C-value/NC-value method. International Journal on Digital Libraries, 3(2), 115-130.
  * RAKE Algorithm. Refered from: Rose, S., Engel, D., Cramer, N., & Cowley, W. (2010). Automatic keyword extraction from individual documents. Text Mining, 1-20.
  * Chi-squared Algorithm. Referred from: Matsuo, Y., & Ishizuka, M. (2004). Keyword extraction from a single document using word co-occurrence statistical information. International Journal on Artificial Intelligence Tools, 13(01), 157-169.