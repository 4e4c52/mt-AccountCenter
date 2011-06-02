package com.syrinxoon.mtaccountcenter;

import android.content.Intent;
import android.net.Uri;
import greendroid.app.GDApplication;

public class MtApplication extends GDApplication {

    @Override
    public Class<?> getHomeActivityClass() {
        return AccountsActivity.class;
    }
    
    @Override
    public Intent getMainApplicationIntent() {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_url)));
    }

}
