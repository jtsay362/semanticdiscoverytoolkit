/*
    Copyright 2009 Semantic Discovery, Inc. (www.semanticdiscovery.com)

    This file is part of the Semantic Discovery Toolkit.

    The Semantic Discovery Toolkit is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    The Semantic Discovery Toolkit is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with The Semantic Discovery Toolkit.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.sd.classifier;


import org.sd.extract.TextContainer;

import java.io.IOException;
import java.util.Properties;

/**
 * Abstract base class for classifier runners and trainers.
 * <p>
 * @author Spence Koehler
 */
public abstract class BaseClassifierRunner {

  private FeatureDictionary featureDictionary;
  private FeatureExtractor featureExtractor;

  protected BaseClassifierRunner(FeatureDictionary featureDictionary, FeatureExtractor featureExtractor) {
    this.featureDictionary = featureDictionary;
    this.featureExtractor = featureExtractor;
  }

  /**
   * Properties-based constructor.
   * <p>
   * Builds a feature dictionary and a feature extractor directly from the properties.
   */
  protected BaseClassifierRunner(Properties properties) throws IOException {
    this.featureDictionary = new FeatureDictionary(properties);
    this.featureExtractor = null; //todo: new PrimaryFeatureExtractor(properties);
  }
  

  /**
   * Get the populated feature vector generated by running the input through the extractor.
   */
  protected final FeatureVector extract(TextContainer input) {
    final FeatureVector result = new FeatureVector();

    result.setExtractionFlag(featureExtractor.process(result, input, featureDictionary));

    return result;
  }

  /**
   * Get this instance's feature dictionary.
   */
  public FeatureDictionary getFeatureDictionary() {
    return featureDictionary;
  }

  /**
   * Get this instance's feature extractor.
   */
  public FeatureExtractor getFeatureExtractor() {
    return featureExtractor;
  }
}
