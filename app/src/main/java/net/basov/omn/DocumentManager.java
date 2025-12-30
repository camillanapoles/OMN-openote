/*
Open Markdown Notes (android application to take and organize everyday notes)

Copyright (c) 2017-2024 Mikhail Basov (https://github.com/mvbasov/OMN)

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.basov.omn;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * DocumentManager - Enhanced multi-document management for Open Markdown Notes
 * Provides organization, search, filtering, and batch operations for markdown documents
 */
public class DocumentManager {
    
    private Context context;
    private File notesDirectory;
    private Map<String, Set<String>> documentTags;
    private Map<String, DocumentInfo> documentCache;
    
    public static class DocumentInfo {
        public String path;
        public String name;
        public long lastModified;
        public long size;
        public Set<String> tags;
        public String category;
        
        public DocumentInfo(String path, String name, long lastModified, long size) {
            this.path = path;
            this.name = name;
            this.lastModified = lastModified;
            this.size = size;
            this.tags = new HashSet<>();
            this.category = "";
        }
    }
    
    public DocumentManager(Context context, File notesDirectory) {
        this.context = context;
        this.notesDirectory = notesDirectory;
        this.documentTags = new HashMap<>();
        this.documentCache = new HashMap<>();
    }
    
    /**
     * Search documents by keyword in filename or content
     */
    public List<DocumentInfo> searchDocuments(String keyword) {
        List<DocumentInfo> results = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return results;
        }
        
        String searchTerm = keyword.toLowerCase(Locale.getDefault());
        searchInDirectory(notesDirectory, searchTerm, results);
        
