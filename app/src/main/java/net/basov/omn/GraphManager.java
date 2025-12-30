/*
Open Markdown Notes (android application to take and organize everyday notes)

Copyright (c) 2024 Graph Visualization Module

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.basov.omn;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GraphManager - Manages note relationship graphs and visualizations
 * Provides graph-based navigation and visualization of note connections
 */
public class GraphManager {
    
    private Context context;
    private Map<String, List<String>> noteLinks; // note -> linked notes
    private Map<String, List<String>> backlinks;  // note -> notes that link to it
    
    public GraphManager(Context context) {
        this.context = context;
        this.noteLinks = new HashMap<>();
        this.backlinks = new HashMap<>();
    }
    
    /**
     * Parse note content and extract links to other notes
     * @param noteName Name of the current note
     * @param noteContent Content to parse
     * @return List of linked note names
     */
    public List<String> extractNoteLinks(String noteName, String noteContent) {
        List<String> links = new ArrayList<>();
        
        if (noteContent == null || noteContent.isEmpty()) {
            return links;
        }
        
        // Pattern for Markdown links: [text](page.html) or [text](page.md)
        Pattern linkPattern = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+\\.(?:html|md))\\)");
        Matcher matcher = linkPattern.matcher(noteContent);
        
        while (matcher.find()) {
            String linkedPage = matcher.group(2);
            // Remove extension and path
            linkedPage = linkedPage.replaceAll("\\.html$|\\.md$", "");
            linkedPage = linkedPage.replaceAll(".*/", "");
            
            if (!links.contains(linkedPage)) {
                links.add(linkedPage);
            }
        }
        
        // Update link maps
        noteLinks.put(noteName, links);
        
        // Update backlinks
        for (String linkedNote : links) {
            if (!backlinks.containsKey(linkedNote)) {
                backlinks.put(linkedNote, new ArrayList<>());
            }
            if (!backlinks.get(linkedNote).contains(noteName)) {
                backlinks.get(linkedNote).add(noteName);
            }
        }
        
        return links;
    }
    
    /**
     * Get all notes that link to the specified note (backlinks)
     * @param noteName Name of the note
     * @return List of notes that link to this note
     */
    public List<String> getBacklinks(String noteName) {
        return backlinks.getOrDefault(noteName, new ArrayList<>());
    }
    
    /**
     * Get all notes that are linked from the specified note (forward links)
     * @param noteName Name of the note
     * @return List of notes linked from this note
     */
    public List<String> getForwardLinks(String noteName) {
        return noteLinks.getOrDefault(noteName, new ArrayList<>());
    }
    
    /**
     * Generate graph data in JSON format for visualization
     * @return JSON string representing the graph
     */
    public String generateGraphJSON() {
        try {
            JSONObject graph = new JSONObject();
            JSONArray nodes = new JSONArray();
            JSONArray edges = new JSONArray();
            
            // Create nodes
            Map<String, Integer> nodeIndexMap = new HashMap<>();
            int index = 0;
            
            for (String noteName : noteLinks.keySet()) {
                JSONObject node = new JSONObject();
                node.put("id", noteName);
                node.put("label", noteName);
                node.put("index", index);
                nodes.put(node);
                nodeIndexMap.put(noteName, index);
                index++;
            }
            
            // Add any linked notes that aren't in the map yet
            for (List<String> links : noteLinks.values()) {
                for (String linkedNote : links) {
                    if (!nodeIndexMap.containsKey(linkedNote)) {
                        JSONObject node = new JSONObject();
                        node.put("id", linkedNote);
                        node.put("label", linkedNote);
                        node.put("index", index);
                        nodes.put(node);
                        nodeIndexMap.put(linkedNote, index);
                        index++;
                    }
                }
            }
            
            // Create edges
            for (Map.Entry<String, List<String>> entry : noteLinks.entrySet()) {
                String source = entry.getKey();
                for (String target : entry.getValue()) {
                    JSONObject edge = new JSONObject();
                    edge.put("source", nodeIndexMap.get(source));
                    edge.put("target", nodeIndexMap.get(target));
                    edges.put(edge);
                }
            }
            
            graph.put("nodes", nodes);
            graph.put("edges", edges);
            
            return graph.toString();
        } catch (JSONException e) {
            return "{}";
        }
    }
    
    /**
     * Generate HTML for graph visualization using D3.js or similar
     * @param currentNoteName Current note to highlight
     * @return HTML string with embedded graph visualization
     */
    public String generateGraphHTML(String currentNoteName) {
        StringBuilder html = new StringBuilder();
        html.append("<div class='graph-container'>\n");
        html.append("<h3>Grafo de Relacionamentos de Notas</h3>\n");
        
        // Show backlinks
        List<String> backlinks = getBacklinks(currentNoteName);
        if (!backlinks.isEmpty()) {
            html.append("<div class='backlinks-section'>\n");
            html.append("<h4>Notas que referenciam esta (").append(backlinks.size()).append("):</h4>\n");
            html.append("<ul>\n");
            for (String backlink : backlinks) {
                html.append("<li><a href='").append(backlink).append(".html'>")
                    .append(backlink).append("</a></li>\n");
            }
            html.append("</ul>\n");
            html.append("</div>\n");
        }
        
        // Show forward links
        List<String> forwardLinks = getForwardLinks(currentNoteName);
        if (!forwardLinks.isEmpty()) {
            html.append("<div class='forwardlinks-section'>\n");
            html.append("<h4>Notas referenciadas (").append(forwardLinks.size()).append("):</h4>\n");
            html.append("<ul>\n");
            for (String link : forwardLinks) {
                html.append("<li><a href='").append(link).append(".html'>")
                    .append(link).append("</a></li>\n");
            }
            html.append("</ul>\n");
            html.append("</div>\n");
        }
        
        // Add placeholder for interactive graph visualization
        html.append("<div id='graph-viz' class='graph-visualization'>\n");
        html.append("<p>Visualização interativa do grafo (requer JavaScript)</p>\n");
        html.append("</div>\n");
        
        html.append("<script>\n");
        html.append("var graphData = ").append(generateGraphJSON()).append(";\n");
        html.append("// Graph rendering code can be added here\n");
        html.append("</script>\n");
        
        html.append("</div>\n");
        
        return html.toString();
    }
    
    /**
     * Get all notes in the graph
     * @return List of all note names
     */
    public List<String> getAllNotes() {
        List<String> allNotes = new ArrayList<>(noteLinks.keySet());
        // Add any notes that are only referenced but don't have content yet
        for (List<String> links : noteLinks.values()) {
            for (String link : links) {
                if (!allNotes.contains(link)) {
                    allNotes.add(link);
                }
            }
        }
        return allNotes;
    }
    
    /**
     * Clear all graph data
     */
    public void clearGraph() {
        noteLinks.clear();
        backlinks.clear();
    }
}
