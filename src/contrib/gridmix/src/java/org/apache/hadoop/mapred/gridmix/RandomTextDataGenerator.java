/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.mapred.gridmix;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.RandomStringUtils;

/**
 * A random text generator. The words are simply sequences of alphabets.
 */
class RandomTextDataGenerator {
  /**
   * Random words list size.
   */
  static final String GRIDMIX_DATAGEN_RANDOMTEXT_LISTSIZE = 
    "gridmix.datagenerator.randomtext.listsize";
  
  /**
   * Random words size.
   */
  static final String GRIDMIX_DATAGEN_RANDOMTEXT_WORDSIZE = 
    "gridmix.datagenerator.randomtext.wordsize";
  
  /**
   * A list of random words
   */
  private String[] words;
  private Random random;
  
  /**
   * Constructor for {@link RandomTextDataGenerator}.
   * @param size the total number of words to consider.
   * @param seed Random number generator seed for repeatability
   * @param wordSize Size of each word
   */
  RandomTextDataGenerator(int size, Long seed, int wordSize) {
    if (seed == null) {
      random = new Random();
    } else {
      random = new Random(seed);
    }
    words = new String[size];
    //TODO change the default with the actual stats
    //TODO do u need varied sized words?
    for (int i = 0; i < size; ++i) {
      words[i] = 
        RandomStringUtils.random(wordSize, 0, 0, true, false, null, random);
    }
  }
  
  /**
   * Returns a randomly selected word from a list of random words.
   */
  String getRandomWord() {
    int index = random.nextInt(words.length);
    return words[index];
  }
  
  /**
   * This is mainly for testing.
   */
  List<String> getRandomWords() {
    return Arrays.asList(words);
  }
}