package com.example.socialmediaapp.fragments;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialmediaapp.AddPostActivity;
import com.example.socialmediaapp.LoginActivity;
import com.example.socialmediaapp.MainActivity;
import com.example.socialmediaapp.R;
import com.example.socialmediaapp.SettingsActivity;
import com.example.socialmediaapp.adapters.AdapterPosts;
import com.example.socialmediaapp.models.ModelPost;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    ImageView avatartv,covertv;
    TextView nam,email,phone;
    RecyclerView postrecycle;
    StorageReference storageReference;
    String storagepath="Users_Profile_Cover_image/";
    FloatingActionButton fab;
    List<ModelPost> posts;
    Button button;
    AdapterPosts adapterPosts;
    String uid;
    ProgressDialog pd;
    private static final int CAMERA_REQUEST=100;
    private static final int STORAGE_REQUEST=200;
    private static final int IMAGEPICK_GALLERY_REQUEST=300;
    private static final int IMAGE_PICKCAMERA_REQUEST=400;
    String cameraPermission[];
    String storagePermission[];
    Uri imageuri;
    String profileOrCoverPhoto;
    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_profile, container, false);

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();
        firebaseDatabase=FirebaseDatabase.getInstance();
        storageReference= FirebaseStorage.getInstance().getReference();
        databaseReference=firebaseDatabase.getReference("Users");
        cameraPermission=new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        checkUserStatus();
        avatartv=view.findViewById(R.id.avatartv);
        covertv=view.findViewById(R.id.cavertv);
        nam=view.findViewById(R.id.nametv);
        email=view.findViewById(R.id.emailtv);
        fab=view.findViewById(R.id.fab);
        button = view.findViewById(R.id.pupload);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getActivity().getApplicationContext(),LoginActivity.class);
                getActivity().startActivity(intent);
                Toast.makeText(getActivity().getApplicationContext(), "Logout successfully", Toast.LENGTH_SHORT).show();
            }
        });
        postrecycle=view.findViewById(R.id.recyclerposts);
        posts=new ArrayList<>();
        pd=new ProgressDialog(getActivity());
        pd.setCanceledOnTouchOutside(false);
        phone=view.findViewById(R.id.phonetv);
        Query query=databaseReference.orderByChild("email").equalTo(firebaseUser.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    String name=""+dataSnapshot1.child("name").getValue();
                    String emaill=""+dataSnapshot1.child("email").getValue();
                    String phonee=""+dataSnapshot1.child("phone").getValue();
                    String image=""+dataSnapshot1.child("image").getValue();
                    String cover=""+dataSnapshot1.child("cover").getValue();
                    nam.setText(name);
                    email.setText(emaill);
                    phone.setText(phonee);
                    try {
                        Picasso.with(getActivity()).load(image).into(avatartv);
                    }catch (Exception e){

                    }
                    try {
                        Picasso.with(getActivity()).load(cover).into(covertv);
                    }catch (Exception e){
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        checkUserStatus();
        loadMyPosts();
        return view;
    }



    private void loadMyPosts() {
        LinearLayoutManager layoutManager=new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        postrecycle.setLayoutManager(layoutManager);

        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Posts");
        Query query=databaseReference.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                posts.clear();
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    ModelPost modelPost=dataSnapshot1.getValue(ModelPost.class);
                    posts.add(modelPost);
                    adapterPosts=new AdapterPosts(getActivity(),posts);
                    postrecycle.setAdapter(adapterPosts);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getActivity(),databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }
    private void searchMyPosts(final String search) {
        LinearLayoutManager layoutManager=new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        postrecycle.setLayoutManager(layoutManager);

        DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference("Posts");
        Query query=databaseReference.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                posts.clear();
                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                    ModelPost modelPost=dataSnapshot1.getValue(ModelPost.class);
                    if(modelPost.getTitle().toLowerCase().contains(search.toLowerCase())||
                            modelPost.getDescription().toLowerCase().contains(search.toLowerCase())) {
                        posts.add(modelPost);
                    }
                    adapterPosts=new AdapterPosts(getActivity(),posts);
                    postrecycle.setAdapter(adapterPosts);

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(getActivity(),databaseError.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private Boolean checkStoragePermission(){
        boolean result= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission(){
        requestPermissions(storagePermission,STORAGE_REQUEST);
    }
    private Boolean checkCameraPermission(){
        boolean result= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)
                ==(PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission(){
        requestPermissions(cameraPermission,CAMERA_REQUEST);
    }
    private void showEditProfileDialog() {
        String options[]={"Edit Profile Picture","Edit Name", "Edit Phone","Edit Cover Photo","Change Password"};
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which==0){
                    pd.setMessage("Updating Profile Picture");
                    profileOrCoverPhoto="image";
                    showImagePicDialog();
                }else if(which==1){
                    pd.setMessage("Updating Name");
                    showNamephoneupdate("name");
                }else if(which==2){
                    pd.setMessage("Updating Phone Number");
                    showNamephoneupdate("phone");
                }
                else if(which==3){
                    pd.setMessage("Updating Cover Pic");
                    profileOrCoverPhoto="cover";
                    showImagePicDialog();
                }
                else if (which==4){
                    pd.setMessage("Changing Password");
                    showPasswordChangeDailog();
                }
            }
        });
        builder.create().show();
    }

    private void showPasswordChangeDailog() {
        View view=LayoutInflater.from(getActivity()).inflate(R.layout.dialog_update_password,null);
        final EditText oldpass=view.findViewById(R.id.oldpasslog);
        final EditText newpass=view.findViewById(R.id.newpasslog);
        Button editpass=view.findViewById(R.id.updatepass);
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setView(view);
        final AlertDialog dialog=builder.create();
        dialog.show();
        editpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldp=oldpass.getText().toString().trim();
                String newp=newpass.getText().toString().trim();
                if(TextUtils.isEmpty(oldp)){
                    Toast.makeText(getActivity(),"Current Password cant be empty",Toast.LENGTH_LONG).show();
                    return;
                }
                if(TextUtils.isEmpty(newp)){
                    Toast.makeText(getActivity(),"New Password cant be empty",Toast.LENGTH_LONG).show();
                    return;
                }
                dialog.dismiss();
                updatePassword(oldp,newp);
            }
        });
    }

    private void updatePassword(String oldp, final String newp) {
        pd.show();
        final FirebaseUser user=firebaseAuth.getCurrentUser();
        AuthCredential authCredential= EmailAuthProvider.getCredential(user.getEmail(),oldp);
        user.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                       user.updatePassword(newp)
                               .addOnSuccessListener(new OnSuccessListener<Void>() {
                                   @Override
                                   public void onSuccess(Void aVoid) {
                                       pd.dismiss();
                                       Toast.makeText(getActivity(),"Changed Password",Toast.LENGTH_LONG).show();

                                   }
                               }).addOnFailureListener(new OnFailureListener() {
                           @Override
                           public void onFailure(@NonNull Exception e) {
                               pd.dismiss();
                               Toast.makeText(getActivity(),"Failed",Toast.LENGTH_LONG).show();
                           }
                       });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getActivity(),"Failed",Toast.LENGTH_LONG).show();
            }
        });

    }

    private void showNamephoneupdate(final String key) {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Update" +key);
        LinearLayout layout=new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(10,10,10,10);
        final EditText editText=new EditText(getActivity());
        editText.setHint("Enter"+key);
        layout.addView(editText);
        builder.setView(layout);
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String value=editText.getText().toString().trim();
                if(!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String ,Object> result=new HashMap<>();
                    result.put(key,value);
                    databaseReference.child(firebaseUser.getUid()).updateChildren(result).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            pd.dismiss();
                            Toast.makeText(getContext()," updated ",Toast.LENGTH_LONG).show();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getContext(),"Unable to update",Toast.LENGTH_LONG).show();
                        }
                    });
                    if(key.equals("name")){
                        final DatabaseReference databaser=FirebaseDatabase.getInstance().getReference("Posts");
                        Query query=databaser.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                                    String child=databaser.getKey();
                                    dataSnapshot1.getRef().child("uname").setValue(value);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        databaser.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                                    String child=dataSnapshot1.getKey();
                                    if(dataSnapshot.child(child).hasChild("Comments")){
                                        String child1=dataSnapshot.child(child).getKey();
                                        Query child2=FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                                                    String child=databaser.getKey();
                                                    dataSnapshot1.getRef().child("uname").setValue(value);
                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }
                else {
                    Toast.makeText(getContext(),"Unable to update",Toast.LENGTH_LONG).show();

                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pd.dismiss();
            }
        });
        builder.create().show();
    }

    private void showImagePicDialog() {
        String options[]={ "Camera","Gallery"};
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image From");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which==0){
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }
                    else {
                        pickFromCamera();
                    }
                }else if(which==1){
                    if(!checkStoragePermission()){
                        requestStoragePermission();
                    }
                    else {
                        pickFromGallery();
                    }

                }
            }
        });
        builder.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == Activity.RESULT_OK){
            if(requestCode==IMAGEPICK_GALLERY_REQUEST){
                imageuri=data.getData();
                uploadProfileCoverPhoto(imageuri);
            }
            if(requestCode==IMAGE_PICKCAMERA_REQUEST){
                uploadProfileCoverPhoto(imageuri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case CAMERA_REQUEST:{
                if(grantResults.length>0){
                    boolean camera_accepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageaccepted=grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(camera_accepted&&writeStorageaccepted){
                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(getActivity(),"Please Enable Camera and Storage Permissions",Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST:{
                if(grantResults.length>0){
                    boolean writeStorageaccepted=grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    if(writeStorageaccepted){
                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(getActivity(),"Please Enable Storage Permissions",Toast.LENGTH_LONG).show();
                    }
                }
            }
            break;
        }

    }
    private void pickFromCamera(){
        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Temp_pic");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        imageuri=getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        Intent camerIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camerIntent.putExtra(MediaStore.EXTRA_OUTPUT,imageuri);
        startActivityForResult(camerIntent,IMAGE_PICKCAMERA_REQUEST);
    }
    private void pickFromGallery(){
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, IMAGEPICK_GALLERY_REQUEST);
    }

    private void uploadProfileCoverPhoto(final Uri uri){
        pd.show();
        String filepathname=storagepath + "" + profileOrCoverPhoto + "_" +firebaseUser.getUid();
        StorageReference storageReference1=storageReference.child(filepathname);
        storageReference1.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                while (!uriTask.isSuccessful());
                final Uri downloadUri=uriTask.getResult();
                if(uriTask.isSuccessful()){
                    HashMap<String,Object> hashMap=new HashMap<>();
                    hashMap.put(profileOrCoverPhoto,downloadUri.toString());
                    databaseReference.child(firebaseUser.getUid()).updateChildren(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            pd.dismiss();
                            Toast.makeText(getContext(),"Updated",Toast.LENGTH_LONG).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(getContext(),"Error Updating ",Toast.LENGTH_LONG).show();
                        }
                    });
                    if(profileOrCoverPhoto.equals("image")) {
                        final DatabaseReference Reference = FirebaseDatabase.getInstance().getReference("Posts");
                        Query query = Reference.orderByChild("uid").equalTo(uid);
                        query.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                    String child =Reference.getKey();
                                    dataSnapshot1.getRef().child("udp").setValue(downloadUri.toString());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        Reference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                                    String child=dataSnapshot1.getKey();
                                    if(dataSnapshot.child(child).hasChild("Comments")){
                                        String child1=dataSnapshot.child(child).getKey();
                                        Query child2=FirebaseDatabase.getInstance().getReference("Posts").child(child1).child("Comments").orderByChild("uid").equalTo(uid);
                                        child2.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                for(DataSnapshot dataSnapshot1:dataSnapshot.getChildren()){
                                                    String child =Reference.getKey();
                                                    dataSnapshot1.getRef().child("udp").setValue(downloadUri.toString());
                                                }

                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });

                    }
                }
                else {
                    pd.dismiss();
                    Toast.makeText(getContext(),"Error",Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(getContext(),"Error",Toast.LENGTH_LONG).show();
            }
        });

    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu,menu);
        menu.findItem(R.id.craetegrp).setVisible(false);
        menu.findItem(R.id.addparticipants).setVisible(false);
        menu.findItem(R.id.grpinfo).setVisible(false);
        menu.findItem(R.id.add).setVisible(false);
        menu.findItem(R.id.search).setVisible(false);
        menu.findItem(R.id.logout).setVisible(false);
        menu.findItem(R.id.settings).setVisible(false);
        MenuItem item=menu.findItem(R.id.search);
        SearchView searchView=(SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!TextUtils.isEmpty(query)){
                    searchMyPosts(query);
                }
                else {
                    loadMyPosts();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!TextUtils.isEmpty(newText)){
                    searchMyPosts(newText);
                }
                else {
                    loadMyPosts();
                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId()==R.id.logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        else if(item.getItemId()==R.id.add){
            startActivity(new Intent(getActivity(), AddPostActivity.class));
        }
        else if(item.getItemId()==R.id.settings){
            startActivity(new Intent(getActivity(), SettingsActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
    private void checkUserStatus(){
        FirebaseUser user=firebaseAuth.getCurrentUser();
        if(user!=null){
            uid=user.getUid();
        }
        else {
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }

//    public void logout(){
//        Intent intent = new Intent(getActivity().getApplicationContext(),LoginActivity.class);
//        getActivity().startActivity(intent);
//    }

}
