package com.thgmobi.catwalk.views;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.gigamole.infinitecycleviewpager.HorizontalInfiniteCycleViewPager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.thgmobi.catwalk.MainActivity;
import com.thgmobi.catwalk.R;
import com.thgmobi.catwalk.adapters.CarrouselAdapter;
import com.thgmobi.catwalk.helper.CheckPermissions;
import com.thgmobi.catwalk.models.Photo;
import com.thgmobi.catwalk.util.Common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class CameraFragment extends Fragment {

    private ConstraintLayout layoutFoto;
    private ImageView ivFoto;
    private Button btnSalvaFoto;
    private FloatingActionButton fabChamaCamera;
    private ProgressBar progressBar;
    private HorizontalInfiniteCycleViewPager cycleViewPager;

    private CheckPermissions checkPermissions = new CheckPermissions();
    private Photo photo;

    private List<Photo> listPhotos;
    private CarrouselAdapter carrouselAdapter;

    private String caminhoFoto;
    private String temp = MainActivity.temp;

    private DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
    private StorageReference storageReference = FirebaseStorage.getInstance().getReference();


    public CameraFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.fragment_camera, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initVars();
        initActions();
        checkPermissions.checkCameraPermission(getContext(), getActivity());
    }

//    @Override
//    public void onStart() {
//        super.onStart();
//        getPhotosInDb();
//        Toast.makeText(getContext(), "onStart", Toast.LENGTH_SHORT).show();
//
//    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        if (layoutFoto == null){
            initActions();
            initVars();
        }
        getPhotosInDb();
    }

    private void initVars() {
        layoutFoto = getActivity().findViewById(R.id.camera_layer_foto);
        ivFoto = getActivity().findViewById(R.id.camera_iv_foto);
        btnSalvaFoto = getActivity().findViewById(R.id.camera_btn_salvar);
        fabChamaCamera = getActivity().findViewById(R.id.camera_fab);
        progressBar = getActivity().findViewById(R.id.camera_progressbar);
        cycleViewPager = getActivity().findViewById(R.id.camera_icvp);

        photo = new Photo();

        listPhotos = new ArrayList<>();

        carrouselAdapter = new CarrouselAdapter(listPhotos, getContext());
        cycleViewPager.setAdapter(carrouselAdapter);


        layoutFoto.setVisibility(View.GONE);
    }

    private void initActions() {

        fabChamaCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermissions.checkCameraPermission(getContext(), getActivity())){
                    dispatchTakePictureIntent();
                }
            }
        });

        btnSalvaFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePhotoInFbStorage();
                progressBar.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == Common.CAMERA_PERMISSION_REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(getContext(), "Não será possivel iniciar a Camera", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        ivFoto.setVisibility(View.VISIBLE);

        File fotoFile = null;

        try{
            fotoFile = createImageFile();
        }catch (IOException ex){
            ex.printStackTrace();
        }

        if (fotoFile != null){
            String authorities = getActivity().getApplicationContext().getPackageName() + ".fileprovider";
            Uri imageUri = FileProvider.getUriForFile(getContext(), authorities, fotoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePictureIntent, Common.CAMERA_PERMISSION_REQUEST_CODE);
            layoutFoto.setVisibility(View.INVISIBLE);

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Common.CAMERA_PERMISSION_REQUEST_CODE && resultCode == RESULT_OK){

            layoutFoto.setVisibility(View.VISIBLE);
            setPic();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        caminhoFoto = image.getAbsolutePath();

        return image;
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = ivFoto.getWidth();
        int targetH = ivFoto.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(caminhoFoto, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(caminhoFoto, bmOptions);
        ivFoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivFoto.setImageBitmap(bitmap);
    }

    private void savePhotoInFbStorage(){

        ivFoto.setDrawingCacheEnabled(true);
        ivFoto.buildDrawingCache();
        Bitmap bitmap = ivFoto.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] data = baos.toByteArray();

        Uri file = Uri.fromFile(new File(caminhoFoto));

        //MetaData Photo
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType("image/jpg")
                .setCustomMetadata("Temperatura:",temp)
                .build();

        StorageReference storageRef =
                storageReference.child(firebaseUser.getUid() + "/" + file.getLastPathSegment());
        UploadTask uploadTask = storageRef.putBytes(data, metadata);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                photo.setUrlDownload(String.valueOf(taskSnapshot.getDownloadUrl()));
                photo.setUserId(firebaseUser.getUid());
                photo.setTemp(temp);
            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {


                photo.savePhotoInFirebaseDB(firebaseUser.getUid());

                layoutFoto.setVisibility(View.GONE);
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Upload, efetuado com sucesso" , Toast.LENGTH_SHORT).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Erro ao efetuar o Upload", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getPhotosInDb(){


        Query query;
        query = databaseReference.child(Common.CHILD_DB_PHOTO).child(firebaseUser.getUid()).orderByChild("userId").equalTo(firebaseUser.getUid());

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                for (DataSnapshot dados: dataSnapshot.getChildren()){

                    Photo photo1 = dados.getValue(Photo.class);

                    listPhotos.add(photo1);

                }

                carrouselAdapter.notifyDataSetChanged();

                progressBar.setVisibility(View.GONE);
                cycleViewPager.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }




















}