        return results;
    }
    
    private void searchInDirectory(File directory, String keyword, List<DocumentInfo> results) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return;
        }
        
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                searchInDirectory(file, keyword, results);
            } else if (file.getName().endsWith(".md")) {
                if (file.getName().toLowerCase(Locale.getDefault()).contains(keyword)) {
                    results.add(createDocumentInfo(file));
                }
            }
        }
    }
    
    /**
     * Get all documents organized by category/folder
     */
    public Map<String, List<DocumentInfo>> getDocumentsByCategory() {
        Map<String, List<DocumentInfo>> categorized = new HashMap<>();
        categorizeDirectory(notesDirectory, "", categorized);
        return categorized;
    }
    
    private void categorizeDirectory(File directory, String category, Map<String, List<DocumentInfo>> categorized) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return;
        }
        
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        
        String currentCategory = category.isEmpty() ? "Root" : category;
        
        for (File file : files) {
            if (file.isDirectory()) {
                String subCategory = category.isEmpty() ? file.getName() : category + "/" + file.getName();
                categorizeDirectory(file, subCategory, categorized);
            } else if (file.getName().endsWith(".md")) {
                if (!categorized.containsKey(currentCategory)) {
                    categorized.put(currentCategory, new ArrayList<DocumentInfo>());
                }
                DocumentInfo info = createDocumentInfo(file);
                info.category = currentCategory;
                categorized.get(currentCategory).add(info);
            }
        }
    }
    
    /**
     * Get recently modified documents
     */
    public List<DocumentInfo> getRecentDocuments(int limit) {
        List<DocumentInfo> allDocs = new ArrayList<>();
        collectAllDocuments(notesDirectory, allDocs);
        
        // Sort by last modified date (newest first)
        Collections.sort(allDocs, new Comparator<DocumentInfo>() {
            @Override
            public int compare(DocumentInfo d1, DocumentInfo d2) {
                return Long.compare(d2.lastModified, d1.lastModified);
            }
        });
        
        // Return only the specified limit
        if (allDocs.size() > limit) {
            return allDocs.subList(0, limit);
        }
        return allDocs;
    }
    
    private void collectAllDocuments(File directory, List<DocumentInfo> documents) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return;
        }
        
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            if (file.isDirectory()) {
                collectAllDocuments(file, documents);
            } else if (file.getName().endsWith(".md")) {
                documents.add(createDocumentInfo(file));
            }
        }
    }
    
    /**
     * Batch operation: Copy multiple documents to a folder
     */
    public boolean copyDocuments(List<String> sourcePaths, String destinationFolder) {
        File destDir = new File(notesDirectory, destinationFolder);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        
        boolean allSuccess = true;
        for (String sourcePath : sourcePaths) {
            File sourceFile = new File(notesDirectory, sourcePath);
            File destFile = new File(destDir, sourceFile.getName());
            
            try {
                copyFile(sourceFile, destFile);
            } catch (IOException e) {
                allSuccess = false;
            }
        }
        
        return allSuccess;
    }
    
    /**
     * Batch operation: Move multiple documents to a folder
     */
    public boolean moveDocuments(List<String> sourcePaths, String destinationFolder) {
        File destDir = new File(notesDirectory, destinationFolder);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        
        boolean allSuccess = true;
        for (String sourcePath : sourcePaths) {
            File sourceFile = new File(notesDirectory, sourcePath);
            File destFile = new File(destDir, sourceFile.getName());
            
            if (!sourceFile.renameTo(destFile)) {
                allSuccess = false;
            }
        }
        
        return allSuccess;
    }
    
    /**
     * Export documents to external storage
     */
    public boolean exportDocuments(List<String> sourcePaths, String exportFolderName) {
        File externalDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        File exportDir = new File(externalDir, exportFolderName);
        
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        
        boolean allSuccess = true;
        for (String sourcePath : sourcePaths) {
            File sourceFile = new File(notesDirectory, sourcePath);
            File destFile = new File(exportDir, sourceFile.getName());
            
            try {
                copyFile(sourceFile, destFile);
            } catch (IOException e) {
                allSuccess = false;
            }
        }
        
        return allSuccess;
    }
    
    /**
     * Create a document from template
     */
    public boolean createFromTemplate(String templatePath, String newDocumentName) {
        File templateFile = new File(notesDirectory, templatePath);
        if (!templateFile.exists()) {
            return false;
        }
        
        File newFile = new File(notesDirectory, newDocumentName);
        try {
            copyFile(templateFile, newFile);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Tag management: Add tag to document
     */
    public void addTag(String documentPath, String tag) {
        if (!documentTags.containsKey(documentPath)) {
            documentTags.put(documentPath, new HashSet<String>());
        }
        documentTags.get(documentPath).add(tag);
        
        // Update cache if exists
        if (documentCache.containsKey(documentPath)) {
            documentCache.get(documentPath).tags.add(tag);
        }
    }
    
    /**
     * Tag management: Get documents by tag
     */
    public List<DocumentInfo> getDocumentsByTag(String tag) {
        List<DocumentInfo> results = new ArrayList<>();
        
        for (Map.Entry<String, Set<String>> entry : documentTags.entrySet()) {
            if (entry.getValue().contains(tag)) {
                File file = new File(notesDirectory, entry.getKey());
                if (file.exists()) {
                    results.add(createDocumentInfo(file));
                }
            }
        }
        
        return results;
    }
    
    /**
     * Get document statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<DocumentInfo> allDocs = new ArrayList<>();
        collectAllDocuments(notesDirectory, allDocs);
        
        long totalSize = 0;
        for (DocumentInfo doc : allDocs) {
            totalSize += doc.size;
        }
        
        stats.put("totalDocuments", allDocs.size());
        stats.put("totalSize", totalSize);
        stats.put("totalCategories", getDocumentsByCategory().size());
        stats.put("totalTags", documentTags.size());
        
        return stats;
    }
    
    /**
     * Helper method to create DocumentInfo from File
     */
    private DocumentInfo createDocumentInfo(File file) {
        String relativePath = getRelativePath(notesDirectory, file);
        DocumentInfo info = new DocumentInfo(
            relativePath,
            file.getName(),
            file.lastModified(),
            file.length()
        );
        
        // Add cached tags if available
        if (documentTags.containsKey(relativePath)) {
            info.tags = documentTags.get(relativePath);
        }
        
        return info;
    }
    
    /**
     * Helper method to get relative path
     */
    private String getRelativePath(File base, File file) {
        String basePath = base.getAbsolutePath();
        String filePath = file.getAbsolutePath();
        
        if (filePath.startsWith(basePath)) {
            return filePath.substring(basePath.length() + 1);
        }
        return filePath;
    }
    
    /**
     * Helper method to copy files
     */
    private void copyFile(File source, File dest) throws IOException {
        if (!source.exists()) {
            throw new IOException("Source file does not exist: " + source.getAbsolutePath());
        }
        
        FileInputStream inStream = new FileInputStream(source);
        FileOutputStream outStream = new FileOutputStream(dest);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null) inChannel.close();
            if (outChannel != null) outChannel.close();
            inStream.close();
            outStream.close();
        }
    }
    
    /**
     * Format date for display
     */
    public static String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
    
    /**
     * Format file size for display
     */
    public static String formatSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format(Locale.getDefault(), "%.1f KB", size / 1024.0);
        } else {
            return String.format(Locale.getDefault(), "%.1f MB", size / (1024.0 * 1024.0));
        }
    }
}
