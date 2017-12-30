package com.example.hp.imageview;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class post extends AppCompatActivity {
    private Button b1,b2;
    public EditText titleField,descField;
    String title,desc;
    private int Gallery_req=2;
    private ImageView imageView;
    private StorageReference mStorage;
    private DatabaseReference mDataBase;
    private ProgressDialog PD;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabaseUser;
     Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        b1=(Button)findViewById(R.id.img);
        b2=(Button)findViewById(R.id.sub);
        mAuth = FirebaseAuth.getInstance();
        mStorage=FirebaseStorage.getInstance().getReference();
        mDataBase= FirebaseDatabase.getInstance().getReference().child("blog");
        titleField=(EditText)findViewById(R.id.titleField);
        descField=(EditText)findViewById(R.id.descField);
        mAuth=FirebaseAuth.getInstance();
        mCurrentUser=mAuth.getCurrentUser();
        mDatabaseUser= FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        imageView=(ImageView)findViewById(R.id.imageView);
    }
   /* @Override
    public void onStart()
    {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // do your stuff
        } else {
            signInAnonymously();
        }

    }
    public void signInAnonymously(){
        mAuth.signInAnonymously().addOnSuccessListener(this, new  OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                // do your stuff
            }
        }).addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.e("failed bro", "signInAnonymously:FAILURE", exception);
                    }
                });
    }*/

    public void startPost(View view)
    {   try {
        title =titleField.getText().toString();
        desc = descField.getText().toString();
    }catch(NullPointerException n){
        n.printStackTrace();
    }
        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(desc) && imageUri!=null)
            startPosting();
        else{
            Toast.makeText(getApplicationContext(),"cant start posting", Toast.LENGTH_SHORT).show();
        }
    }
    public void startPosting()
    { try {
        if(PD == null){

            //
            PD = new ProgressDialog(post.this);
            PD.setMessage("Please Wait..");
            PD.setIndeterminate(false);
            PD.setCancelable(true);
            PD.show();
        }


        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(desc) && imageUri!=null) {
            StorageReference filepath = mStorage.child("Blog_image").child(imageUri.getLastPathSegment());
            filepath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                  @SuppressWarnings("VisibleForTests")  final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                  final DatabaseReference newPost=mDataBase.push();
                  // newPost.child("title").setValue(title);
                   // newPost.child("desc").setValue(desc);
                    //newPost.child("image").setValue(downloadUrl.toString());
                    //newPost.child("uid").setValue(mCurrentUser.getUid());
                    //newPost.child("username").setValue(mDatabaseUser.child("name"));
                  mDatabaseUser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                           newPost.child("title").setValue(title);
                           newPost.child("desc").setValue(desc);
                            newPost.child("image").setValue(downloadUrl.toString());
                            newPost.child("uid").setValue(mCurrentUser.getUid());
                            newPost.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "uploading is successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(post.this, MainActivity.class));
                                    }
                                    else{
                                        Toast.makeText(getApplicationContext(), "uploading is not successful", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });
            Toast.makeText(getApplicationContext(), "uploading is successful", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(post.this, MainActivity.class));

        }
        if(PD.isShowing()){
            PD.dismiss();
        }


    }
    catch(Exception e){
        e.printStackTrace();
    }

    }
    public void imgClick(View view)
    {
        Intent intent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, Gallery_req);
            Toast.makeText(getApplicationContext(),"select a pic bro", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Gallery_req && resultCode == RESULT_OK && data!=null) {
            try {
                 imageUri = data.getData();
                 InputStream imageStream = getContentResolver().openInputStream(imageUri);
                 Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                Bitmap.createScaledBitmap(selectedImage, 100, 100, true);
                imageView.setImageBitmap(selectedImage);
                /*imageView.setImageBitmap(
                        decodeSampledBitmapFromResource(getResources(), R.id.imageView, 100, 100));*/
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}

