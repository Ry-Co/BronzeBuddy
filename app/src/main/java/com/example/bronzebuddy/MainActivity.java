package com.example.bronzebuddy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.bronzebuddy.Objects.dayObject;
import com.example.bronzebuddy.TanTimerWidget.TanTimerSetupActivity;
import com.example.bronzebuddy.Workers.LocalStorageWorker;
import com.example.bronzebuddy.Workers.LocationWorker;
import com.example.bronzebuddy.Workers.WeatherWorker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    Toolbar toolbar;
    ImageButton tanTimerIB, forecastIB, tanPlanIB, localIB, recoProdIB;
    ArrayList<Integer> localClimateList = new ArrayList<>(12);

    LocationWorker locationWorker;
    WeatherWorker weatherWorker;
    LocalStorageWorker localStorageWorker;

    Location mCurrentLocation = null;
    ArrayList<dayObject> forecast = null;

    //TODO: add a loading screen splash for contacting the weather api
    //also check if the weather api has been called recently(last 6hours), if so, skip it and take the old data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_null);

        initLayout();
    }

    public void initLayout() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        tanTimerIB = findViewById(R.id.tanTimerButton);
        forecastIB = findViewById(R.id.todaysForecastButton);
        tanPlanIB = findViewById(R.id.tanPlanButton);
        localIB = findViewById(R.id.localSeasonButton);
        recoProdIB = findViewById(R.id.recommendedProductsButton);

        tanTimerIB.setOnClickListener(this);
        forecastIB.setOnClickListener(this);
        tanPlanIB.setOnClickListener(this);
        localIB.setOnClickListener(this);
        recoProdIB.setOnClickListener(this);

        localStorageWorker = new LocalStorageWorker(this);
        locationWorker = new LocationWorker(this, this);
        if (locationWorker.checkPermissions()) {
            getCurrentLocation();
        } else {
            locationWorker.requestPermissions();
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_settings);
        item.setVisible(false);
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_home) {

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_settings) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tanTimerButton:
                startTimerActivity();
                break;
            case R.id.todaysForecastButton:
                break;
            case R.id.tanPlanButton:
                break;
            case R.id.localSeasonButton:
                startLocalSeasonActivity();
                break;
            case R.id.recommendedProductsButton:
                break;
        }
    }

    public void startTimerActivity() {
        int skinTone = localStorageWorker.loadSkinToneSP();
        double UVI = forecast.get(0).getUVI();

        Intent intent = new Intent(this, TanTimerSetupActivity.class);
        intent.putExtra("UVI", UVI);
        intent.putExtra("skinTone", skinTone);
        startActivity(intent);
    }

    public void startLocalSeasonActivity() {
        Intent intent = new Intent(this, LocalSeasonActivity.class);
        intent.putExtra("localClimateList", localClimateList);
        startActivity(intent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.i(TAG, "MainActivity: onRequestPermissionResult");
        int REQUEST_CODE = 01;
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length <= 0) {
                //interrupted interactions result in empty arrays
                Log.i(TAG, "com.example.musket.Classes.User interaction was cancelled");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted, updates requested, starting location updates");
                getCurrentLocation();
            } else {
                //permission denied
                showSnackbar(R.string.permission_denied_explanation,
                        R.string.settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void getCurrentLocation() {
        locationWorker.getFusedLocationClient().getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    mCurrentLocation = task.getResult();
                    localClimateList = buildClimateMap();
                    weatherWorker = new WeatherWorker(getBaseContext(), mCurrentLocation, new WeatherWorker.ForecastListener() {
                        @Override
                        public void onForecastReady(ArrayList<dayObject> fc) {
                            forecast = fc;
                        }
                    });
                    weatherWorker.execute();
                }
            }
        });
    }

    public void showSnackbar(final int mainTextStringId, final int actionStringId, View.OnClickListener listener) {
        Snackbar.make(
                this.findViewById(android.R.id.content),
                this.getResources().getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(this.getResources().getString(actionStringId), listener).show();
    }

    public void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    //this method is so fucking gross tbh, but it works so don't touch
    public ArrayList<Integer> buildClimateMap() {
        ArrayList<String> returnRay = new ArrayList<>();

        InputStream inputStream = getResources().openRawResource(R.raw.uv_by_latitude);
        CSVFile csvFile = new CSVFile(inputStream);
        ArrayList<ArrayList> returnList = csvFile.read();
        returnList.remove(0);
        ArrayList<String> currentLatList = new ArrayList<>();
        currentLatList.add(String.valueOf(Math.round(mCurrentLocation.getLatitude())));
        returnList.add(currentLatList);
        Collections.sort(returnList, new Comparator<ArrayList>() {
            @Override
            public int compare(ArrayList o1, ArrayList o2) {
                //unnecessary boxing ftw
                Integer lat1 = Integer.parseInt(o1.get(0).toString());
                Integer lat2 = Integer.parseInt(o2.get(0).toString());
                return lat1.compareTo(lat2);
            }
        });
        int indexCurrent = returnList.indexOf(currentLatList);
        if (indexCurrent == returnList.size()) {
            //its at the top, grab one down
            returnList.get(indexCurrent - 1);
        } else if (indexCurrent == 0) {
            //its at the bottom, grab one up
            returnList.get(indexCurrent + 1);
        } else {
            //somewhere in the middle, find which is closer
            int a = Integer.parseInt((String) returnList.get(indexCurrent + 1).get(0));
            int c = Integer.parseInt(currentLatList.get(0));
            int b = Integer.parseInt((String) returnList.get(indexCurrent - 1).get(0));

            int absAC = Math.abs(a - c);
            int absBC = Math.abs(b - c);

            //use list with shorter abs value

            if (absAC < absBC) {
                //a
                returnRay = returnList.get(indexCurrent + 1);
            } else if (absBC < absAC) {
                //b
                returnRay = returnList.get(indexCurrent - 1);
            } else {
                //equivalent distance, pick one
                if (Math.round(Math.random()) % 2 == 0) {
                    //a
                    returnRay = returnList.get(indexCurrent + 1);
                } else {
                    //b
                    returnRay = returnList.get(indexCurrent - 1);
                }
            }
        }
        returnRay.remove(0);
        ArrayList<Integer> returnRayInt = new ArrayList<>();
        for (String s : returnRay) {
            returnRayInt.add(Integer.parseInt(s));
        }
        return returnRayInt;
    }

    //util class for reading csv climate file, maybe move to another file?
    public class CSVFile {
        InputStream inputStream;

        public CSVFile(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        public ArrayList<ArrayList> read() {
            ArrayList<ArrayList> resultList = new ArrayList();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String csvLine;
                while ((csvLine = reader.readLine()) != null) {
                    ArrayList<String> row = new ArrayList<>(Arrays.asList(csvLine.split(",")));
                    resultList.add(row);
                }
            } catch (IOException ex) {
                throw new RuntimeException("Error in reading CSV file: " + ex);
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new RuntimeException("Error while closing input stream: " + e);
                }
            }
            return resultList;
        }
    }

}





