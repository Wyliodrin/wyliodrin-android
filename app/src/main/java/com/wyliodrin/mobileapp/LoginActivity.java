package com.wyliodrin.mobileapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.wyliodrin.mobileapp.api.ServerConnection;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends Activity {

    private ProgressDialog progresDialog;
    private SharedPreferences shPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        Button scanQrCodeButton = (Button) findViewById(R.id.qrcode_button);

        scanQrCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IntentIntegrator.initiateScan(LoginActivity.this);
            }
        });

        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra("logout", false)) {
            shPref = getSharedPreferences("login", MODE_PRIVATE);
            shPref.edit().clear().commit();

            TextView textViewLogout = (TextView) findViewById(R.id.logout_text);
            textViewLogout.setVisibility(View.VISIBLE);
        }

        shPref = getSharedPreferences("login", MODE_PRIVATE);
        String user = shPref.getString("user", "");
        String password = shPref.getString("password", "");
        String owner = shPref.getString("owner", "");

        if (!user.isEmpty() && !password.isEmpty() && !owner.isEmpty()) {
            new LoginThread(user, password, owner).start();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == Activity.RESULT_OK) {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null) {
                String token = scanResult.getContents();
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(token);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (jsonObject != null) {

                    String user = jsonObject.opt("jid").toString();
                    String password = jsonObject.opt("password").toString();
                    String owner = jsonObject.opt("owner").toString();

                    new LoginThread(user, password, owner).start();
                } else {
                    Toast.makeText(LoginActivity.this, "The QR Code is wrong.", Toast.LENGTH_SHORT).show();
                }
            }
        }

    }

    private class LoginThread extends Thread {

        private String user;
        private String password;
        private String owner;

        LoginThread(String user, String password, String owner) {
            this.user = user;
            this.password = password;
            this.owner = owner;
            Log.d("test", user+" " + password + " " + owner);
            progresDialog = ProgressDialog.show(LoginActivity.this, "Please wait", "Logging in", true, false);
        }

        @Override
        public void run() {
            super.run();

            final ServerConnection.LoginResult result = ServerConnection.getInstance().connect(user, password, owner);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (progresDialog != null) {
                        progresDialog.dismiss();
                        progresDialog = null;
                    }
                    
                    if (result.equals(ServerConnection.LoginResult.Success)) {
                        SharedPreferences.Editor editor = shPref.edit();
                        editor.putString("user", user);
                        editor.putString("password", password);
                        editor.putString("owner", owner);
                        editor.commit();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else if (result.equals(ServerConnection.LoginResult.Failed)) {
                        Toast.makeText(LoginActivity.this, "Please check your Internet connection.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
