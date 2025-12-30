# Document Management Guide

## Multi-Document Management Features

Open Markdown Notes now includes powerful multi-document management capabilities to help you organize and work with large collections of markdown notes.

## Features

### 1. Document Search

Search across all your documents by filename or content:

- **Quick Search**: Start typing in the search box to find documents
- **Real-time Results**: Results appear as you type
- **Fuzzy Matching**: Finds documents even with partial matches

### 2. Category Organization

Organize documents by folders and categories:

- **Folder Structure**: Documents are automatically categorized by their folder location
- **Category Filter**: Filter documents to show only specific categories
- **Hierarchical View**: See your document structure at a glance

### 3. Recent Documents

Quick access to your recently modified documents:

- **Last Modified**: Shows the most recently edited documents
- **Quick Open**: Click to open any recent document
- **Configurable Limit**: Set how many recent documents to display

### 4. Batch Operations

Perform operations on multiple documents at once:

#### Copy Documents
- Select multiple documents
- Copy them to a new folder
- Original documents remain in place

#### Move Documents
- Select multiple documents
- Move them to a new folder
- Documents are relocated

#### Export Documents
- Select multiple documents
- Export to external storage
- Creates a backup outside the app

### 5. Document Statistics

View comprehensive statistics about your document library:

- **Total Documents**: Number of documents in your library
- **Total Size**: Combined size of all documents
- **Categories**: Number of categories/folders
- **Tags**: Number of tags in use

### 6. Document Tags

Organize documents with custom tags:

- **Tag Documents**: Add tags to documents for easy organization
- **Filter by Tags**: Find all documents with a specific tag
- **Multiple Tags**: Add multiple tags to each document

### 7. Templates

Create new documents from templates:

- **Save Templates**: Save frequently used document structures
- **Quick Creation**: Create new documents from templates
- **Customizable**: Edit templates to fit your needs

## Using the Document Manager

### Accessing Document Manager

The document manager can be accessed through:
- Main menu → Document Manager
- JavaScript API in custom pages
- Programmatic access via the Android API

### Search Workflow

1. Open the document manager
2. Type your search term in the search box
3. Results appear automatically
4. Click on a document to open it

### Batch Operation Workflow

1. Open the document manager
2. Select multiple documents using checkboxes
3. Click the operation button (Copy, Move, or Export)
4. Enter the destination folder name
5. Confirm the operation

### Category Organization

1. Documents are automatically organized by folder
2. Use the category filter to view specific folders
3. Create new folders by moving/copying documents

## JavaScript API

For advanced users, the document manager provides a JavaScript API:

```javascript
// Search documents
var results = Android.searchDocuments("keyword");
var docs = JSON.parse(results);

// Get recent documents
var recent = Android.getRecentDocuments(10);
var recentDocs = JSON.parse(recent);

// Get documents by category
var categorized = Android.getDocumentsByCategory();
var cats = JSON.parse(categorized);

// Copy documents
var sourcePaths = ["note1.md", "note2.md"];
var success = Android.copyDocuments(JSON.stringify(sourcePaths), "NewFolder");

// Move documents
var success = Android.moveDocuments(JSON.stringify(sourcePaths), "NewFolder");

// Export documents
var success = Android.exportDocuments(JSON.stringify(sourcePaths), "ExportFolder");

// Get statistics
var stats = Android.getDocumentStatistics();
var statistics = JSON.parse(stats);

// Open a document
Android.openDocument("path/to/note.md");
```

## Tips and Best Practices

### Organization

1. **Use Folders**: Organize documents into logical folders
2. **Consistent Naming**: Use consistent naming conventions
3. **Add Tags**: Tag documents for cross-category organization
4. **Regular Cleanup**: Use batch operations to reorganize periodically

### Search

1. **Keywords**: Use specific keywords for better results
2. **Partial Matches**: Don't worry about exact spelling
3. **Recent Documents**: Check recent documents first

### Backup

1. **Regular Exports**: Export important documents regularly
2. **External Storage**: Use export to create backups
3. **Version Control**: Consider using git for version control

### Performance

1. **Limit Results**: Use filters to reduce large result sets
2. **Clear Cache**: Restart the app if search becomes slow
3. **Organize Files**: Keep folder structure relatively flat

## Troubleshooting

### Search Not Finding Documents

- Check that documents are in the notes directory
- Verify file extensions are .md
- Restart the app to refresh the index

### Batch Operations Failing

- Ensure destination folder name is valid
- Check available storage space
- Verify permissions are granted

### Export Not Working

- Grant storage permissions
- Check external storage is available
- Ensure Documents folder exists

### Performance Issues

- Reduce number of documents loaded
- Use category filters
- Clear app cache
- Restart the application

## Future Enhancements

Planned features for future versions:

- Full-text search within document content
- Advanced filtering (by date, size, tags)
- Document preview in search results
- Bulk tagging operations
- Import from external sources
- Cloud synchronization
- Collaborative editing

## Feedback

We welcome feedback on the document management features. Please report issues or suggest improvements through the GitHub repository.

---

**Open Markdown Notes - Version 35+**
