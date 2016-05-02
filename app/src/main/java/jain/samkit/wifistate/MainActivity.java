package jain.samkit.wifistate;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private SwitchCompat toggle;
    private WifiManager wifiManager;
    private WifiInfo wifiInfo;
    private TextView ssidv, bssidv, levelv, linkv, statusv;
    private String ssid, bssid, level, link, status;
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialising...

        toggle = (SwitchCompat) findViewById(R.id.toggle);
        ssidv = (TextView) findViewById(R.id.ssidv);
        bssidv = (TextView) findViewById(R.id.bssidv);
        levelv = (TextView) findViewById(R.id.levelv);
        linkv = (TextView) findViewById(R.id.linkv);
        statusv = (TextView) findViewById(R.id.statusv);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();

        // Checking if WiFi was already enabled

        if(wifiManager.isWifiEnabled()) {
            toggle.setChecked(true);
            toggle.setText(R.string.off);
        } else {
            toggle.setChecked(false);
            toggle.setText(R.string.on);
        }

        update();

        // Handling state changes in Switch

        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                wifiManager.setWifiEnabled(isChecked);

                if(isChecked) {
                    toggle.setText(R.string.off);
                } else {
                    toggle.setText(R.string.on);
                }

                update();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                update();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void update() {
        new Update(this).execute();
    }

    // AsyncTask to update the table

    private class Update extends AsyncTask<Void, Void, Void> {

        private Context mcontext;

        public Update(Context ucontext) {
            mcontext = ucontext;
        }

        protected void onPreExecute() {
            dialog = ProgressDialog.show(mcontext, "Updating", "Please wait...", true);
        }

        protected Void doInBackground(Void... params) {

            wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            wifiInfo = wifiManager.getConnectionInfo();

            if(wifiManager.isWifiEnabled() && wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                ssid = (wifiInfo.getSSID() != null) ? wifiInfo.getSSID() : "N/A";
                bssid = (wifiInfo.getBSSID() != null) ? wifiInfo.getBSSID() : "N/A";
                level = (wifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5) > 0) ? Integer.toString(wifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5)) : "N/A";
                link = (wifiInfo.getLinkSpeed() > 0) ? wifiInfo.getLinkSpeed() + " Mbps" : "N/A";
            } else {
                ssid = "N/A";
                bssid = "N/A";
                level = "N/A";
                link = "N/A";
            }

            Log.v("ssid", ssid);
            Log.v("bssid", bssid);
            Log.v("level", level);
            Log.v("link", link);

            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            if(networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                switch (networkInfo.getDetailedState()) {
                    case AUTHENTICATING:
                        status = "Authenticating";
                        break;
                    case BLOCKED:
                        status = "Access to this network is blocked";
                        break;
                    case CAPTIVE_PORTAL_CHECK:
                        status = "Checking if network is a captive portal";
                        break;
                    case CONNECTED:
                        status = "Connected";
                        break;
                    case CONNECTING:
                        status = "Connecting";
                        break;
                    case DISCONNECTED:
                        status = "Disconnected";
                        break;
                    case DISCONNECTING:
                        status = "Disconnecting";
                        break;
                    case FAILED:
                        status = "Attempt to connect failed";
                        break;
                    case IDLE:
                        status = "Idle";
                        break;
                    case OBTAINING_IPADDR:
                        status = "Obtaining IP Address";
                        break;
                    case SCANNING:
                        status = "Scanning";
                        break;
                    case SUSPENDED:
                        status = "Suspended";
                        break;
                    case VERIFYING_POOR_LINK:
                        status = "Link has poor connectivity";
                }
            } else {
                status = "N/A";
            }

            Log.v("status", status);

            return null;
        }

        protected void onPostExecute(Void aVoid) {
            ssidv.setText(ssid);
            bssidv.setText(bssid);
            levelv.setText(level);
            linkv.setText(link);
            statusv.setText(status);
            dialog.dismiss();
        }
    }
}
