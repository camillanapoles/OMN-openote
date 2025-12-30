/*
Open Markdown Notes (android application to take and organize everyday notes)

Copyright (c) 2017-2024 Mikhail Basov (https://github.com/mvbasov/OMN)

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.basov.omn;

import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.content.FileProvider;
import android.text.Html;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import net.basov.util.AppDetails;
import net.basov.util.FileIO;
import net.basov.util.MyLog;

public class WebViewJSCallback {

    private final Context mContext;

    WebViewJSCallback(Context c) {
        mContext = c;
    }

    @JavascriptInterface
    public void prefButtonCallback() {
        Intent i = new Intent();
        i.setAction(mContext.getPackageName() + ".PREFERENCES");
        mContext.startActivity(i);
    }

    @JavascriptInterface
    public void quicknoteButtonCallback() {
        Intent i = new Intent();
        i.setAction(mContext.getPackageName() + ".QUICK_NOTE");
        mContext.startActivity(i);
    }

    @JavascriptInterface
    public void newPageButtonCallback() {
        Intent i = new Intent();
        i.setAction(mContext.getPackageName() + ".NEW_PAGE");
        mContext.startActivity(i);
    }

    @JavascriptInterface
    public void createButtonCallback(String PFN, String wvUA) {
        FileIO.createPageIfNotExists(mContext, PFN,"", wvUA);

        if(PFN.equals("/" + Constants.BUILD_PAGE)) {
            Intent i = new Intent();
            i.setAction(mContext.getPackageName() + ".REDISPLAY_PAGE");
            mContext.startActivity(i);
        } else {
            editButtonCallback(PFN);
        }
    }

    @JavascriptInterface
    public void editButtonCallback(String pn) {

        Intent i = new Intent();
        i.setAction(mContext.getPackageName()+".EDIT_PAGE");
        i.putExtra("name", pn);

        mContext.startActivity(i);
    }

    @JavascriptInterface
    public void homeButtonCallback() {

        Intent i = new Intent();
        i.setAction(mContext.getPackageName()+".HOME_PAGE");

        mContext.startActivity(i);
    }

    @JavascriptInterface
    public void debugButtonCallback() {

        Intent i = new Intent();
        i.setAction(mContext.getPackageName()+".DEBUG_PAGE");

        mContext.startActivity(i);
    }

    @JavascriptInterface
    public void emailButtonCallback(String pn, String title) {

        Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
        // TODO: send HTML E-Mail body.
        //i.setType("text/html");

        // Help user send platform statistic to correct dev. address
        if (pn.equals("/default/Build")) {
            // Simplest defence from spamer - put e-mail address in different part of code
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{Constants.EMA + "@" + Constants.EMA_DOM});
            // Get build information
            String appInfo = "";
            try {
                appInfo += " " + AppDetails.getAppName(mContext);
            } catch (PackageManager.NameNotFoundException e) {
                MyLog.LogE(e, "Get application name problem.");
            }
            String subject = "["
                    + Build.MANUFACTURER
                    + " - "
                    + Build.MODEL
                    + "] "
                    + appInfo;
            i.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        } else {
            i.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
        }

        i.putExtra(
                Intent.EXTRA_HTML_TEXT,
                Html.fromHtml(
                        FileIO.getStringFromFile(
                                FileIO.getFilesDir(mContext) + "/html" + pn + ".html"
                        )
                )
        );
        i.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + FileIO.getFilesDir(mContext) + "/html" + pn + ".html"));
        try {
        i.putExtra(
                Intent.EXTRA_TEXT,
                Constants.EMA_H_VER
                + ": " + AppDetails.getAppName(mContext)
                + "\n" + Constants.EMA_H_PFN
                + ": " + pn + "\n"
                + Constants.EMA_MARK_START +"\n"
                + FileIO.getStringFromFile(
                    FileIO.getFilesDir(mContext) + "/md" + pn + ".md"
                  )
                + "\n" + Constants.EMA_MARK_STOP
        );
        } catch (PackageManager.NameNotFoundException e) {
            MyLog.LogE(e, "Get application name problem.");
        }
            
        try {
            mContext.startActivity(i);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(mContext, "No E-Mail client found. Please install one.", Toast.LENGTH_LONG).show();
        }
    }
    
    @JavascriptInterface
    public void folderButtonCallback (String pn) {
        Intent intent = new Intent();

        SharedPreferences defSharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (defSharedPref.getBoolean(mContext.getString(R.string.pk_use_view_directory), false)) {
            /* For ES File Manager and X-plore File Manager */
            intent.setAction("org.openintents.action.VIEW_DIRECTORY");
        } else {
            /* For OI File Manager */
            intent.setAction("android.intent.action.VIEW");
        }
        String pageFolder = pn.replaceFirst("[^/]*$", "");
        Uri uri = Uri.parse("file://" + FileIO.getFilesDir(mContext).getAbsolutePath() + "/md" + pageFolder);
        intent.setData(uri);
        try {
            // Dirty hack to enable file:// URI
            StrictMode.VmPolicy old = StrictMode.getVmPolicy();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder(old)
                                           .detectFileUriExposure()
                                           //.penaltyLog()
                                           .build());
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.N){
                    try{
                        Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                        m.invoke(null);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
                                           
                mContext.startActivity(Intent.createChooser(intent, "Open page folder"));
                StrictMode.setVmPolicy(old);
            } else {
                mContext.startActivity(Intent.createChooser(intent, "Open page folder"));
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, "No File Manager found. Please install one.", Toast.LENGTH_LONG).show();
        }
    }

    @JavascriptInterface
    public void linkButtonCallback(String pn, String title) {
        Toast.makeText(mContext, "Link to page in clipboard.", Toast.LENGTH_SHORT).show();

        ClipboardManager clipboard =
                (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(
                title,
                "["+title+"]("+pn.substring(pn.lastIndexOf("/") + 1)+".html)"
        );
        clipboard.setPrimaryClip(clip);      
    }

    @JavascriptInterface
    public void refreshHtmlButtonCallback() {
        Intent i = new Intent();
        i.setAction(mContext.getPackageName() + ".REDISPLAY_PAGE");
        i.putExtra("RECREATE_HTML", true);
        mContext.startActivity(i);
    }

    @JavascriptInterface
    public void sendButtonCallback(String pn, String title) {
        String appInfo = "";
        try {
            appInfo += " " + AppDetails.getAppName(mContext);
        } catch (PackageManager.NameNotFoundException e) {
            MyLog.LogE(e, "Get application name problem.");
        }
        String fileName = "/md" + pn + ".md";
        File pageFile = new File(FileIO.getFilesDir(mContext), fileName);
        Uri uri = NoteProvider.getUriForFile(mContext, mContext.getPackageName() + ".NoteProvider", pageFile);
        MyLog.LogD("URI in WebUiewJSCallback: " + uri.toString());
        Intent sendToIntent = new Intent();
        sendToIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        sendToIntent.setAction(Intent.ACTION_SEND);
        sendToIntent.putExtra(Intent.EXTRA_TITLE, "Title test");
        sendToIntent.putExtra(Intent.EXTRA_SUBJECT, title +" [OMN v" + appInfo +"]" );
        sendToIntent.putExtra(Intent.EXTRA_TEXT, "* [" + title + "](" + pn +")");
        sendToIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendToIntent.setType("text/markdown");

        mContext.startActivity(Intent.createChooser(sendToIntent, "Send page: " + pn));
    }

    @JavascriptInterface
    public void saveHTML(String html, String PFN, String Title) {
        Toast.makeText(mContext, "Save HTML.", Toast.LENGTH_SHORT).show();

        /* Make directory prefix for right css reference */
        int dirCount = PFN.length() - PFN.replaceAll("/","").length();
        String dirPrefix = "";
        if(PFN.charAt(0) == '/') dirCount--;
        for(int i=0; i < dirCount; i++) dirPrefix += "../";

        String[] htmlMetaNames = {
                "date",
                "modified",
                "authors",
                "tags",
                "keywords",
                "summary"
        };
        Page page = new Page(PFN);
        page.setMdContent(
                FileIO.getStringFromFile(
                        FileIO.getFilesDir(mContext)
                                + "/md"
                                + PFN
                                + ".md"
                )
        );

        String htmlMeta = "";
        LinkedHashMap<String, String> pageMeta = page.getPageMeta();

        for (String metaName: htmlMetaNames) {
            if (pageMeta.containsKey(metaName)) {
                String metaRealName = metaName;
                if(metaName.equals("authors") && !pageMeta.get(metaName).contains(","))
                    metaRealName = "author";
                htmlMeta += mContext.getString(
                        R.string.html_meta_template,
                        metaRealName,
                        pageMeta.get(metaName)
                );
            }
        }
        String appNameAndVersion = mContext.getResources().getString(R.string.app_name);
        try {
            appNameAndVersion += " "
                    + AppDetails.getAppName(mContext);
        } catch (PackageManager.NameNotFoundException e) {
            MyLog.LogE("Get application version error");
        }
        htmlMeta += mContext.getString(
                R.string.html_meta_template,
                "generator",
                appNameAndVersion
        );

        String tagsMarks = "";
        ArrayList<String> tags = new ArrayList<String>();
        if (page.hasMetaWithKey("tags")) {
            for (String tg: page.getMetaByKey("tags").split(",")) {
                // Trim edge spaces
                tg = tg.trim();
                if (tg.length() >0)
                    tags.add(tg);
                tagsMarks += mContext.getString(
                            R.string.html_one_tag_template,
                            // Replace internal spaces to &nbsp;
                            // to prevent split tag when wrap to next line
                            tg.replace(" ", "&nbsp;"),
                            dirPrefix + "Tags.html#" + tg
                                    .replace(" ", "-")
                                    .replace("#", "")
                                    .replace("<", "")
                                    .replace(">", "")
                                    .replace("/","")
                    );
            }
            tagsMarks = mContext.getString(
                    R.string.html_tags_template,
                    tagsMarks);
            FileIO.savePageTags(mContext, PFN, page.getPageTitleOrName(), tags);
        } else {
            FileIO.savePageTags(mContext, PFN, "fake", null);
        }

        String htmlTop = mContext.getString(
                R.string.html_top,
                mContext.getPackageName(),
                Title,      //Page title
                dirPrefix,  //Reference to top (../*[0..n])
                htmlMeta,   //Summary
				PFN,		//Page name in top control bock
                Title,      //1-st page header as Title and top buttons block controll
                tagsMarks   //Tags
        );
        String htmlBottom = mContext.getString(R.string.html_buttom);
        String htmlPage = htmlTop + html + htmlBottom;
        if(FileIO.isFileExists(mContext, "md"+PFN+".md"))
            FileIO.saveHTML(mContext, PFN, htmlPage);
        Intent i = new Intent();
        i.setAction(mContext.getPackageName()+".REDISPLAY_PAGE");

        mContext.startActivity(i);
    }

    @JavascriptInterface
    public void shortcutButtonCallback(String pn, String title) {

        Toast.makeText(mContext, "Create shortcut to \"" + title + "\"(" + pn + ")", Toast.LENGTH_SHORT).show();

        Intent shortcutIntent = new Intent(mContext.getApplicationContext(),
                MainActivity.class);
        shortcutIntent.putExtra("page_name", pn);
        shortcutIntent.setAction(Intent.ACTION_MAIN);


        if(Build.VERSION.SDK_INT >= 26) {
            ShortcutManager mShortcutManager =
                    mContext.getSystemService(ShortcutManager.class);
            ShortcutInfo shortcut = new ShortcutInfo.Builder(mContext, pn.replaceAll("/",""))
                    .setShortLabel(title)
                    .setLongLabel(title)
                    .setIcon(Icon.createWithResource(mContext, R.mipmap.omn_ic_shortcut))
                    .setIntent(shortcutIntent)
                    .build();
            if (mShortcutManager.isRequestPinShortcutSupported()) {
                Intent pinnedShortcutCallbackIntent =
                        mShortcutManager.createShortcutResultIntent(shortcut);
                PendingIntent successCallback = PendingIntent.getBroadcast(mContext, 0,
                        pinnedShortcutCallbackIntent, PendingIntent.FLAG_IMMUTABLE);

                mShortcutManager.requestPinShortcut(shortcut,
                        successCallback.getIntentSender());
            }

        } else {
            // Code from here: https://stackoverflow.com/a/16873257
            Intent addIntent = new Intent();
            addIntent
                    .putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, title);
            addIntent.putExtra(
                    Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(
                            mContext.getApplicationContext(),
                            R.mipmap.omn_ic_shortcut
                    )
            );
            addIntent
                    .setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            addIntent.putExtra("duplicate", false);  //may it's already there so don't duplicate
            mContext.getApplicationContext().sendBroadcast(addIntent);
        }
    }
    
    /**
     * Get AI context suggestions for the current page
     * @param pageName Name of the current page
     * @param pageContent Content of the page
     * @return JSON string with AI suggestions
     */
    @JavascriptInterface
    public String getAIContextSuggestions(String pageName, String pageContent) {
        try {
            MainActivity mainActivity = (MainActivity) mContext;
            if (mainActivity.aiContextManager != null) {
                mainActivity.aiContextManager.analyzeNoteContext(pageName, pageContent);
                java.util.List<String> suggestions = mainActivity.aiContextManager.generateOrganizationSuggestions(pageName);
                return android.text.TextUtils.join("\n", suggestions);
            }
        } catch (Exception e) {
            MyLog.LogE(e, "Error getting AI suggestions");
        }
        return "";
    }
    
    /**
     * Get related notes based on AI context analysis
     * @param pageName Name of the current page
     * @return JSON array of related note names
     */
    @JavascriptInterface
    public String getRelatedNotes(String pageName) {
        try {
            MainActivity mainActivity = (MainActivity) mContext;
            if (mainActivity.aiContextManager != null) {
                java.util.List<String> relatedNotes = mainActivity.aiContextManager.findRelatedNotes(pageName);
                return android.text.TextUtils.join(",", relatedNotes);
            }
        } catch (Exception e) {
            MyLog.LogE(e, "Error getting related notes");
        }
        return "";
    }
    
    /**
     * Get graph visualization HTML for the current page
     * @param pageName Name of the current page
     * @param pageContent Content of the page
     * @return HTML string with graph visualization
     */
    @JavascriptInterface
    public String getGraphVisualization(String pageName, String pageContent) {
        try {
            MainActivity mainActivity = (MainActivity) mContext;
            if (mainActivity.graphManager != null) {
                mainActivity.graphManager.extractNoteLinks(pageName, pageContent);
                return mainActivity.graphManager.generateGraphHTML(pageName);
            }
        } catch (Exception e) {
            MyLog.LogE(e, "Error getting graph visualization");
        }
        return "";
    }
    
    /**
     * Search documents by keyword
     * @param keyword Search term
     * @return JSON array of matching documents
     */
    @JavascriptInterface
    public String searchDocuments(String keyword) {
        try {
            MainActivity mainActivity = (MainActivity) mContext;
            if (mainActivity.documentManager != null) {
                java.util.List<DocumentManager.DocumentInfo> results = 
                    mainActivity.documentManager.searchDocuments(keyword);
                return convertDocumentListToJson(results);
            }
        } catch (Exception e) {
            MyLog.LogE(e, "Error searching documents");
        }
        return "[]";
    }
    
    /**
     * Get recent documents
     * @param limit Maximum number of documents to return
     * @return JSON array of recent documents
     */
    @JavascriptInterface
    public String getRecentDocuments(int limit) {
        try {
            MainActivity mainActivity = (MainActivity) mContext;
            if (mainActivity.documentManager != null) {
                java.util.List<DocumentManager.DocumentInfo> results = 
                    mainActivity.documentManager.getRecentDocuments(limit);
                return convertDocumentListToJson(results);
            }
        } catch (Exception e) {
            MyLog.LogE(e, "Error getting recent documents");
        }
        return "[]";
    }
    
    /**
     * Get documents organized by category
     * @return JSON object with categories as keys and document arrays as values
     */
    @JavascriptInterface
    public String getDocumentsByCategory() {
        try {
            MainActivity mainActivity = (MainActivity) mContext;
            if (mainActivity.documentManager != null) {
                java.util.Map<String, java.util.List<DocumentManager.DocumentInfo>> categorized = 
                    mainActivity.documentManager.getDocumentsByCategory();
                return convertCategorizedDocumentsToJson(categorized);
            }
        } catch (Exception e) {
            MyLog.LogE(e, "Error getting documents by category");
        }
        return "{}";
    }
    
    /**
     * Copy multiple documents to a destination folder
     * @param sourcePathsJson JSON array of source paths
     * @param destinationFolder Destination folder name
     * @return true if all documents were copied successfully
     */
    @JavascriptInterface
    public boolean copyDocuments(String sourcePathsJson, String destinationFolder) {
        try {
            MainActivity mainActivity = (MainActivity) mContext;
            if (mainActivity.documentManager != null) {
                java.util.List<String> sourcePaths = parseJsonArray(sourcePathsJson);
                return mainActivity.documentManager.copyDocuments(sourcePaths, destinationFolder);
            }
        } catch (Exception e) {
            MyLog.LogE(e, "Error copying documents");
        }
        return false;
    }
    
    /**
     * Move multiple documents to a destination folder
     * @param sourcePathsJson JSON array of source paths
     * @param destinationFolder Destination folder name
     * @return true if all documents were moved successfully
     */
    @JavascriptInterface
    public boolean moveDocuments(String sourcePathsJson, String destinationFolder) {
        try {
            MainActivity mainActivity = (MainActivity) mContext;
            if (mainActivity.documentManager != null) {
                java.util.List<String> sourcePaths = parseJsonArray(sourcePathsJson);
                return mainActivity.documentManager.moveDocuments(sourcePaths, destinationFolder);
            }
        } catch (Exception e) {
            MyLog.LogE(e, "Error moving documents");
        }
        return false;
    }
    
    /**
     * Export multiple documents to external storage
     * @param sourcePathsJson JSON array of source paths
     * @param exportFolderName Export folder name
     * @return true if all documents were exported successfully
     */
    @JavascriptInterface
    public boolean exportDocuments(String sourcePathsJson, String exportFolderName) {
        try {
            MainActivity mainActivity = (MainActivity) mContext;
            if (mainActivity.documentManager != null) {
                java.util.List<String> sourcePaths = parseJsonArray(sourcePathsJson);
                return mainActivity.documentManager.exportDocuments(sourcePaths, exportFolderName);
            }
        } catch (Exception e) {
            MyLog.LogE(e, "Error exporting documents");
        }
        return false;
    }
    
    /**
     * Get document statistics
     * @return JSON object with statistics
     */
    @JavascriptInterface
    public String getDocumentStatistics() {
        try {
            MainActivity mainActivity = (MainActivity) mContext;
            if (mainActivity.documentManager != null) {
                java.util.Map<String, Object> stats = mainActivity.documentManager.getStatistics();
                return convertStatisticsToJson(stats);
            }
        } catch (Exception e) {
            MyLog.LogE(e, "Error getting document statistics");
        }
        return "{}";
    }
    
    /**
     * Open a document by path
     * @param path Document path
     */
    @JavascriptInterface
    public void openDocument(String path) {
        try {
            // Navigate to the document
            Intent i = new Intent();
            i.setAction(mContext.getPackageName() + ".OPEN_PAGE");
            i.putExtra("name", "/" + path.replace(".md", ""));
            mContext.startActivity(i);
        } catch (Exception e) {
            MyLog.LogE(e, "Error opening document");
        }
    }
    
    // Helper methods for JSON conversion
    
    private String convertDocumentListToJson(java.util.List<DocumentManager.DocumentInfo> documents) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < documents.size(); i++) {
            if (i > 0) json.append(",");
            DocumentManager.DocumentInfo doc = documents.get(i);
            json.append("{")
                .append("\"path\":\"").append(escapeJson(doc.path)).append("\",")
                .append("\"name\":\"").append(escapeJson(doc.name)).append("\",")
                .append("\"lastModified\":").append(doc.lastModified).append(",")
                .append("\"size\":").append(doc.size).append(",")
                .append("\"category\":\"").append(escapeJson(doc.category)).append("\"")
                .append("}");
        }
        json.append("]");
        return json.toString();
    }
    
    private String convertCategorizedDocumentsToJson(java.util.Map<String, java.util.List<DocumentManager.DocumentInfo>> categorized) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (java.util.Map.Entry<String, java.util.List<DocumentManager.DocumentInfo>> entry : categorized.entrySet()) {
            if (!first) json.append(",");
            first = false;
            json.append("\"").append(escapeJson(entry.getKey())).append("\":")
                .append(convertDocumentListToJson(entry.getValue()));
        }
        json.append("}");
        return json.toString();
    }
    
    private String convertStatisticsToJson(java.util.Map<String, Object> stats) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (java.util.Map.Entry<String, Object> entry : stats.entrySet()) {
            if (!first) json.append(",");
            first = false;
            json.append("\"").append(escapeJson(entry.getKey())).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(escapeJson((String) value)).append("\"");
            } else {
                json.append(value);
            }
        }
        json.append("}");
        return json.toString();
    }
    
    private java.util.List<String> parseJsonArray(String jsonArray) {
        java.util.List<String> result = new java.util.ArrayList<>();
        // Simple JSON array parser for string arrays
        String trimmed = jsonArray.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            String content = trimmed.substring(1, trimmed.length() - 1);
            if (!content.trim().isEmpty()) {
                String[] parts = content.split(",");
                for (String part : parts) {
                    String cleaned = part.trim();
                    if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
                        cleaned = cleaned.substring(1, cleaned.length() - 1);
                    }
                    result.add(cleaned);
                }
            }
        }
        return result;
    }
    
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
