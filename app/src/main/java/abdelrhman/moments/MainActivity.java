package abdelrhman.moments;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new CameraFragment())
                    .commit();
        }

        ListView mDrawerList;
        ArrayAdapter<String> mAdapter;
        mDrawerList = (ListView)findViewById(R.id.navList);
        String[] Array = { "New Moment", "My Moments", "Settings"};
        mAdapter = new ArrayAdapter<>(this, R.layout.drawer_list_item, Array);
        mDrawerList.setAdapter(mAdapter);

    }

}
