package com.example.souhaib100.marfspeakeridentapp;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.souhaib100.marfspeakeridentapp.fragments.RecognizeFragment;
import com.example.souhaib100.marfspeakeridentapp.fragments.TrainingFragment;
import com.example.souhaib100.marfspeakeridentapp.marf.SpeakerIdentApp;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final String[] permissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.navigation_recognize:
                    selectedFragment = RecognizeFragment.newInstance();
                    break;
                case R.id.navigation_training:
                    selectedFragment = TrainingFragment.newInstance();
                    break;
            }
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, selectedFragment);
            transaction.commit();
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //Manually displaying the first fragment - one time only
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, RecognizeFragment.newInstance());
        transaction.commit();

        //Check for permissions
        Dexter.withActivity(this).withPermissions(permissions)
                .withListener(new MultiplePermissionsListener() {
                                  @Override
                                  public void onPermissionsChecked(MultiplePermissionsReport report) {
                                      if (report.areAllPermissionsGranted()) {

                                      } else {
                                          Toast.makeText(getApplication(), "Permissions denied", Toast.LENGTH_LONG).show();
                                          finish();
                                          //onDestroy();
                                      }
                                  }

                                  @Override
                                  public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                                  }
                              }

                ).onSameThread().check();

        SpeakerIdentApp.path = getExternalFilesDir("").getPath();

        //Prepare the files used by MARF
        File file = new File(getExternalFilesDir("").getPath() + File.separator + "speakers.txt");
        if (!file.exists()) {
            copyToFile(file, R.raw.speakers);
        }
        file = new File(getExternalFilesDir("").getPath() + File.separator + "marf.Storage.TrainingSet.107.308.532.gzbin");
        if (!file.exists()) {
            copyToFile(file, R.raw.ds);
        }

    }

    private void copyToFile(File file, int id) {
        try {
            InputStream inputStream = getResources().openRawResource(id);
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            byte buf[] = new byte[1024];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                fileOutputStream.write(buf, 0, len);
            }

            fileOutputStream.close();
            inputStream.close();
        } catch (IOException ignored) {
        }

    }

}
