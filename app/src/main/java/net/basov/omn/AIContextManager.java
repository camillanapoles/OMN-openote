/*
Open Markdown Notes (android application to take and organize everyday notes)

Copyright (c) 2024 AI Context Engineering Module

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.basov.omn;

import android.content.Context;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AIContextManager - Manages AI-powered context analysis for notes
 * Provides context engineering capabilities for intelligent note organization
 */
public class AIContextManager {
    
    private Context context;
    private Map<String, List<String>> noteContextMap;
    private Map<String, List<String>> noteRelationships;
    
    public AIContextManager(Context context) {
        this.context = context;
        this.noteContextMap = new HashMap<>();
        this.noteRelationships = new HashMap<>();
    }
    
    /**
     * Analyze note content and extract contextual information
     * @param noteName Name of the note
     * @param noteContent Content to analyze
     * @return List of extracted context keywords
     */
    public List<String> analyzeNoteContext(String noteName, String noteContent) {
        List<String> contextKeywords = new ArrayList<>();
        
        if (noteContent == null || noteContent.isEmpty()) {
            return contextKeywords;
        }
        
        // Simple keyword extraction (can be enhanced with ML models)
        String[] words = noteContent.toLowerCase().split("\\s+");
        Map<String, Integer> wordFrequency = new HashMap<>();
        
        for (String word : words) {
            // Clean word: keep only letters
            StringBuilder cleanWord = new StringBuilder();
            for (char c : word.toCharArray()) {
                if (Character.isLetter(c)) {
                    cleanWord.append(c);
                }
            }
            word = cleanWord.toString();
            
            // Skip short words and common words
            if (word.length() > 3 && !isCommonWord(word)) {
                wordFrequency.put(word, wordFrequency.getOrDefault(word, 0) + 1);
            }
        }
        
        // Get top keywords using streams
        contextKeywords = wordFrequency.entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(10)
            .map(Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toList());
        
        noteContextMap.put(noteName, contextKeywords);
        return contextKeywords;
    }
    
    /**
     * Find related notes based on context similarity
     * @param noteName Name of the note to find relations for
     * @return List of related note names
     */
    public List<String> findRelatedNotes(String noteName) {
        List<String> relatedNotes = new ArrayList<>();
        List<String> currentContext = noteContextMap.get(noteName);
        
        if (currentContext == null || currentContext.isEmpty()) {
            return relatedNotes;
        }
        
        // Compare context with other notes
        for (Map.Entry<String, List<String>> entry : noteContextMap.entrySet()) {
            if (!entry.getKey().equals(noteName)) {
                int commonKeywords = countCommonKeywords(currentContext, entry.getValue());
                if (commonKeywords > 0) {
                    relatedNotes.add(entry.getKey());
                }
            }
        }
        
        noteRelationships.put(noteName, relatedNotes);
        return relatedNotes;
    }
    
    /**
     * Generate AI-powered suggestions for note organization
     * @param noteName Name of the note
     * @return List of suggestions
     */
    public List<String> generateOrganizationSuggestions(String noteName) {
        List<String> suggestions = new ArrayList<>();
        List<String> relatedNotes = findRelatedNotes(noteName);
        
        if (!relatedNotes.isEmpty()) {
            suggestions.add("Notas relacionadas encontradas: " + relatedNotes.size());
            suggestions.add("Considere criar links entre estas notas");
        }
        
        List<String> context = noteContextMap.get(noteName);
        if (context != null && !context.isEmpty()) {
            suggestions.add("Palavras-chave principais: " + String.join(", ", context));
        }
        
        return suggestions;
    }
    
    /**
     * Get context graph data for visualization
     * @return Map of note relationships for graph rendering
     */
    public Map<String, List<String>> getContextGraph() {
        return new HashMap<>(noteRelationships);
    }
    
    private int countCommonKeywords(List<String> list1, List<String> list2) {
        int count = 0;
        for (String keyword : list1) {
            if (list2.contains(keyword)) {
                count++;
            }
        }
        return count;
    }
    
    private boolean isCommonWord(String word) {
        // Common Portuguese and English stop words (expanded list)
        String[] commonWords = {
            // English
            "the", "and", "for", "are", "but", "not", "you", "all", "can", "her", "was", 
            "one", "our", "out", "day", "get", "has", "him", "his", "how", "man", "new",
            "now", "old", "see", "two", "way", "who", "boy", "did", "its", "let", "put",
            "say", "she", "too", "use", "from", "have", "this", "that", "with", "they",
            "will", "your", "what", "were", "when", "more", "then", "than", "been",
            // Portuguese
            "que", "para", "com", "uma", "por", "mais", "ser", "como", "este", "esta", 
            "seu", "sua", "dos", "das", "isso", "esse", "essa", "aquele", "aquela",
            "pelo", "pela", "pelos", "pelas", "estes", "estas", "dele",
            "dela", "deles", "delas", "nele", "nela", "neles", "nelas",
            "esses", "essas", "aqueles", "aquelas", "deste", "desta",
            "destes", "destas", "neste", "nesta", "nestes", "nestas", "desse", "dessa",
            "desses", "dessas", "nesse", "nessa", "nesses", "nessas", "isto",
            "aquilo", "desde", "entre", "sobre", "todos", "todas", "todo", "toda",
            "muito", "muita", "muitos", "muitas", "pouco", "pouca", "poucos", "poucas",
            "cada", "qual", "quais", "onde", "quando", "porque", "portanto",
            "assim", "tambem", "também", "ainda", "sempre", "nunca", "apenas", "somente"
        };
        
        for (String common : commonWords) {
            if (word.equals(common)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Summarize note content using simple extractive summarization
     * @param noteContent Content to summarize
     * @param maxSentences Maximum number of sentences in summary
     * @return Summary text
     */
    public String summarizeNote(String noteContent, int maxSentences) {
        if (noteContent == null || noteContent.isEmpty()) {
            return "";
        }
        
        String[] sentences = noteContent.split("[.!?]\\s+");
        StringBuilder summary = new StringBuilder();
        
        int sentenceCount = Math.min(maxSentences, sentences.length);
        for (int i = 0; i < sentenceCount; i++) {
            summary.append(sentences[i].trim());
            if (i < sentenceCount - 1) {
                summary.append(". ");
            }
        }
        
        return summary.toString();
    }
}
