package hcmute.edu.project_06_fishclassification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePassActivity extends AppCompatActivity {
    private FirebaseAuth authProfile;
    private EditText editTextPwdCurr, editTextPwdNew, editTextPwdConfirmNew;
    private TextView textViewAuthenticated;
    private MaterialButton buttonChangePwd, buttonReAuthenticate;
    private ProgressBar progressBar, progressBarChangePwd;
    private String userPwdCurr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changepass);

        editTextPwdNew = findViewById(R.id.input_nPass);
        editTextPwdCurr = findViewById(R.id.input_oPass);
        editTextPwdConfirmNew = findViewById(R.id.input_rPass);

        progressBar = findViewById(R.id.progressBar1);
        progressBarChangePwd = findViewById(R.id.progressBar);
        buttonChangePwd = findViewById(R.id.btn_changePass);
        buttonReAuthenticate = findViewById(R.id.btn_authUser);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        if(firebaseUser.equals("")){
            Toast.makeText(ChangePassActivity.this,"Something wrong! User null", Toast.LENGTH_LONG).show();
            onBackPressed();
        }else{
            reAuthenticateUser(firebaseUser);
        }
    }

    private void reAuthenticateUser(FirebaseUser firebaseUser) {
        buttonReAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userPwdCurr = editTextPwdCurr.getText().toString();

                if(TextUtils.isEmpty(userPwdCurr)){
                    Toast.makeText(ChangePassActivity.this,"Pass is needed", Toast.LENGTH_SHORT).show();
                    editTextPwdCurr.setError("Please enter your current pass to authenticate");
                    editTextPwdCurr.requestFocus();
                }else{
                    progressBar.setVisibility(View.VISIBLE);

                    AuthCredential credential = EmailAuthProvider.getCredential(firebaseUser.getEmail(), userPwdCurr);

                    firebaseUser.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                progressBar.setVisibility(View.GONE);

                                editTextPwdCurr.setEnabled(false);
                                editTextPwdNew.setEnabled(true);
                                editTextPwdConfirmNew.setEnabled(true);

                                buttonChangePwd.setEnabled(true);
                                buttonReAuthenticate.setText("DONE");
                                buttonReAuthenticate.setEnabled(false);

                                Toast.makeText(ChangePassActivity.this,"Authenticated", Toast.LENGTH_SHORT).show();

                                buttonChangePwd.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        changePwd(firebaseUser);
                                    }
                                });
                            }else{
                                try{
                                    throw task.getException();
                                }catch (Exception e){
                                    Toast.makeText(ChangePassActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        });
    }

    private void changePwd(FirebaseUser firebaseUser) {
        String userPwdNew = editTextPwdNew.getText().toString();
        String userPwdConfirmNew = editTextPwdConfirmNew.getText().toString();

        if(TextUtils.isEmpty(userPwdNew)){
            Toast.makeText(ChangePassActivity.this,"New pass is needed", Toast.LENGTH_SHORT).show();
            editTextPwdNew.setError("Please fill this");
            editTextPwdNew.requestFocus();
        } else if (TextUtils.isEmpty(userPwdConfirmNew)) {
            Toast.makeText(ChangePassActivity.this,"Confirm pass is needed", Toast.LENGTH_SHORT).show();
            editTextPwdConfirmNew.setError("Please fill this");
            editTextPwdConfirmNew.requestFocus();
        } else if (!userPwdNew.matches(userPwdConfirmNew)) {
            Toast.makeText(ChangePassActivity.this,"Confirm pass did not match", Toast.LENGTH_SHORT).show();
            editTextPwdConfirmNew.setError("Please re-enter same pass");
            editTextPwdConfirmNew.requestFocus();
        } else if (!userPwdCurr.matches(userPwdNew)) {
            Toast.makeText(ChangePassActivity.this,"New pass cannot be same as old pass", Toast.LENGTH_SHORT).show();
            editTextPwdNew.setError("Please enter a new pass");
            editTextPwdNew.requestFocus();
        }else{
            progressBarChangePwd.setVisibility(View.VISIBLE);

            firebaseUser.updatePassword(userPwdNew).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChangePassActivity.this,"Update password successfully", Toast.LENGTH_LONG).show();
                        onBackPressed();
                    }else{
                        try{
                            throw task.getException();
                        }catch (Exception e){
                            Toast.makeText(ChangePassActivity.this,e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                    progressBarChangePwd.setVisibility(View.GONE);
                }
            });
        }
    }
}