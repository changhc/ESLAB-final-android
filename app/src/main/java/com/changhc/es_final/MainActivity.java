package com.changhc.es_final;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends AppCompatActivity {
    private String mIpAddr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void connectClicked(View view) {
        EditText ipEditText = (EditText) findViewById(R.id.ipAddr);
        EditText portEditText = (EditText) findViewById(R.id.port);
        if (checkIpValidity(ipEditText.getText().toString())) {
            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra("IP_ADDR", mIpAddr);
            intent.putExtra("PORT", portEditText.getText().toString());
            startActivity(intent);
        }
    }

    private boolean checkIpValidity(String ip) {
        String[] parts = ip.split("\\.");
        Log.d("Main", ip);
        for (String part: parts) {
            Log.d("Main", part);
        }

        try {
            if (parts.length != 4) {
                throw new Exception("Invalid IP");
            }
            for (String part: parts) {
                int num = Integer.parseInt(part);
                if (num > 255 || num < 0) {
                    throw new Exception("Invalid IP");
                }
            }
            mIpAddr = ip;
            return true;
        } catch (Exception e) {
            Toast.makeText(findViewById(R.id.root_layout).getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }

    }
}
