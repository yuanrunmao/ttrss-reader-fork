/*
 * ttrss-reader-fork for Android
 * 
 * Copyright (C) 2010 N. Braden.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 3 as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 */

package org.ttrssreader.model.article;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import org.ttrssreader.R;
import org.ttrssreader.gui.activities.MediaPlayerActivity;
import org.ttrssreader.utils.Utils;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MyWebViewClient extends WebViewClient {
    
    private Context context;
    
    public boolean shouldOverrideUrlLoading(WebView view, final String url) {
        
        Log.e(Utils.TAG, "Link clicked: " + url);
        context = view.getContext();
        boolean media = false;
        
        for (String s : Utils.MEDIA_EXTENSIONS) {
            if (url.toLowerCase().contains(s)) {
                media = true;
                break;
            }
        }
        
        if (media) {
            final CharSequence[] items = { "Display in Mediaplayer", "Download" };
            
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("What shall we do?");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                
                public void onClick(DialogInterface dialog, int item) {
                    
                    switch (item) {
                        case 0:
                            Log.e(Utils.TAG, "Displaying file in mediaplayer: " + url);
                            Intent i = new Intent(context, MediaPlayerActivity.class);
                            i.putExtra(MediaPlayerActivity.URL, url);
                            context.startActivity(i);
                            break;
                        case 1:
                            downloadFile(url);
                            break;
                        default:
                            Log.e(Utils.TAG, "Doing nothing, but why is that?? Item: " + item);
                            break;
                    }
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            Uri uri = Uri.parse(url);
            context.startActivity(new Intent(Intent.ACTION_VIEW, uri));
        }
        
        return true;
    }
    
    private void downloadFile(String url) {
        if (!externalStorageState()) {
            Log.e(Utils.TAG, "External Storage not available, skipping download...");
            return;
        }
        
        try {
            new AsyncDownloader().execute(new URL(url));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    
    private boolean externalStorageState() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        } else {
            return false;
        }
    }
    
    private class AsyncDownloader extends AsyncTask<URL, Void, Void> {
        protected Void doInBackground(URL... urls) {
            if (urls.length < 1) {
                return null;
            }
            
            URL url = urls[0];
            long start = System.currentTimeMillis();
            
            // Build name as "download_123801230712", then try to extract a proper name from URL
            String name = "download_" + System.currentTimeMillis();
            if (!url.getFile().equals("")) {
                int pos = url.getFile().lastIndexOf("/");
                if (pos > 0)
                    name = url.getFile().substring(pos + 1);
            }
            
            // Path: /sdcard/Android/data/org.ttrssreader/files/
            StringBuilder sb = new StringBuilder();
            sb.append(Environment.getExternalStorageDirectory()).append(File.separator).append(Utils.SDCARD_PATH)
                    .append(File.separator).append(name);
            File file = new File(sb.toString());
            
            try {
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("GET");
                c.setDoOutput(true);
                c.connect();
                
                FileOutputStream f = new FileOutputStream(file);
                InputStream in = c.getInputStream();
                
                byte[] buffer = new byte[1024];
                int len1 = 0;
                while ((len1 = in.read(buffer)) != -1) {
                    f.write(buffer, 0, len1);
                }
                f.close();
                
                int time = (int) (System.currentTimeMillis() - start) / 1000;
                Log.d(Utils.TAG, "Finished. Path: " + file.getAbsolutePath() + " Time: " + time + "s.");
                showNotification(file.getAbsolutePath(), time);
                
            } catch (IOException e) {
                Log.d(Utils.TAG, "Error while downloading: " + e);
            }
            
            return null;
        }
    }
    
    public void showNotification(String path, int time) {
        if (path == null)
            return;
        
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotMan = (NotificationManager) context.getSystemService(ns);
        
        int icon = R.drawable.icon;
        CharSequence ticker = "Download finished.";
        long when = System.currentTimeMillis();
        CharSequence title = "Download finished in " + time + "s.";

        PendingIntent intent = PendingIntent.getActivity(context, 0, new Intent(), 0);
        Notification n = new Notification(icon, ticker, when);
        n.flags |= Notification.FLAG_AUTO_CANCEL;
        n.setLatestEventInfo(context, title, path, intent);
        
        mNotMan.notify(0, n);
    }
    
}
