package com.example.hp.imageview;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import android.os.Build;
import android.speech.tts.Voice;
import android.annotation.SuppressLint;
import android.speech.tts.TextToSpeech;
import android.content.Context;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import org.w3c.dom.Text;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {
    private Button b1;
    private RecyclerView mBlogList;
    private DatabaseReference mDataBase, mDatabaseUsers, mDatabaseLike;
    private DatabaseReference mDatabaseCurrentUser;
    private FirebaseAuth mAuth;
    private Query mCurrentuserQuery;
    //TextToSpeech T1;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private boolean mProcessLike = false;
    private TextToSpeech TTS;
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBlogList = (RecyclerView) findViewById(R.id.blog_list);
        mBlogList.setHasFixedSize(true);
        mAuth = FirebaseAuth.getInstance();
        mDataBase = FirebaseDatabase.getInstance().getReference().child("blog");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likings");
        mDatabaseUsers.keepSynced(true);
        mDatabaseLike.keepSynced(true);
        mDataBase.keepSynced(true);
        mBlogList.setLayoutManager(new LinearLayoutManager(this));
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
        //Toast.makeText(getApplicationContext(),"FROM ONCREATE", Toast.LENGTH_SHORT).show();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // Toast.makeText(getApplicationContext(),"came first", Toast.LENGTH_SHORT).show();
                if (mAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    //     Toast.makeText(getApplicationContext(),"going to loginactivity", Toast.LENGTH_SHORT).show();
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };
        String CurrentUser=mAuth.getCurrentUser().getUid();
        mDatabaseCurrentUser =FirebaseDatabase.getInstance().getReference().child("blog");
        mCurrentuserQuery=mDatabaseCurrentUser.orderByChild("uid").equalTo(CurrentUser);

    }

    @Override
    protected void onStart() {
        super.onStart();
        checkUserExist();
        // Toast.makeText(getApplicationContext(),"came second", Toast.LENGTH_SHORT).show();
        mAuth.addAuthStateListener(mAuthListener);
        // Toast.makeText(getApplicationContext(),"AFTER MAUTHLISTENER", Toast.LENGTH_SHORT).show();
        try {
            FirebaseRecyclerAdapter<Blog, BlogViewHolder> FirebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(
                    Blog.class,
                    R.layout.blog_row,
                    BlogViewHolder.class,
                    mDataBase
            ) {
                @Override
                protected void populateViewHolder(final BlogViewHolder viewHolder, Blog model, int position) {
                    final String post_key = getRef(position).getKey();
                    boolean k = false;
                    viewHolder.setTitle(model.getTitle());
                    viewHolder.setDesc(model.getDesc());
                    viewHolder.setImage(getApplicationContext(), model.getImage());
                    viewHolder.setusername(model.getUsername());
                    viewHolder.setLikeBtn(post_key);
                   // viewHolder.speechBtn(post_key);
                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Toast.makeText(getApplicationContext(),post_key, Toast.LENGTH_SHORT).show();
                            Intent singleBlockIntent = new Intent(MainActivity.this, BlockSingleActivity.class);
                            singleBlockIntent.putExtra("blog id", post_key);
                            startActivity(singleBlockIntent);

                        }
                    });
                    viewHolder.setLikesNum(post_key);
                    viewHolder.Speech.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mDataBase.child(post_key).addValueEventListener(new ValueEventListener() {
                                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(TTS.isSpeaking())
                                        TTS.stop();
                                    String description = (String) dataSnapshot.child("desc").getValue();
                                    TTS.speak(description, 0, null, "1");
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    });
                    viewHolder.mLikeBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mProcessLike = true;

                            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (mProcessLike) {
                                        if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                                            mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).removeValue();
                                            mProcessLike = false;
                                        } else {
                                            mDatabaseLike.child(post_key).child(mAuth.getCurrentUser().getUid()).setValue("random value");
                                            mProcessLike = false;
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    });

                }

            };
            mBlogList.setAdapter(FirebaseRecyclerAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkUserExist() {
        if (mAuth.getCurrentUser() != null) {
            //  Toast.makeText(getApplicationContext(),"mAuth.getCurrentUser()!=null", Toast.LENGTH_SHORT).show();
            final String user_id = mAuth.getCurrentUser().getUid();
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild(user_id)) {
                        // Toast.makeText(getApplicationContext(),"coming inside checkuserexist", Toast.LENGTH_SHORT).show();
                        Intent setupIntent = new Intent(MainActivity.this, LoginActivity.class);
                        setupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder {
        View mView;
        ImageButton mLikeBtn;
        FirebaseAuth mAuth;
        TextView Number_like;
        ImageButton Speech;
        DatabaseReference mDataBase, mDatabaseUsers, mDatabaseLike;
        public BlogViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mDataBase = FirebaseDatabase.getInstance().getReference().child("blog");
            mAuth = FirebaseAuth.getInstance();
            mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
            mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likings");
            mDatabaseUsers.keepSynced(true);
            mDatabaseLike.keepSynced(true);
            mDataBase.keepSynced(true);
            mLikeBtn = (ImageButton) mView.findViewById(R.id.post_like);
            Number_like = (TextView) mView.findViewById(R.id.number_like);
            Speech = (ImageButton) mView.findViewById(R.id.post_speak);
               /* post_title=(TextView)mView.findViewById(R.id.post_title);
                post_title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.v("MainActivity","you clicked post_title");
                    }
                });*/
        }
        //void speechBtn(final String mPostKey){
        //}
        void setLikesNum(final String post_key) {

            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(post_key)) {
                        long likes = dataSnapshot.child(post_key).getChildrenCount();
                        Number_like.setText(String.valueOf(likes));

                    } else {
                        Number_like.setText("0");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        void setLikeBtn(final String post_key) {
            mDatabaseLike.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child(post_key).hasChild(mAuth.getCurrentUser().getUid())) {
                        mLikeBtn.setImageResource(R.mipmap.ic_thumb_up_black_24dp);
                    } else {
                        mLikeBtn.setImageResource(R.mipmap.ic_thumb_up_white_24dp);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        public void setTitle(String title) {
            TextView post_title = (TextView) mView.findViewById(R.id.post_title);
            post_title.setText(title);
        }

        public void setDesc(String desc) {
            TextView post_desc = (TextView) mView.findViewById(R.id.post_desc);
            post_desc.setText(desc);
        }

        public void setusername(String username) {
            TextView post_name = (TextView) mView.findViewById(R.id.post_name);
            post_name.setText(username);
        }

        public void setImage(final Context ctx, final String image) {
            try {
                final ImageView post_image = (ImageView) mView.findViewById(R.id.post_image);
                //Picasso.with(ctx).load(image).into(post_image);
                Picasso.with(ctx).load(image).networkPolicy(NetworkPolicy.OFFLINE).into(post_image, new Callback() {
                    @Override
                    public void onSuccess() {
                        //Picasso.with(ctx).load(image).into(post_image);
                    }

                    @Override
                    public void onError() {
                        Picasso.with(ctx).load(image).into(post_image);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        //Toast.makeText(getApplicationContext(),"entered onCreate Options", Toast.LENGTH_SHORT).show();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_plus) {   //Toast.makeText(getApplicationContext(),"entered", Toast.LENGTH_SHORT).show();
            //startActivity(new Intent(MainActivity.this,Post_activity.class));
            Intent intent = new Intent(MainActivity.this, post.class);
            startActivity(intent);
        }
        if (item.getItemId() == R.id.action_logout) {
            logout();
        }
        if (item.getItemId() == R.id.action_settings) {
            Intent setIntent = new Intent(MainActivity.this, setupActivity.class);
            startActivity(setIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void ButtonClick(View view) {
        Intent intent = new Intent(MainActivity.this, post.class);
        startActivity(intent);

    }

    public void logout() {
        mAuth.signOut();
    }
}

