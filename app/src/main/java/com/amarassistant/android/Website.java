package com.amarassistant.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.efortshub.webview.weblibrary.WebCondition;
import com.efortshub.webview.weblibrary.WebListener;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

public class Website extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    //Variables
    static final float END_SCALE = 1f;

    private ImageView menuIcon;
    private com.efortshub.webview.weblibrary.WebView webView;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    LinearLayout contentView;
    AlertDialog.Builder ExitDialog;
    private LinearLayout NoInternet;

    private WebListener webListener;
    private ValueCallback<Uri[]> webFilePathCallback;
    private PermissionRequest permissionRequest;

    private static final String TAG = "Website";

    //Drawer Menu
    DrawerLayout drawerLayout;
    NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        setContentView(R.layout.activity_website);

        NoInternet = findViewById(R.id.noInternet);
        //Assign variable
        webView = findViewById(R.id.websiteView);
        progressBar = findViewById(R.id.progress_Bar);
        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        contentView = findViewById(R.id.content);
        menuIcon = findViewById(R.id.menu_icon);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);

        navigationDrawer();
    }
    //Navigation Drawer Functions
    private void navigationDrawer() {
        //Navigation Hocks
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);


        Menu m=navigationView.getMenu();
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (drawerLayout.isDrawerVisible(GravityCompat.START))
                    drawerLayout.closeDrawer(GravityCompat.START);
                else drawerLayout.openDrawer(GravityCompat.START);
            }
        });


        //Initialize connectivityManager
        final ConnectivityManager connectivityManager = (ConnectivityManager)
                getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);



        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();


        if (networkInfo == null || !networkInfo.isConnected() || !networkInfo.isAvailable()) {


            //Initialize dialog
            Dialog dialog = new Dialog(this);
            //Set content view
            dialog.setContentView(R.layout.alert_dialog);

            //Set outside touch
            dialog.setCanceledOnTouchOutside(false);
            //Set dialog width and height
            dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
            //Set transparent background
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


            //Set animation
            dialog.getWindow().getAttributes().windowAnimations =
                    android.R.style.Animation_Dialog;



            Button btTryAgain = dialog.findViewById(R.id.bt_try_again);

            //Perform onClick listener
            btTryAgain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Call recreate methode
                    recreate();
                }
            });

            //Show dialog
            dialog.show();
        } else {
            //When internet is active

            webView.setScrollbarFadingEnabled(true);
            webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

            initializeWebView();

            //load  url webView

            webView.loadUrl("https://amarassistant.com");
           // webView.loadUrl("https://google.com");




            swipeRefreshLayout.setOnRefreshListener(() -> {
                swipeRefreshLayout.setRefreshing(true);
                new Handler().postDelayed(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    webView.reload();
                }, 1500);

            });

            swipeRefreshLayout.setColorSchemeColors(getResources().getColor(android.R.color.black), getResources().getColor(android.R.color.holo_blue_dark));


        }
        if (Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                Log.d("permission", "permission denied to WRITE_EXTERNAL_STORAGE -requesting it");
                String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions,1);
            }
        }


    }







    private void initializeWebView() {

        WebCondition.applyWebSettings(webView);

        if (webListener==null) {
            webListener = new WebListener() {
                @Override
                public void onPageStarted(WebView webView, String url, Bitmap favicon) {

                    Log.d(TAG, "onPageStarted: "+url);

                }

                @Override
                public void onPageStopped(WebView webView, String url) {
                    Log.d(TAG, "onPageStopped: ");

                }

                @Override
                public void onProgressChanged(WebView webView, int progress) {

                    progressBar.setVisibility(View.VISIBLE);

                    progressBar.setProgress(progress);
                    if (progress == 100) {
                        progressBar.setVisibility(View.GONE);
                    }
                    Log.d(TAG, "onProgressChanged: "+progress);

                }

                @Override
                public void onPermissionRequest(PermissionRequest permissionRequest) {
                    //keep this listener blank, bcoz I have done this functionality via webviewHelper library
                }

                @Override
                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                    Log.d(TAG, "onReceivedSslError: ");


                }

                @Override
                public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {

                    Log.d(TAG, "onShowFileChooser: ");

                    webFilePathCallback = filePathCallback;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        String[] s =  fileChooserParams.getAcceptTypes();
                        for (String a: s){
                            Log.d(TAG, "onShowFileChooser: s :"+a);
                        }
                        Intent intent = fileChooserParams.createIntent();
                        startActivityForResult(intent, 32);
                    }
                    return true;
                }

                @Override
                public boolean checkPermission(PermissionRequest request, List<String> permissions) {

                    permissionRequest = request;

                    Log.d(TAG, "checkPermission: "+permissions.get(0));
                    boolean isAllGranted =  WebCondition.requestNewPermission(

                            Website.this,
                            Website.this,
                            permissions);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                        request.grant(request.getResources());

                    }

                    Log.d(TAG, "checkPermission: "+isAllGranted);

                    return isAllGranted;
                }

                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

                    Toast.makeText(Website.this, description, Toast.LENGTH_SHORT).show();

                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {

                    if (url.startsWith("mailto:")) {
                        startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(url)));
                        return true;

                    } else if (url.contains("facebook.com") || url.contains("twitter.com")) {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(i);
                        return true;

                    }else if (url != null && url.startsWith("https://wa.me")) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url.replace("+", ""))));

                        return true;

                    }


                    return  false;

                }

                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        String url = request.getUrl().toString();


                        if (url.startsWith("mailto:")) {
                            startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(url)));
                            return true;

                        } else if (url.contains("facebook.com") || url.contains("twitter.com")) {
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(i);
                            return true;

                        }else if (url != null && url.startsWith("https://wa.me")) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url.replace("+", ""))));

                            return true;

                        }

                    }

                    return false;
                }
            };

        }
        WebCondition webCondition = WebCondition.getInstance(webListener);

        webView.setWebCondition(webCondition);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == 32 && resultCode==RESULT_OK) {
            Log.d(TAG, "onActivityResult: reuslt got : "+data.getData());

            if (webFilePathCallback!=null){
                webFilePathCallback.onReceiveValue(new Uri[]{data.getData()});
            }

            webFilePathCallback = null;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode== WebCondition.PERMISSION_CODE){
            List<String> notGrantedPermissions = WebCondition.getNotGrantedPermissions(permissions, grantResults);

            if (!notGrantedPermissions.isEmpty()){
                WebCondition.showPermissionNotGrantedDialog(Website.this,Website.this,  notGrantedPermissions);
            }
        }

    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerVisible(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            loadExitDialog();
        }

    }

    public void loadExitDialog(){
        ExitDialog=new AlertDialog.Builder(Website.this);
        ExitDialog.setMessage("Do you want to Exit");
        ExitDialog.setIcon(R.drawable.exit);
        ExitDialog.setTitle("Amar Assistant");
        ExitDialog.setCancelable(false);
        ExitDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        ExitDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        AlertDialog alertDialog = ExitDialog.create();
        alertDialog.show();
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.nav_home:
                Intent intent = new Intent(getApplicationContext(), Website.class);
                startActivity(intent);

                finish();

        }

        switch (item.getItemId()) {


            case R.id.nav_exit:
                finish();


        }


        switch (item.getItemId()) {

            case R.id.nav_share:
                Intent shareintent = new Intent();
                shareintent.setAction(Intent.ACTION_SEND);
                shareintent.putExtra(Intent.EXTRA_SUBJECT, "Install latest AmarAssistant.com app.");
                shareintent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.amarassistant.android");
                shareintent.setType("text/plain");
                startActivity(Intent.createChooser(shareintent, "share via"));
                break;

            case R.id.nav_rate:
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.amarassistant.android");
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                try {
                    startActivity(intent);
                }catch (Exception e){
                    Toast.makeText(this,"Unable to open\n"+e.getMessage(),Toast.LENGTH_SHORT).show();
                }
                break;

        }




        return true;
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}


