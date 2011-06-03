package com.syrinxoon.mtaccountcenter;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import greendroid.app.ActionBarActivity;
import greendroid.app.GDTabActivity;

public class InformationsActivity extends GDTabActivity {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.informations);
        
        final String aboutText =  getString(R.string.about);
        final Intent aboutIntent = new Intent(this, AboutActivity.class);
        aboutIntent.putExtra(ActionBarActivity.GD_ACTION_BAR_VISIBILITY, View.GONE);
        addTab(aboutText, aboutText, aboutIntent);

        final String licenseText =  getString(R.string.license);
        final Intent licenseIntent = new Intent(this, WebContentActivity.class);
        licenseIntent.putExtra(ActionBarActivity.GD_ACTION_BAR_VISIBILITY, View.GONE);
        licenseIntent.putExtra(WebContentActivity.EXTRA_CONTENT_URL, "file:///android_asset/COPYRIGHT.txt");
        addTab(licenseText, licenseText, licenseIntent);
    }

    public void onAppUrlClicked(View v) {
        final Uri appUri = Uri.parse(getString(R.string.project_url));
        startActivity(new Intent(Intent.ACTION_VIEW, appUri));
    }
}
