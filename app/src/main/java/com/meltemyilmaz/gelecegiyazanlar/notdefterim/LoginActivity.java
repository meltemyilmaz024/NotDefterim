package com.meltemyilmaz.gelecegiyazanlar.notdefterim;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText inputEmail, inputPassword;
    private Button btn_sistemeGiris, btn_yeniKayıt;
    private ProgressBar progressBar;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initialize();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check auth on Activity start
        if (mAuth.getCurrentUser() != null) {
            onAuthSuccess(mAuth.getCurrentUser());
        }
    }

    private void initialize(){

        mDatabase = FirebaseDatabase.getInstance().getReference();
        // Get Firebase Auth Instance
        mAuth = FirebaseAuth.getInstance();

        btn_yeniKayıt = (Button) findViewById(R.id.btn_yeniKayıt);
        btn_sistemeGiris = (Button) findViewById(R.id.btn_sistemeGiris);

        inputEmail = (EditText) findViewById(R.id.editTextEmail);
        inputPassword = (EditText) findViewById(R.id.editTextSifre);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        btn_sistemeGiris.setOnClickListener(this);
        btn_yeniKayıt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

            switch (v.getId()){

                case R.id.btn_sistemeGiris:
                    sistemeGiris();        // Bu sayfayı kapat.
                    break;

                case R.id.btn_yeniKayıt:
                    KayıtSayfasınaGit();
                    break;
            }
    }

    private void sistemeGiris(){

        String email = inputEmail.getText().toString();
        final String password = inputPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // authenticate user
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        progressBar.setVisibility(View.GONE);

                        if(task.isSuccessful()){
                            onAuthSuccess(task.getResult().getUser());
//                            Toast.makeText(LoginActivity.this,
//                                    "Giriş başarılı, onAuth çalıştı",
//                                    Toast.LENGTH_LONG).show();
                        }else{
                            // there was an error
                            if (password.length() < 6) {
                                inputPassword.setError("Şifreniz çok kısa, lütfen minimum 6 karakter giriniz");
                            } else {
                                Toast.makeText(LoginActivity.this, "Giriş yapılamadı, lütfen yeniden deneyiniz..", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);
        mDatabase.child("users").child(userId).setValue(user);
    }

    private void onAuthSuccess(FirebaseUser user) {

        String username = usernameFromEmail(user.getEmail());

        // Write new user
        writeNewUser(user.getUid(), username, user.getEmail());

        // Go to MainActivity
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    private void KayıtSayfasınaGit(){
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);
    }
}
