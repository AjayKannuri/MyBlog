package com.example.hp.imageview;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import static android.R.attr.data;

public class setupActivity extends AppCompatActivity {
    private ImageButton mSetupImageBtn;
    private EditText mNameField;
    private Button mSubmitBtn;
    private ProgressDialog PD;
    private FirebaseAuth mAuth;
    private Uri mImageUri=null;
    private DatabaseReference mDatabaseusers;
    private StorageReference mStorageImage;
    private static final int Gallery_request=1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        mSetupImageBtn =(ImageButton)findViewById(R.id.SetupImageBtn);
        mNameField =(EditText)findViewById(R.id.setupNameField);
        mAuth=FirebaseAuth.getInstance();
        mStorageImage = FirebaseStorage.getInstance().getReference().child("profile_images");
        mDatabaseusers = FirebaseDatabase.getInstance().getReference().child("Users");
        mSubmitBtn =(Button)findViewById(R.id.setupFinishBtn);
        mSetupImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent =new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,Gallery_request);
            }
        });
        mSubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startSetupAccount();
            }
        });
    }

    private void startSetupAccount() {
            final String name=mNameField.getText().toString().trim();
            final String user_id =mAuth.getCurrentUser().getUid();
            if(!TextUtils.isEmpty(name) && mImageUri!=null) {
                if(PD == null){
                //
                PD = new ProgressDialog(this);
                PD.setMessage("uploading the image");PD.setIndeterminate(false);
                PD.setCancelable(true);
                PD.show();
                }
                StorageReference filepath=mStorageImage.child(mImageUri.getLastPathSegment());
                filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            String downloadUrl=taskSnapshot.getDownloadUrl().toString();
                            mDatabaseusers.child(user_id).child("profile_name").setValue(name);
                            mDatabaseusers.child(user_id).child("profile_pic").setValue(downloadUrl);
                            PD.dismiss();
                        Intent mainIntent = new Intent(setupActivity.this, MainActivity.class);
                        //mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(mainIntent);
                    }
                });
            }
            else{
                Toast.makeText(getApplicationContext(),"select image and enter ur name", Toast.LENGTH_SHORT).show();
            }
    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==Gallery_request && resultCode==RESULT_OK)
        {       Uri imageUri = data.getData();
                 mImageUri=imageUri;
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                mSetupImageBtn.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

}
