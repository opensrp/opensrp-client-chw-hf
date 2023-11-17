package org.smartregister.chw.hf.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;

import org.smartregister.chw.hf.activity.PmtctReportsViewActivity;

public class LocalContentWebViewClient extends WebViewClientCompat {

    private final WebViewAssetLoader mAssetLoader;

    private WebView mWebView;

    private ProgressBar progressBar;

    public LocalContentWebViewClient(WebViewAssetLoader assetLoader,WebView mWebView,ProgressBar progressBar) {
        mAssetLoader = assetLoader;
        this.mWebView = mWebView;
        this.progressBar = progressBar;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        mWebView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean shouldOverrideUrlLoading(@NonNull WebView view, @NonNull WebResourceRequest request) {
        progressBar.setVisibility(View.VISIBLE);
        mWebView.setVisibility(View.GONE);
        return super.shouldOverrideUrlLoading(view, request);
    }

    @Override
    @RequiresApi(21)
    public WebResourceResponse shouldInterceptRequest(WebView view,
                                                      WebResourceRequest request) {
        return mAssetLoader.shouldInterceptRequest(request.getUrl());
    }

    @Override
    @SuppressWarnings("deprecation") // to support API < 21
    public WebResourceResponse shouldInterceptRequest(WebView view,
                                                      String url) {
        return mAssetLoader.shouldInterceptRequest(Uri.parse(url));
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        PmtctReportsViewActivity.printWebView = view;
        mWebView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onPageCommitVisible(@NonNull WebView view, @NonNull String url) {
        super.onPageCommitVisible(view, url);
        mWebView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
    }
}
