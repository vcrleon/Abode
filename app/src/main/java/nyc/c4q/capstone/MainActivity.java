package nyc.c4q.capstone;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;


import butterknife.BindView;
import butterknife.ButterKnife;
import nyc.c4q.capstone.BottomNavFragment.DashBoardFragment;
import nyc.c4q.capstone.BottomNavFragment.DocsFragment;

import nyc.c4q.capstone.BottomNavFragment.MaintanceFragment;
import nyc.c4q.capstone.BottomNavFragment.PaymentFragment;
import nyc.c4q.capstone.database.TenantDataBaseHelper;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    @BindView(R.id.container)
    ViewPager viewPager;
    @BindView(R.id.bottom_navigation)
    AHBottomNavigation bottom;

    private ArrayList<AHBottomNavigationItem> items = new ArrayList<>();

    private static final String TAG = "MainActivity";
    public static final String ANONYMOUS = "anonymous";
    private static FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    public static String mUsername;
    private SharedPreferences preferences;
    public static GoogleApiClient googleApiClient;


    private static final int NOTIFICATION_ID = 555;
    String NOTIFICATION_CHANNEL = "C4Q Notifications";
    SectionsPageAdapter adapter;

    FirebaseDatabase database =FirebaseDatabase.getInstance();
    TenantDataBaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        bottom = findViewById(R.id.bottom_navigation);
        setBottomNav();
        adapter = new SectionsPageAdapter(getSupportFragmentManager());
        db = TenantDataBaseHelper.getInstance(database);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUsername = ANONYMOUS;
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        checkSignIn();

        long unixTime = System.currentTimeMillis() / 1000L;
    }

    public void checkSignIn() {
        if (firebaseUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = firebaseUser.getDisplayName();
            Log.e("User",firebaseUser.getUid());
            db.getUserInfoFromDataBase(firebaseUser.getUid());
        }
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();
    }

    public void setBottomNav() {
        setupViewPager(viewPager);
        AHBottomNavigationItem item1 = new AHBottomNavigationItem("DashBoard", R.drawable.dash);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem("Payment", R.drawable.payment);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem("Maintenance", R.drawable.maintenance);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem("Docs", R.drawable.docs);
        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(item4);
        bottom.addItems(items);
        bottom.setCurrentItem(0);
        bottom.setDefaultBackgroundColor(Color.LTGRAY);
        bottom.setAccentColor(Color.parseColor("#52c7b8"));
//        bottom.setColoredModeColors(Color.LTGRAY,Color.parseColor("#52c7b8"));
//        bottom.setAccentColor(Color.parseColor("#52c7b8"));
        bottom.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                try {
                    viewPager.setCurrentItem(position);
                    bottom.setCurrentItem(position, wasSelected);
                    Log.e(TAG, adapter.getCount() + "");
                } catch (StackOverflowError e) {
                    Log.e("Caught Error", e.getMessage());
                }
                return false;
            }
        });
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.initTenanetBottomNav();
        viewPager.setAdapter(adapter);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
