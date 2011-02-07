package com.github.rnewson.couchdb.lucene;

/**
 * Copyright 2010 Filip de Waard
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.UUID;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import org.apache.log4j.Logger;

import org.apache.lucene.index.IndexReader;

import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.github.rnewson.couchdb.lucene.util.ServletUtils;

public final class Suggestions {

	private final IndexReader reader;
	private final Logger logger;
	private final Directory dir;

    public Suggestions(final File root, final IndexReader reader) 
            throws IOException {
        this.reader = reader;
		this.logger = Logger.getLogger(Suggestions.class.getName() + ".");
        
        UUID uuid = UUID.randomUUID();
        File directory = new File(root, uuid.toString());
        this.dir = FSDirectory.open(directory);
    }

    public JSONArray suggest(final HttpServletRequest req,
            final HttpServletResponse resp) throws IOException, JSONException {
        final String suggestField = req.getParameter("suggest_field");
        final String term = req.getParameter("term");
		final int limit = Integer.parseInt(req.getParameter("limit"));

        SpellChecker spellChecker = new SpellChecker(this.dir);
        spellChecker.indexDictionary(new LuceneDictionary(this.reader, 
            suggestField));

        String[] suggestions = spellChecker.suggestSimilar(term, limit);

        final JSONArray result = new JSONArray();
        final JSONObject queryRow = new JSONObject();
		final JSONArray rows = new JSONArray();

        if(suggestions!=null && suggestions.length>0) {
            for(String word : suggestions) {
                rows.put(word);
            }
        }

        queryRow.put("rows", rows);
        result.put(queryRow); 

        return result;
    }
}
