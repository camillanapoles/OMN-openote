/**
 * DocumentManager.js - Multi-document management for Open Markdown Notes
 * Provides UI interactions for document organization, search, and batch operations
 * 
 * TODO: Replace prompt() dialogs with custom modal dialogs for better UX
 *       Current implementation uses browser prompt() for simplicity and compatibility
 *       Future versions should implement custom modal dialogs matching app design
 */

var DocumentManagerUI = (function() {
    'use strict';
    
    var docManager = {
        currentView: 'all',
        selectedDocs: [],
        searchTimeout: null
    };
    
    /**
     * Initialize document manager UI
     */
    docManager.init = function() {
        console.log('DocumentManager UI initialized');
        this.setupEventListeners();
        this.loadRecentDocuments();
    };
    
    /**
     * Setup event listeners for document management
     */
    docManager.setupEventListeners = function() {
        // Search input handler with debounce
        var searchInput = document.getElementById('doc-search');
        if (searchInput) {
            searchInput.addEventListener('input', function(e) {
                clearTimeout(docManager.searchTimeout);
                docManager.searchTimeout = setTimeout(function() {
                    docManager.performSearch(e.target.value);
                }, 300);
            });
        }
        
        // Category filter
        var categoryFilter = document.getElementById('category-filter');
        if (categoryFilter) {
            categoryFilter.addEventListener('change', function(e) {
                docManager.filterByCategory(e.target.value);
            });
        }
        
        // Batch operation buttons
        var batchCopyBtn = document.getElementById('batch-copy');
        if (batchCopyBtn) {
            batchCopyBtn.addEventListener('click', function() {
                docManager.batchCopy();
            });
        }
        
        var batchMoveBtn = document.getElementById('batch-move');
        if (batchMoveBtn) {
            batchMoveBtn.addEventListener('click', function() {
                docManager.batchMove();
            });
        }
        
        var batchExportBtn = document.getElementById('batch-export');
        if (batchExportBtn) {
            batchExportBtn.addEventListener('click', function() {
                docManager.batchExport();
            });
        }
    };
    
    /**
     * Load and display recent documents
     */
    docManager.loadRecentDocuments = function() {
        try {
            if (typeof Android !== 'undefined' && Android.getRecentDocuments) {
                var recentDocsJson = Android.getRecentDocuments(10);
                var recentDocs = JSON.parse(recentDocsJson);
                this.displayDocuments(recentDocs, 'recent-docs-list');
            }
        } catch (e) {
            console.error('Error loading recent documents:', e);
        }
    };
    
    /**
     * Perform document search
     */
    docManager.performSearch = function(keyword) {
        if (!keyword || keyword.trim().length === 0) {
            this.loadRecentDocuments();
            return;
        }
        
        try {
            if (typeof Android !== 'undefined' && Android.searchDocuments) {
                var resultsJson = Android.searchDocuments(keyword);
                var results = JSON.parse(resultsJson);
                this.displayDocuments(results, 'search-results-list');
            }
        } catch (e) {
            console.error('Error searching documents:', e);
        }
    };
    
    /**
     * Filter documents by category
     */
    docManager.filterByCategory = function(category) {
        try {
            if (typeof Android !== 'undefined' && Android.getDocumentsByCategory) {
                var categorizedJson = Android.getDocumentsByCategory();
                var categorized = JSON.parse(categorizedJson);
                
                if (category === 'all') {
                    var allDocs = [];
                    for (var cat in categorized) {
                        allDocs = allDocs.concat(categorized[cat]);
                    }
                    this.displayDocuments(allDocs, 'filtered-docs-list');
                } else {
                    this.displayDocuments(categorized[category] || [], 'filtered-docs-list');
                }
            }
        } catch (e) {
            console.error('Error filtering by category:', e);
        }
    };
    
    /**
     * Display documents in a list
     */
    docManager.displayDocuments = function(documents, containerId) {
        var container = document.getElementById(containerId);
        if (!container) {
            console.warn('Container not found:', containerId);
            return;
        }
        
        container.innerHTML = '';
        
        if (!documents || documents.length === 0) {
            container.innerHTML = '<div class="no-results">No documents found</div>';
            return;
        }
        
        documents.forEach(function(doc) {
            var docItem = document.createElement('div');
            docItem.className = 'document-item';
            docItem.dataset.path = doc.path;
            
            var checkbox = document.createElement('input');
            checkbox.type = 'checkbox';
            checkbox.className = 'doc-checkbox';
            checkbox.addEventListener('change', function(e) {
                docManager.toggleDocumentSelection(doc.path, e.target.checked);
            });
            
            var docInfo = document.createElement('div');
            docInfo.className = 'doc-info';
            
            var docName = document.createElement('div');
            docName.className = 'doc-name';
            docName.textContent = doc.name;
            docName.addEventListener('click', function() {
                docManager.openDocument(doc.path);
            });
            
            var docMeta = document.createElement('div');
            docMeta.className = 'doc-meta';
            
            var lastModified = new Date(doc.lastModified);
            var formattedDate = lastModified.toLocaleDateString() + ' ' + lastModified.toLocaleTimeString();
            
            docMeta.textContent = formattedDate + ' • ' + docManager.formatSize(doc.size);
            
            if (doc.category) {
                var category = document.createElement('span');
                category.className = 'doc-category';
                category.textContent = ' • ' + doc.category;
                docMeta.appendChild(category);
            }
            
            docInfo.appendChild(docName);
            docInfo.appendChild(docMeta);
            
            docItem.appendChild(checkbox);
            docItem.appendChild(docInfo);
            
            container.appendChild(docItem);
        });
    };
    
    /**
     * Toggle document selection for batch operations
     */
    docManager.toggleDocumentSelection = function(path, selected) {
        var index = this.selectedDocs.indexOf(path);
        
        if (selected && index === -1) {
            this.selectedDocs.push(path);
        } else if (!selected && index !== -1) {
            this.selectedDocs.splice(index, 1);
        }
        
        this.updateBatchOperationButtons();
    };
    
    /**
     * Update batch operation button states
     */
    docManager.updateBatchOperationButtons = function() {
        var buttons = document.querySelectorAll('.batch-operation-btn');
        buttons.forEach(function(btn) {
            btn.disabled = docManager.selectedDocs.length === 0;
        });
    };
    
    /**
     * Batch copy selected documents
     */
    docManager.batchCopy = function() {
        if (this.selectedDocs.length === 0) {
            return;
        }
        
        var destination = prompt('Enter destination folder name:');
        if (!destination) {
            return;
        }
        
        try {
            if (typeof Android !== 'undefined' && Android.copyDocuments) {
                var success = Android.copyDocuments(JSON.stringify(this.selectedDocs), destination);
                if (success) {
                    alert('Documents copied successfully!');
                    this.clearSelection();
                } else {
                    alert('Failed to copy some documents.');
                }
            }
        } catch (e) {
            console.error('Error copying documents:', e);
            alert('Error copying documents: ' + e.message);
        }
    };
    
    /**
     * Batch move selected documents
     */
    docManager.batchMove = function() {
        if (this.selectedDocs.length === 0) {
            return;
        }
        
        var destination = prompt('Enter destination folder name:');
        if (!destination) {
            return;
        }
        
        try {
            if (typeof Android !== 'undefined' && Android.moveDocuments) {
                var success = Android.moveDocuments(JSON.stringify(this.selectedDocs), destination);
                if (success) {
                    alert('Documents moved successfully!');
                    this.clearSelection();
                    this.loadRecentDocuments(); // Refresh view
                } else {
                    alert('Failed to move some documents.');
                }
            }
        } catch (e) {
            console.error('Error moving documents:', e);
            alert('Error moving documents: ' + e.message);
        }
    };
    
    /**
     * Batch export selected documents
     */
    docManager.batchExport = function() {
        if (this.selectedDocs.length === 0) {
            return;
        }
        
        var exportFolder = prompt('Enter export folder name:', 'OMN_Export');
        if (!exportFolder) {
            return;
        }
        
        try {
            if (typeof Android !== 'undefined' && Android.exportDocuments) {
                var success = Android.exportDocuments(JSON.stringify(this.selectedDocs), exportFolder);
                if (success) {
                    alert('Documents exported successfully to Documents/' + exportFolder);
                    this.clearSelection();
                } else {
                    alert('Failed to export some documents.');
                }
            }
        } catch (e) {
            console.error('Error exporting documents:', e);
            alert('Error exporting documents: ' + e.message);
        }
    };
    
    /**
     * Clear document selection
     */
    docManager.clearSelection = function() {
        this.selectedDocs = [];
        var checkboxes = document.querySelectorAll('.doc-checkbox');
        checkboxes.forEach(function(cb) {
            cb.checked = false;
        });
        this.updateBatchOperationButtons();
    };
    
    /**
     * Open a document
     */
    docManager.openDocument = function(path) {
        try {
            if (typeof Android !== 'undefined' && Android.openDocument) {
                Android.openDocument(path);
            } else {
                // Fallback to navigation
                window.location.href = 'omn://' + path;
            }
        } catch (e) {
            console.error('Error opening document:', e);
        }
    };
    
    /**
     * Format file size for display
     */
    docManager.formatSize = function(bytes) {
        if (bytes < 1024) {
            return bytes + ' B';
        } else if (bytes < 1024 * 1024) {
            return (bytes / 1024).toFixed(1) + ' KB';
        } else {
            return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
        }
    };
    
    /**
     * Get document statistics
     */
    docManager.getStatistics = function() {
        try {
            if (typeof Android !== 'undefined' && Android.getDocumentStatistics) {
                var statsJson = Android.getDocumentStatistics();
                var stats = JSON.parse(statsJson);
                this.displayStatistics(stats);
            }
        } catch (e) {
            console.error('Error getting statistics:', e);
        }
    };
    
    /**
     * Display document statistics
     */
    docManager.displayStatistics = function(stats) {
        var container = document.getElementById('doc-statistics');
        if (!container) {
            return;
        }
        
        container.innerHTML = '<h3>Document Statistics</h3>' +
            '<div class="stat-item">Total Documents: ' + stats.totalDocuments + '</div>' +
            '<div class="stat-item">Total Size: ' + this.formatSize(stats.totalSize) + '</div>' +
            '<div class="stat-item">Categories: ' + stats.totalCategories + '</div>' +
            '<div class="stat-item">Tags: ' + stats.totalTags + '</div>';
    };
    
    return docManager;
})();

// Auto-initialize if DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function() {
        DocumentManagerUI.init();
    });
} else {
    DocumentManagerUI.init();
}
