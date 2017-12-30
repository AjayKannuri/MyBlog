package com.example.hp.imageview;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import android.speech.tts.TextToSpeech;


import org.w3c.dom.Text;

import java.util.Locale;

//import static com.google.android.gms.internal.zzbco.NULL;
import static com.google.android.gms.internal.zzben.NULL;

public class BlockSingleActivity extends AppCompatActivity {
    private String mPostKey=null;
    private DatabaseReference mDataBase,mDatabaseUsers,mDatabaseLike;
    private FirebaseAuth mAuth;
    private ImageView mBlogSingleImage;
    private TextView mBlogSingleTitle;
    private TextView mBlogSingleDesc;
    private Button mPost_del;
    private Button Post_speech;
    String post_uid;
    String post_desc;
    TextToSpeech TTS;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_single);
        mPostKey = getIntent().getExtras().getString("blog id");
        mPost_del=(Button)findViewById(R.id.post_delete);
        mAuth =FirebaseAuth.getInstance();
        mDataBase = FirebaseDatabase.getInstance().getReference().child("blog");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likings");
        mDatabaseUsers.keepSynced(true);
        mDatabaseLike.keepSynced(true);
        mDataBase.keepSynced(true);
        mBlogSingleImage = (ImageView) findViewById(R.id.blog_image);
        Post_speech=(Button)findViewById(R.id.speak_pls);
        mBlogSingleTitle = (TextView) findViewById(R.id.blog_title);
        mBlogSingleDesc = (TextView) findViewById(R.id.blog_desc);
        TTS = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    TTS.setLanguage(Locale.ENGLISH);
                    TTS.setPitch(0);
                    TTS.setSpeechRate((float) 0.0001);
                }
            }
        });

        //Toast.makeText(getApplicationContext(),mAuth.getCurrentUser().getUid(), Toast.LENGTH_SHORT).show();
        try {
            mDataBase.child(mPostKey).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String post_title = (String) dataSnapshot.child("title").getValue();
                     post_desc = (String) dataSnapshot.child("desc").getValue();
                    String post_image = (String) dataSnapshot.child("image").getValue();
                     post_uid = (String) dataSnapshot.child("uid").getValue();
                    mBlogSingleTitle.setText(post_title);
                   /* if(post_title=NULL){
                        Intent loginIntent = new Intent(BlockSingleActivity.this, MainActivity.class);
                        Toast.makeText(getApplicationContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                        startActivity(loginIntent);

                    }*/
                   // mBlogSingleDesc.setVisibility(View.INVISIBLE);
                    if(!mAuth.getCurrentUser().getUid().equals(post_uid)){
                        mPost_del.setVisibility(View.INVISIBLE);
                    }
                    mBlogSingleDesc.setText(post_desc);
                    Picasso.with(getApplicationContext()).load(post_image).into(mBlogSingleImage);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
           // Toast.makeText(getApplicationContext(), "we entered into the blogSingleActivity", Toast.LENGTH_SHORT).show();
        }catch(Exception e){
            e.printStackTrace();
        }

        mPost_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAuth.getCurrentUser().getUid().equals(post_uid)) {
                    mDataBase.child(mPostKey).removeValue();
                    mDatabaseLike.child(mPostKey).removeValue();
                    Intent loginIntent = new Intent(BlockSingleActivity.this, MainActivity.class);
                    Toast.makeText(getApplicationContext(), "Deleted Successfully", Toast.LENGTH_SHORT).show();
                    startActivity(loginIntent);
                }

            }
        });
        Post_speech.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            public void onClick(View view) {
                TTS.stop();
                String speechStr = post_desc;
                TTS.speak(speechStr, 0, null, "1");
            }
        });
    }

}
