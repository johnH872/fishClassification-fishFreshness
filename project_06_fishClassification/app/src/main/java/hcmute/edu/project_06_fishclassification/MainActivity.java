package hcmute.edu.project_06_fishclassification;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import hcmute.edu.project_06_fishclassification.fragments.HomeFragment;
import hcmute.edu.project_06_fishclassification.fragments.PersonalFragment;

public class MainActivity extends AppCompatActivity {
    ActivityResultLauncher<Intent> activityResultLauncher;
    FloatingActionButton actionButton;
    String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    FirebaseUser user;
    FirebaseAuth auth;
    Bitmap fishImgBitmap;
    FragmentManager fragmentManager;
    private BottomNavigationView bottomNavigation;
    final HomeFragment homeFragment = new HomeFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        actionButton = findViewById(R.id.btn_camera);
        //get camera permission
        getPermission();
        //file permission
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (Environment.isExternalStorageManager()) {
                            Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                        } else  {
                            Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        if (checkPermission()) {
            Log.e("Permission","permission granted");
        } else {
            requestPermission();
        }
        //Kiểm tra đã đăng nhập chưa, nếu chưa thì đăng xuất
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }

        bottomNavigation = findViewById(R.id.bottom_nav);
        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.frameLayout, HomeFragment.class, null )
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();

        bottomNavigation.setOnItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.tab_home:
                    fragmentManager.beginTransaction()
                            .replace(R.id.frameLayout, HomeFragment.class, null )
                            .setReorderingAllowed(true)
                            .addToBackStack(null)
                            .commit();
                    return true;
                case R.id.tab_personal:
                    fragmentManager.beginTransaction()
                            .replace(R.id.frameLayout, PersonalFragment.class, null )
                            .setReorderingAllowed(true)
                            .addToBackStack(null)
                            .commit();
                    return true;
                default:
                    return false;
            }
        });

        actionButton.setOnClickListener(view -> {
            Intent intentG = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            captureImageLauncher.launch(intentG);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void getPermission() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 11) {
            if(grantResults.length > 0) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) this.getPermission();
            }
        }
        if (requestCode == 30) {
            if (grantResults.length>0) {
                boolean readper = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                boolean writeper = grantResults[1]==PackageManager.PERMISSION_GRANTED;
                if (readper && writeper) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "You Denied Permission", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    void requestPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s", getApplicationContext().getPackageName())));
                activityResultLauncher.launch(intent);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                activityResultLauncher.launch(intent);
            }
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, permission, 30);
        }
    }

    boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int readCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
            int writeCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return readCheck == PackageManager.PERMISSION_GRANTED && writeCheck == PackageManager.PERMISSION_GRANTED;
        }
    }

    ActivityResultLauncher captureImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri uri = data.getData();
                        fishImgBitmap = (Bitmap) data.getExtras().get("data");
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        fishImgBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byte[] byteArray = stream.toByteArray();
                        Bundle bundle = new Bundle();
                        bundle.putByteArray("fishImageFromMainActivity",byteArray);
                        homeFragment.setArguments(bundle);
                        fragmentManager.beginTransaction()
                                .replace(R.id.frameLayout, homeFragment, null )
                                .setReorderingAllowed(true)
                                .addToBackStack(null)
                                .commit();
                    }
                }
            });

}