/*
##DIRELECT IMPLEMENTATIONS
##DECIDED THAT THERE IS NO NEED FOR A USER ACCOUNT AND CAN DO EVERYTHING LOCALLY
##WISH ME LUCK  09/08/2019


    TextView userNameTV, subtitleTV;
    ImageView profileImageView;
        private static final int RC_SIGN_IN = 9001;



public void initLayout(){
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        tanTimerIB = findViewById(R.id.tanTimerButton);
        forecastIB = findViewById(R.id.todaysForecastButton);
        tanPlanIB = findViewById(R.id.tanPlanButton);
        localIB = findViewById(R.id.localSeasonButton);
        recoProdIB = findViewById(R.id.recommendedProductsButton);

        tanTimerIB.setOnClickListener(this);
        forecastIB.setOnClickListener(this);
        tanPlanIB.setOnClickListener(this);
        localIB.setOnClickListener(this);
        recoProdIB.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        drawer.post(new Runnable() {
            @Override
            public void run() {
                updateUI(mAuth.getCurrentUser());

            }
        });
        if(!locationWorker.checkPermissions()){
            locationWorker.requestPermissions();
        }
    }

    private void updateUI(FirebaseUser user){
        userNameTV = findViewById(R.id.userNameTextView);
        subtitleTV = findViewById(R.id.subtitleTextView);
        profileImageView = findViewById(R.id.profilePictureNavHeader);
        if(user == null){
            userNameTV.setText(R.string.nav_header_title);
            subtitleTV.setText(R.string.nav_header_subtitle);
            return;
        }else{
            userNameTV.setText(mAuth.getCurrentUser().getDisplayName());
            subtitleTV.setText(mAuth.getCurrentUser().getEmail());
            LocalStorageWorker_old lsWorker = new LocalStorageWorker_old(this);

            //T0D0: get profile picture to load properly after account creation
            Bitmap profPic=lsWorker
                    .setFileName(mAuth.getCurrentUser().getUid())
                    .setDirectoryName("images")
                    .loadImage();
            profileImageView.setImageBitmap(profPic);
        }
    }


        private void startSignIn(){
        //T0D0 ENABLE MORE FEDERATED SIGN INS https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md
        startActivityForResult(AuthUI.getInstance()
        .createSignInIntentBuilder()
                //.setLogo(R.drawable.ic_adb_copper)
        .setAvailableProviders(Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.PhoneBuilder().build()))
                .build(),RC_SIGN_IN);
    }

    private void signOut() {
        AuthUI.getInstance().signOut(this);
        updateUI(null);
    }



     public void openCreateAccountDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false).setTitle("You need an account!")
                .setMessage("You must create an account to continue")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startSignIn();

                    }
                });
        Dialog dia = builder.create();
        dia.setCanceledOnTouchOutside(false);
        dia.setCancelable(false);
        dia.show();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            IdpResponse response = IdpResponse.fromResultIntent(data);
            //successful sign in
            if(resultCode == RESULT_OK){
                showToast("Sign in successful");
                updateUI(mAuth.getCurrentUser());
                Map<String, Object> user = new HashMap<>();
                user.put("userID", mAuth.getCurrentUser().getUid());
                user.put("email",mAuth.getCurrentUser().getEmail());
                user.put("name", mAuth.getCurrentUser().getDisplayName());
                db.collection("users").document(mAuth.getCurrentUser().getUid()).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "User added to Firestore");
                        }else{
                            Log.e(TAG, "Failed to add user to firestore");
                        }}});
                Intent intent = new Intent(MainActivity.this, AccountSetupActivity.class);
                startActivity(intent);
            }else{
                //sign in failed
                if(response == null){
                    showToast("Sign in cancelled");
                    return;
                }
                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    showToast("No internet connection");
                    return;
                }
                showToast("An unknown error occurred");
                Log.e(TAG, "Sign-in error: ", response.getError());
            }
        }

    }

 */
