package abdelrhman.moments;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class MainActivity extends FragmentActivity {

    DrawerLayout drawerLayout;
    ListView drawerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new CameraFragment())
                    .commit();
        }

         drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ArrayAdapter<String> mAdapter;
        drawerList = (ListView)findViewById(R.id.navList);
        String[] Array = { "New Moment", "My Moments", "Settings"};
        mAdapter = new ArrayAdapter<>(this, R.layout.drawer_list_item, Array);
        drawerList.setAdapter(mAdapter);
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chooseItem(position);
            }
        });

    }

    void chooseItem(int position) {
        drawerList.setItemChecked(position, true);
        drawerLayout.closeDrawer(drawerList);
        switch (position){
            case 0: {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new CameraFragment())
                        .commit();
                break;
            }
            case 1: {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new VideosFragment())
                        .commit();
                break;
            }
        }
    }

}
