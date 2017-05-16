package com.race.planner.activities;

import android.Manifest;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.race.planner.R;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import com.race.planner.data_models.*;
import com.race.planner.utils.WrapContentViewPager;

import org.w3c.dom.Text;

/**
 * The MainActivity class currently allows users to choose between creating a new training plan and
 * listing all current training plans that are created.
 */
public class MainActivity extends Activity implements EasyPermissions.PermissionCallbacks
{

    static final int VIEW_PAGER_FIRST_ELEMENT = 0;
    static final int VIEW_PAGER_LAST_ELEMENT = 1;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String TAG = AuthenticateAndCallAPI.class.getName();
    private static final String BUTTON_TEXT = "Call Google Calendar API";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = {CalendarScopes.CALENDAR};
    private TextView mOutputText;
    private ProgressDialog mProgress;
    private GoogleAccountCredential mCredential;
    int currentPage;
    ImageButton backViewPagers;
    ImageButton forwardViewPager;
    ViewPager viewPager;
    TextView viewMoreTextHint;
    Boolean isHintTextFaded = false;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        /**
         * This is the viewpager adapter. The view pager holds the four TextViews. The adapter
         * converts those into the viewpager object through the ViewPagerAdapter inner class.
         */
        final ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter();
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(5); // required to prevent TextViews from disappearing

//        viewMoreTextHint = (TextView) findViewById(R.id.text_view_more_info);
//        viewMoreTextHint.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));

        // TODO: determine whether this is necessary
        mProgress = new ProgressDialog(this);
        mProgress.setMessage("Calling Google Calendar API ...");

        // instantiate credentials and service object.
        mCredential = GoogleAccountCredential.usingOAuth2(
                getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        // if called from other activity, grabs updated account name for listing
        if (getIntent().hasExtra(GlobalVariables.CREDENTIAL_ACCOUNT_NAME))
        {
            mCredential.setSelectedAccountName(getIntent().getExtras().getString(GlobalVariables.CREDENTIAL_ACCOUNT_NAME));
        }

        // TODO: check location, possibly move it to bottom of onCreate()
        requestPermissions();

        // instantiate button for creating a new training plan
        Button openSelectTrainingPlanActivity = (Button) findViewById(R.id.button_open_select_training_plan_activity);
        openSelectTrainingPlanActivity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                Intent intent = new Intent(MainActivity.this, SelectTrainingPlan.class);
                intent.putExtra(GlobalVariables.CREDENTIAL_ACCOUNT_NAME, mCredential.getSelectedAccountName());
                startActivity(intent);
            }
        });

        // instantiate button for listing all created training plans
        Button openListTrainingPlansActivity = (Button) findViewById(R.id.button_open_list_training_plans_activity);
        openListTrainingPlansActivity.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MainActivity.this, ListTrainingPlans.class);
                intent.putExtra(GlobalVariables.CREDENTIAL_ACCOUNT_NAME, mCredential.getSelectedAccountName());
                startActivity(intent);
            }
        });
    }

    /**
     * checks to make sure all necessary prerequisites for the google API are met
     *
     * @return
     */
    private boolean meetsPreReqs()
    {
        if (!isGooglePlayServicesAvailable())
        {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null)
        {
            //chooseAccount();
        } else if (!isDeviceOnline())
        {
            mOutputText.setText("No network connection available.");
        }
        return true;
    }

    private void requestPermissions()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SYNC_SETTINGS) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_CALENDAR,
                            Manifest.permission.WRITE_CALENDAR,
                            Manifest.permission.READ_SYNC_SETTINGS,
                            Manifest.permission.GET_ACCOUNTS},
                    GlobalVariables.MY_PERMISSIONS_REQUEST_READ_CALENDAR);
        }
    }


    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode  code indicating the result of the incoming
     *                    activity result.
     * @param data        Intent (containing result data) returned by incoming
     *                    activity result.
     */
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK)
                {
                    mOutputText.setText(
                            "This app requires Google Play Services. Please install " + "Google Play Services on your device and relaunch this app.");
                } else
                {
                    meetsPreReqs();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null)
                {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null)
                    {
                        SharedPreferences settings =
                                PreferenceManager.getDefaultSharedPreferences(this);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        meetsPreReqs();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK)
                {
                    meetsPreReqs();
                }
                break;
        }
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     *
     * @param requestCode  The request code passed in
     *                     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);

        // ask for account info
        String accountName = PreferenceManager.getDefaultSharedPreferences(this)
                .getString(PREF_ACCOUNT_NAME, null);
        if (accountName != null)
        {
            mCredential.setSelectedAccountName(accountName);
            Log.e(TAG, "Create Calendar inside chooseAccount called");

        } else
        {
            // Start a dialog from which the user can choose an account
            startActivityForResult(
                    mCredential.newChooseAccountIntent(),
                    REQUEST_ACCOUNT_PICKER);
        }
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list)
    {
        // do nothing
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     *
     * @param requestCode The request code associated with the requested
     *                    permission
     * @param list        The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list)
    {
        // Do nothing.
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline()
    {
        ConnectivityManager connMgr =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable()
    {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices()
    {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(this);
        if (apiAvailability.isUserResolvableError(connectionStatusCode))
        {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode)
    {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                MainActivity.this,
                connectionStatusCode,
                REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * Custom adapter for view pager widget
     */
    private class ViewPagerAdapter extends PagerAdapter
    {
        private Context mContext;

        public ViewPagerAdapter()
        {
        }

        // instantiates based on panel position
        public Object instantiateItem(ViewGroup collection, int position)
        {
            int resId = 0;
            switch (position)
            {
                case 0:
                    resId = R.id.view_pager_slide_1;
                    break;
                case 1:
                    resId = R.id.view_pager_slide_2;
                    break;
                case 2:
                    resId = R.id.view_pager_slide_3;
                    break;
                case 3:
                    resId = R.id.view_pager_slide_4;
                    break;
                case 4:
                    resId = R.id.view_pager_slide_5;
                    break;
                case 5:
                    resId = R.id.view_pager_slide_6;
                    break;
            }
            return findViewById(resId);
        }

        // not used because we increased the limit of how many objects can be instantiated at a time
        @Override
        public void destroyItem(ViewGroup container, int position, Object object)
        {
            container.removeView((View) object);

        }

        // determines the amount of panels to create
        @Override
        public int getCount()
        {
            return 6;
        }

        // checks to see if the current view on the view pager is from the object called
        @Override
        public boolean isViewFromObject(View view, Object object)
        {
            return view == (View) object;
        }

    }
}
