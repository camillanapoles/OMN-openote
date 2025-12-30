/**
 * AI Context and Graph Visualization JavaScript Module
 * Provides client-side functionality for AI context analysis and graph visualization
 */

// Global variables
var aiGraphModule = (function() {
    'use strict';
    
    var currentPageName = '';
    var currentPageContent = '';
    
    /**
     * Initialize AI and Graph features for a page
     */
    function init(pageName, pageContent) {
        currentPageName = pageName;
        currentPageContent = pageContent;
        
        // Check if Android interface is available
        if (typeof Android === 'undefined') {
            console.log('Android interface not available');
            return;
        }
        
        // Load AI context and graph data
        loadAIContext();
        loadGraphVisualization();
    }
    
    /**
     * Load and display AI context suggestions
     */
    function loadAIContext() {
        try {
            var suggestions = Android.getAIContextSuggestions(currentPageName, currentPageContent);
            if (suggestions && suggestions.length > 0) {
                displayAIContext(suggestions);
            }
            
            var relatedNotes = Android.getRelatedNotes(currentPageName);
            if (relatedNotes && relatedNotes.length > 0) {
                displayRelatedNotes(relatedNotes.split(','));
            }
        } catch (e) {
            console.error('Error loading AI context:', e);
        }
    }
    
    /**
     * Display AI context suggestions in the page
     */
    function displayAIContext(suggestions) {
        var container = document.getElementById('ai-context-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'ai-context-container';
            container.className = 'ai-context-container';
            
            var content = document.getElementById('content');
            if (content) {
                content.appendChild(container);
            }
        }
        
        var html = '<h3>🤖 Análise de Contexto AI</h3>';
        html += '<div class="ai-suggestions">';
        html += '<ul>';
        
        var suggestionLines = suggestions.split('\n');
        for (var i = 0; i < suggestionLines.length; i++) {
            if (suggestionLines[i].trim()) {
                html += '<li>' + escapeHtml(suggestionLines[i]) + '</li>';
            }
        }
        
        html += '</ul></div>';
        container.innerHTML = html;
    }
    
    /**
     * Display related notes
     */
    function displayRelatedNotes(relatedNotes) {
        var container = document.getElementById('ai-context-container');
        if (!container) return;
        
        if (relatedNotes.length > 0) {
            var html = '<div class="related-notes">';
            html += '<h4>📚 Notas Relacionadas:</h4>';
            
            for (var i = 0; i < relatedNotes.length; i++) {
                var noteName = relatedNotes[i].trim();
                if (noteName) {
                    html += '<a href="' + noteName + '.html">' + escapeHtml(noteName) + '</a>';
                }
            }
            
            html += '</div>';
            container.innerHTML += html;
        }
    }
    
    /**
     * Load and display graph visualization
     */
    function loadGraphVisualization() {
        try {
            var graphHTML = Android.getGraphVisualization(currentPageName, currentPageContent);
            if (graphHTML && graphHTML.length > 0) {
                displayGraph(graphHTML);
            }
        } catch (e) {
            console.error('Error loading graph visualization:', e);
        }
    }
    
    /**
     * Display graph visualization in the page
     */
    function displayGraph(graphHTML) {
        var container = document.getElementById('graph-container');
        if (!container) {
            container = document.createElement('div');
            container.id = 'graph-container';
            
            var content = document.getElementById('content');
            if (content) {
                content.appendChild(container);
            }
        }
        
        container.innerHTML = graphHTML;
    }
    
    /**
     * Toggle AI context visibility
     */
    function toggleAIContext() {
        var container = document.getElementById('ai-context-container');
        if (container) {
            container.style.display = container.style.display === 'none' ? 'block' : 'none';
        }
    }
    
    /**
     * Toggle graph visualization visibility
     */
    function toggleGraph() {
        var container = document.getElementById('graph-container');
        if (container) {
            container.style.display = container.style.display === 'none' ? 'block' : 'none';
        }
    }
    
    /**
     * Escape HTML to prevent XSS
     */
    function escapeHtml(text) {
        var map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return text.replace(/[&<>"']/g, function(m) { return map[m]; });
    }
    
    // Public API
    return {
        init: init,
        toggleAIContext: toggleAIContext,
        toggleGraph: toggleGraph,
        loadAIContext: loadAIContext,
        loadGraphVisualization: loadGraphVisualization
    };
})();

// Auto-initialize when page loads
if (typeof PageName !== 'undefined' && PageName) {
    // Wait for page to be fully loaded
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function() {
            // Get page content from the markdown div if it exists
            var contentDiv = document.getElementById('content');
            var pageContent = contentDiv ? contentDiv.innerText : '';
            aiGraphModule.init(PageName, pageContent);
        });
    } else {
        var contentDiv = document.getElementById('content');
        var pageContent = contentDiv ? contentDiv.innerText : '';
        aiGraphModule.init(PageName, pageContent);
    }
}
