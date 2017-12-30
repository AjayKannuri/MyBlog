package com.example.hp.imageview;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private EditText mNameField;
    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mRegisterBtn;
    private FirebaseAuth mAuth;
    private ProgressDialog PD;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mNameField =(EditText)findViewById(R.id.nameField);
        mAuth=FirebaseAuth.getInstance();
        mDatabase= FirebaseDatabase.getInstance().getReference().child("Users");
        mEmailField=(EditText)findViewById(R.id.emailField);
        mPasswordField =(EditText)findViewById(R.id.passwordField);
        mRegisterBtn=(Button)findViewById(R.id.registerBtn);
    }
    public void startRegister(View view){
        final String name= mNameField.getText().toString().trim();
        final String email=mEmailField.getText().toString().trim();
        final String password=mPasswordField.getText().toString().trim();
        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password))
        {
            if(PD == null){

                //
                PD = new ProgressDialog(this);
                PD.setMessage("signing up");
                PD.setIndeterminate(false);
                PD.setCancelable(true);
                PD.show();
            }
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        String user_id=mAuth.getCurrentUser().getUid();
                        DatabaseReference current_user_db= mDatabase.child(user_id);
                        current_user_db.child("name").setValue(name);
                        current_user_db.child("image").setValue("default");
                        Toast.makeText(getApplicationContext(),"uploading is successful", Toast.LENGTH_SHORT).show();
                        Intent newIntent =new Intent(RegisterActivity.this,LoginActivity.class);
                        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(newIntent);
                    }
                }
            });
            if(PD.isShowing()){
                PD.dismiss();
            }
        }
        else{
            Toast.makeText(getApplicationContext(),"PLEASE ENTER THE FIELDS", Toast.LENGTH_SHORT).show();
        }

    }
}
