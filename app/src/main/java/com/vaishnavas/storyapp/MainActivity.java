package com.vaishnavas.storyapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
  private static int final_result =1;
   CircularImageView profileImage;
    private Button choosebtn;
int i =0;
    ListView statuslist;
    private Uri videoUri;
    MediaController mediaController;
    private StorageReference mStorageRef;
    private DatabaseReference mDataBaseRef;
    private GoogleApiClient googleApiClient;
    private GoogleSignInOptions gso;
    final ArrayList<allvideos> AllVideos = new ArrayList<>();
    public static Map<String,String> downloadedcontent = new HashMap<>();
String userName;
String userId;
String userEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
      statuslist = (ListView)  findViewById(R.id.listview);
        profileImage = (CircularImageView) findViewById(R.id.profileImage);
        choosebtn = (Button) findViewById(R.id.choose_btn);
        mediaController = new MediaController(this);

        mStorageRef = FirebaseStorage.getInstance().getReference("videos");
        mDataBaseRef = FirebaseDatabase.getInstance().getReference("videos");
        choosebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                choosevideofromstorage();
            }
        });

        gso =  new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleApiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this)
                .addApi(Auth.GOOGLE_SIGN_IN_API,gso)
                .build();
/*
    for logout
    logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                if (status.isSuccess()){
                                    gotoMainActivity();
                                }else{
                                    Toast.makeText(getApplicationContext(),"Session not close", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });
 */

  // downloading and storing name:videolink in the AllVideos Arraylist
        final DatabaseReference database =FirebaseDatabase.getInstance().getReference();
        database.child("AllVideos").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                downloadedcontent.clear();
                for(DataSnapshot snapshot:datasnapshot.getChildren() ){

                        downloadedcontent.put(snapshot.getKey(), snapshot.getValue().toString());


                }
                Refresh();
                showinglist();  }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

  profileImage.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
          Toast.makeText(MainActivity.this,"starting your status",Toast.LENGTH_LONG).show();
          DatabaseReference database = FirebaseDatabase.getInstance().getReference();
          // getting my recent status link
          database.child("Users").child(userId).addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                  for(DataSnapshot snapshot:datasnapshot.getChildren() ){

                      if(snapshot.getKey().toString().equals("status")){
                          String videolink = Objects.requireNonNull(snapshot.getValue()).toString();
                          LayoutInflater inflater = getLayoutInflater();
                          final View statusview = inflater.inflate(R.layout.status_particular, null);
                          VideoView statusvideo = statusview.findViewById(R.id.Video_view);
                          TextView statususer = statusview.findViewById(R.id.username);
                          statususer.setText(userName);
                          MediaController mediacontroller = new MediaController(MainActivity.this);
                          // playing video
                          statusvideo.setMediaController(mediacontroller);
                          mediacontroller.setAnchorView(statusvideo);
                          Uri uri = Uri.parse(videolink);
                          statusvideo.setVideoURI(uri);
                          statusvideo.start();
                          AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).setView(statusview).create();
                          alertDialog.show();
                      }

                  }

              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {

              }
          });

      }
  });
    }

    public void showinglist() {
        statusAdapter Adapter = new statusAdapter(this, AllVideos);
        statuslist.setAdapter(Adapter);

     /*   statuslist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                LayoutInflater inflater = getLayoutInflater();
                final View statusview = inflater.inflate(R.layout.status_particular, null);
                VideoView statusvideo = statusview.findViewById(R.id.Video_view);
                TextView statususer = statusview.findViewById(R.id.username);
                allvideos k = AllVideos.get(position);
                MediaController mediacontroller = new MediaController(MainActivity.this);
                // playing video
                statusvideo.setMediaController(mediacontroller);
                mediacontroller.setAnchorView(statusvideo);
                Uri uri = Uri.parse(k.videolink);
                statusvideo.setVideoURI(uri);
                statusvideo.start();
                // setting username of uploader
                statususer.setText(k.name);
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).setView(statusview).create();
                alertDialog.show();
            }
        });
        */
    }

    private void Refresh() {
       AllVideos.clear();
        for (Map.Entry m : downloadedcontent.entrySet()) {
            allvideos instance = new allvideos();
            instance.name = m.getKey().toString();
            instance.videolink = m.getValue().toString();
            AllVideos.add(instance);

        }
    }

    // for login info
    @Override
    protected void onStart() {
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> opr= Auth.GoogleSignInApi.silentSignIn(googleApiClient);
        if(opr.isDone()){
            GoogleSignInResult result=opr.get();
            handleSignInResult(result);
        }else{
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    private void handleSignInResult(GoogleSignInResult result){
        if(result.isSuccess()){
            GoogleSignInAccount account=result.getSignInAccount();
            assert account != null;
            userName= account.getDisplayName();
            userEmail =  account.getEmail();
            userId = account.getId();
            profileImage.setImageResource(R.drawable.images);
          if(account.getPhotoUrl()!=null){
              try{
                  Glide.with(this).load(account.getPhotoUrl()).into(profileImage);
              }catch (NullPointerException e){

                  Toast.makeText(this,"image not found",Toast.LENGTH_LONG).show();
              }
          }


        }else{
            gotoLogin();
        }
    }
    private void gotoLogin(){
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
    }
    private void choosevideofromstorage(){
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video for story.."),final_result);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == final_result && resultCode == RESULT_OK && data != null && data.getData() != null) {
            videoUri = data.getData();
          //  videoView.setVideoURI(videoUri);
            LayoutInflater inflater = getLayoutInflater();
            final View statusview = inflater.inflate(R.layout.uploadinglayout, null);
            VideoView statusvideo = statusview.findViewById(R.id.Video_view);
           final ProgressBar progressBar = statusview.findViewById(R.id.progress_bar);
            Button uploadbotton = statusview.findViewById(R.id.upload_btn);
            TextView statususer = statusview.findViewById(R.id.username);
            MediaController mediacontroller = new MediaController(MainActivity.this);
            // playing video
            statusvideo.setMediaController(mediacontroller);
            mediacontroller.setAnchorView(statusvideo);
            statusvideo.setVideoURI(videoUri);
            statusvideo.start();
            // setting mine username
            statususer.setText(userName);
            uploadbotton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadvideotoserver(progressBar);
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).setView(statusview).create();
            alertDialog.show();
        }
    }
    // for getting extension of the uri
        private String getfileExt(Uri videoUri){
            ContentResolver contentResolver = getContentResolver();
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(videoUri));
        }


    private void uploadvideotoserver(final ProgressBar p){

      if(videoUri!=null){

    final StorageReference reference = mStorageRef.child(System.currentTimeMillis()+getfileExt(videoUri));
    reference.putFile(videoUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            p.setVisibility(View.INVISIBLE);
            Toast.makeText(getApplicationContext(),"Upload is Successful",Toast.LENGTH_LONG).show();
            reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Toast.makeText(MainActivity.this, uri.toString().trim(),Toast.LENGTH_LONG).show();
                    updatingserver(uri);
                 // database.child("AllVideos").child(userName).setValue(uri);
                  // database.child("Users").child(userId).child("status").setValue(uri);
                }
            });
        }
    }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
        }
    });
}
    }

    private void updatingserver(Uri uri) {
       Toast.makeText(MainActivity.this,"updatingserver",Toast.LENGTH_LONG).show();
        final DatabaseReference database =FirebaseDatabase.getInstance().getReference();
        database.child("AllVideos").child(userName+"2").setValue(uri.toString());
         database.child("Users").child(userId).child("status").setValue(uri.toString());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
    // showing story of others
    public static class allvideos{
        private String name;
        private String videolink;
   public void allvideos(){
       // notification update whenever someone upload a new story...
        }
    }
    // setting mainlistview
    public class statusAdapter extends BaseAdapter {
        ArrayList<allvideos> allvideos;
        // public ArrayList<AdaptersItem> listnewsDataAdpater ;
        Context mContext;

        public statusAdapter(Context mContext, ArrayList<allvideos> Data) {
            this.mContext = mContext;
            this.allvideos = Data;
//            Toast.makeText(mContext,allvideos.size(),Toast.LENGTH_LONG).show();
        }


        @Override
        public int getCount() {
            return allvideos.size();
        }

        @Override
        public Object getItem(int position) {
            return allvideos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_mainscreen_ticket, parent, false);
            }
            final VideoView videoL = (VideoView)convertView.findViewById(R.id.Video_viewL);
            TextView fromL = (TextView) convertView.findViewById(R.id.usernameL);
            Uri uriL = null;
           final VideoView videoR = (VideoView)convertView.findViewById(R.id.Video_viewR);
            TextView fromR = (TextView) convertView.findViewById(R.id.usernameR);
            Uri uriR = null;
            int proxyposition = position + 1; // for the sake of calculation we will use proxyposition
            if(getCount()%2==0 && getCount()!=0 && proxyposition<=(getCount()/2)){

                allvideos kL = allvideos.get((proxyposition*2)-2); // for left
              //   MediaController mediaL = new MediaController(mContext);

                // playing video left
                videoL.setMediaController(null);
            //    mediaL.setAnchorView(videoL);
                 uriL = Uri.parse(kL.videolink);
                videoL.setVideoURI(uriL);
                videoL.start();
                // setting username of uploader
                fromL.setText(kL.name);
                allvideos kR = allvideos.get((proxyposition*2)-1); // for right
              //  MediaController mediaR = new MediaController(mContext);
                // playing video right
                videoR.setMediaController(null);
             //   mediaR.setAnchorView(videoR);
                 uriR = Uri.parse(kR.videolink);
                videoR.setVideoURI(uriR);
                videoR.start();
                // setting username of uploader
                fromR.setText(kR.name);

                animation(convertView,proxyposition,mContext);
            }else if(getCount()%2!=0 && getCount()!=0 && proxyposition<=((getCount()+1)/2)){
                if(proxyposition<((getCount()+1)/2)){

                    allvideos kL = allvideos.get((proxyposition*2)-2); // for left
                    MediaController mediaL = new MediaController(mContext);

                    // playing video left
                    videoL.setMediaController(null);
                  //  mediaL.setAnchorView(videoL);
                     uriL = Uri.parse(kL.videolink);
                    videoL.setVideoURI(uriL);
                    videoL.start();
                    videoL.pause();
                    // setting username of uploader
                    fromL.setText(kL.name);
                    allvideos kR = allvideos.get((proxyposition*2)-1); // for right
                  //  MediaController mediaR = new MediaController(mContext);
                    // playing video right
                    videoR.setMediaController(null);
                  //  mediaR.setAnchorView(videoR);
                     uriR = Uri.parse(kR.videolink);
                    videoR.setVideoURI(uriR);
                    videoR.start();
                    videoR.pause();
                    // setting username of uploader
                    fromR.setText(kR.name);
                    animation(convertView,proxyposition,mContext);
                }else{
                    // yein last video ke liye
                    allvideos k = allvideos.get((proxyposition*2)-2);

                  //  MediaController mediacontroller = new MediaController(mContext);

                    // playing video
                    videoL.setMediaController(null);
                   // mediacontroller.setAnchorView(videoL);
                     uriL = Uri.parse(k.videolink);
                    videoL.setVideoURI(uriL);
                    videoL.start();
                    videoL.pause();
                    // setting username of uploader
                    fromL.setText(k.name);
                    animation(convertView,proxyposition,mContext);
                }
            }
            videoL.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {

                    mp.setVolume(0, 0);
                }
            });
            videoR.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer mp) {

                    mp.setVolume(0, 0);
                }
            });
            videoL.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    videoL.start();
                }
            });
            videoR.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    videoR.start();
                }
            });
            final String nameL = fromL.getText().toString();
            final Uri finalUriL = uriL;
            videoL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    inflatevideo(finalUriL,nameL);
                }
            });
            final String nameR = fromR.getText().toString();
            final Uri finalUriR = uriR;
            videoR.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    inflatevideo(finalUriR,nameR);
                }
            });
        /*    allvideos k = allvideos.get(position);

            MediaController mediacontroller = new MediaController(mContext);
            VideoView video = (VideoView)convertView.findViewById(R.id.Video_view);
            TextView from = (TextView) convertView.findViewById(R.id.username);
            // playing video
            video.setMediaController(mediacontroller);
            mediacontroller.setAnchorView(video);
            Uri uri = Uri.parse(k.videolink);
            video.setVideoURI(uri);
            video.start();
            video.pause();
            // setting username of uploader
            from.setText(k.name);
            //  (adding animation in listview
            // Animation animation = AnimationUtils.loadAnimation(mContext,R.anim.fade_in);
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_up);
            //   Animation animation = AnimationUtils.loadAnimation(mContext,R.anim.shake);
            //   Animation animation = AnimationUtils.loadAnimation(mContext,R.anim.slide_left);
            //  Animation animation = AnimationUtils.loadAnimation(mContext,R.anim.slide_up);
            convertView.startAnimation(animation);
            convertView.setTag(k.name); */
            return convertView;
       }

        private void inflatevideo(Uri finalUriL,String name) {
            LayoutInflater inflater = getLayoutInflater();
            final View statusview = inflater.inflate(R.layout.status_particular, null);
            VideoView statusvideo = statusview.findViewById(R.id.Video_view);
            TextView statususer = statusview.findViewById(R.id.username);
            MediaController mediacontroller = new MediaController(MainActivity.this);
            // playing video
            statusvideo.setMediaController(mediacontroller);
            mediacontroller.setAnchorView(statusvideo);
            statusvideo.setVideoURI(finalUriL);
            statusvideo.start();
            // setting username of uploader
            statususer.setText(name);
            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).setView(statusview).create();
            alertDialog.show();
        }

    }
    public static View animation(View convertView, int k,Context mContext){

        //  (adding animation in listview
        // Animation animation = AnimationUtils.loadAnimation(mContext,R.anim.fade_in);
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_up);
        //   Animation animation = AnimationUtils.loadAnimation(mContext,R.anim.shake);
        //   Animation animation = AnimationUtils.loadAnimation(mContext,R.anim.slide_left);
        //  Animation animation = AnimationUtils.loadAnimation(mContext,R.anim.slide_up);
        convertView.startAnimation(animation);
        convertView.setTag("tag"+k);
        return convertView;

    }
    }