package com.changhc.es_final;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Picture;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class WebViewActivity extends AppCompatActivity {

    static public String mIpAddr, mPort;
    private WebView mWebView;
    static public RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(this);
        setContentView(R.layout.activity_web_view);
        Intent intent = getIntent();
        mIpAddr = intent.getStringExtra("IP_ADDR");
        mPort = intent.getStringExtra("PORT");
        mWebView = (WebView) findViewById(R.id.webView);
        Intent intentForService = new Intent(this, DummyService.class);
        startService(intentForService);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        controlStreaming("start");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controlStreaming("stop");
    }


    public void rotate(View v) {
        String url = String.format("http://%s:%s/api/rotate", mIpAddr, mPort);
        JSONObject jsonBody = new JSONObject();
        try {
            Button button = (Button) v;
            String direction = button.getText().toString().equals("UP") ? "1" : "-1";
            jsonBody.put("command", direction);
        } catch (Exception e) {
            Log.e("WebView", e.getMessage());
        }
        final String requestBody = jsonBody.toString();
        StringRequest req = new StringRequest(Request.Method.PUT, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(mWebView.getRootView().getContext(), "Rotated", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mWebView.getRootView().getContext(), String.format("Error: %s", error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        }
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse res) {
                Log.d("Webview", String.valueOf(res.statusCode));
                if (res.statusCode > 300) {

                }
                return super.parseNetworkResponse(res);
            }
        };
        queue.add(req);
    }

    public void captureImage(View v) {
        String url = String.format("http://%s:%s/api/capture", mIpAddr, mPort);
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("command", "1");
        } catch (Exception e) {
            Log.e("WebView", e.getMessage());
        }
        final String requestBody = jsonBody.toString();
        StringRequest req = new StringRequest(Request.Method.PUT, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(mWebView.getRootView().getContext(), "Captured", Toast.LENGTH_SHORT).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(mWebView.getRootView().getContext(), String.format("Error: %s", error.getMessage()), Toast.LENGTH_SHORT).show();
            }
        }
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse res) {
                Log.d("Webview", String.valueOf(res.statusCode));
                if (res.statusCode < 300) {

                }
                return super.parseNetworkResponse(res);
            }
        };
        queue.add(req);
    }

    private void controlStreaming(String type) {
        String url = String.format("http://%s:%s/api/streaming", mIpAddr, mPort);
        Log.d("webview", url);
        JSONObject jsonBody = new JSONObject();
        final String command = type.equals("start") ? "1" : "0";
        try {
            jsonBody.put("command", command);
        } catch (Exception e) {
            Log.e("WebView", e.getMessage());
        }
        final String requestBody = jsonBody.toString();
        StringRequest req = new StringRequest(Request.Method.PUT, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    String verb = command.equals("1") ? "started" : "stopped";
                    Toast.makeText(mWebView.getRootView().getContext(), String.format("Streaming %s", verb), Toast.LENGTH_SHORT).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("webview", error.toString());
                    Toast.makeText(mWebView.getRootView().getContext(), String.format("Error: %s", error.getMessage()), Toast.LENGTH_SHORT).show();
                }
            }
        ) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse res) {
                Log.d("Webview", String.valueOf(res.statusCode));
                if (res.statusCode < 300 && command.equals("1")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String url = String.format("http://%s:%s", mIpAddr, mPort);
                            Log.d("WebView", url);
                            mWebView.loadUrl(url);
                        }
                    });
                }
                return super.parseNetworkResponse(res);
            }
        };
        req.setRetryPolicy(new DefaultRetryPolicy( 50000, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(req);
    }

}
