package com.xw.xianwan;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * appid: 1010
 * appsecret:  nw2olixipulielgp
 * appsign : 10000
 */
public class MainActivity extends AppCompatActivity {
    private EditText edAppid, edSecret, edAppsign;
    private Button btn;
    String appid, secret, appsign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        edAppid = findViewById(R.id.appid);
        edSecret = findViewById(R.id.secret);
        edAppsign = findViewById(R.id.appsign);
        SharedPreferences sPreferences = getSharedPreferences(
                "xw", 0);
        appid = sPreferences.getString("appid", "");
        secret = sPreferences.getString("secret", "");
        appsign = sPreferences.getString("appsign", "");
        if (!TextUtils.isEmpty(appid)) {
            edAppid.setText(appid);
        }
        if (!TextUtils.isEmpty(secret)) {
            edSecret.setText(secret);
        }
        if (!TextUtils.isEmpty(appsign)) {
            edAppsign.setText(appsign);
        }
        btn = findViewById(R.id.btn);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(edAppid.getText())) {
                    appid = edAppid.getText().toString();
                }
                if (!TextUtils.isEmpty(edSecret.getText())) {
                    secret = edSecret.getText().toString();
                }
                if (!TextUtils.isEmpty(edAppsign.getText())) {
                    appsign = edAppsign.getText().toString();
                }

                if (TextUtils.isEmpty(appid) || TextUtils.isEmpty(secret) || TextUtils.isEmpty(appsign)) {
                    Toast.makeText(MainActivity.this, "请输入必填信息", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    SharedPreferences sp = getSharedPreferences("xw", MODE_PRIVATE);
                    sp.edit().putString("appid", appid).commit();
                    sp.edit().putString("secret", secret).commit();
                    sp.edit().putString("appsign", appsign).commit();
                }

                Intent intent = new Intent(MainActivity.this, AdListActivity.class);
                intent.putExtra("appid", appid);
                intent.putExtra("secret", secret);
                intent.putExtra("appsign", appsign);
                startActivity(intent);
            }
        });
    }
}
