package com.example.kyshi.finding_lost_kids_application;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.Preference;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.kyshi.finding_lost_kid_application.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class Kid_Photo_Upload_Activity extends AppCompatActivity {

    /* 상수들 */
    private static final int PICK_FROM_CAMERA = 0;
    private static final int PICK_FROM_ALBUM = 1;
    private static final int CROP_FROM_IMAGE = 2;
    private static final int MY_CAMERA_REQUEST_CODE = 100;

    /* 액티비티 관련 변수 */
    private Context mContext = this;
    private Intent intenttofindinglocationactivity;  // 현재 사용 X
    private Intent turnoverintent;
    User_Home_Activity aActivity;

    /* 뷰 관련 변수 */
    private ImageView img;
    private EditText editText;
    private Editable kidName;
    private LinearLayout ll;

    /* Alert 관련 변수 */
    AlertDialog.Builder builder = null;
    private CharSequence[] items = {"사진 찍기", "취소"};

    /* 파일(사진) 관련 변수 */
    private File photoFile = null;
    private String url = null;
    private Bitmap photo;
    private Uri mImageCaptureUri;
    private String absolutePath;

    /* SharedPreference 관련 변수 */    // 앱 뷰 변동으로 필요 X
//    private SharedPreferences sp;
//    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.kid_photo_upload);

        // 넘길 Intent -> 새로운 구현으로 비활성화
        // intenttofindinglocationactivity = new Intent(mContext, Finding_Kid_Location_Activity.class);    // Finding_Kid_Location_Activity 로 넘어가기 위한 intent

        // User_Home_Activity 종료용
        aActivity = (User_Home_Activity) User_Home_Activity.AActivity;

        // Layout 배경 하얗게
        ll = findViewById(R.id.kid_photo_upload_linearlayout);
        ll.setBackgroundColor(Color.WHITE);

        /**
         * 카메라, 저장장치 기능에 대한 권한 요청
         */

        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_CAMERA_REQUEST_CODE);
        }
        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_CAMERA_REQUEST_CODE);
        }

        builder = new AlertDialog.Builder(this);    // 사진을 눌렀을 경우 사진 찍기, 앨범 선택 창이 뜨게 함


        /**
         * 뷰에 대한 연동 처리
         */

        ImageButton btn = (ImageButton)findViewById(R.id.upload_button);
        img = (ImageView)findViewById(R.id.kidView);
        editText = (EditText)findViewById(R.id.Kid_Name);
        kidName = editText.getText();

        /**
         * 이미지를 눌렀을 경우 동작
         */

        img.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                builder.setTitle("업로드할 이미지 선택").setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which){
                            case PICK_FROM_CAMERA:
                                doTakePhotoAction();
                                break;

                            default:
                                dialog.dismiss();
                                break;
                        }
                    }
                }).show();

            }
        });


        /**
         * 업로드 버튼을 눌렀을 경우 동작
         */

        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                try {
                    /* 이미지 업로드 */
                    if(kidName.length() != 0) {

                        //Toast.makeText(getApplicationContext(), "아이의 이름: " + kidName, Toast.LENGTH_LONG).show();

                        turnoverintent = new Intent();
                        turnoverintent.putExtra("Name", kidName);
                        if(photo != null)
                            turnoverintent.putExtra("Photo", photo);
                        setResult(1, turnoverintent);
                        //startActivity(intenttofindinglocationactivity);
                        finish();
                       aActivity.finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "아이의 이름을 적어주세요.", Toast.LENGTH_LONG).show();
                    }
                } catch(Exception e){
                    Toast.makeText(getApplicationContext(), "네트워크 상태를 확인해주세요.", Toast.LENGTH_LONG).show();
                }

            }
        });

    }




    /**
     * 카메라에서 사진 촬영
     */

    public void doTakePhotoAction(){
        Intent intent = new Intent((MediaStore.ACTION_IMAGE_CAPTURE));

        url = "Finding_Kid_Location/my_kid_" + String.valueOf(System.currentTimeMillis()) + ".jpg";

        photoFile = new File(Environment.getExternalStorageDirectory(), url);


        if(Build.VERSION.SDK_INT > 23){
            mImageCaptureUri = FileProvider.getUriForFile(this, "com.example.kyshi.finding_lost_kids_application.Kid_Photo_Upload_Activity.fileprovider", photoFile);
        } else {
            mImageCaptureUri = Uri.fromFile((new File(Environment.getExternalStorageDirectory(), url)));
        }


//        File photoFile = new File(Environment.getExternalStorageDirectory(), url);



        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + mImageCaptureUri)));

        startActivityForResult(intent, PICK_FROM_CAMERA);
    }

    /**
     * 앨범에서 이미지 가져오기
     */

    public void doTakeAlbumAction(){

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);

    }

    /**
     * 사진 업로드하기
     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);

         if(resultCode != RESULT_OK) {
            return;
         }

        switch (requestCode)
        {
            case PICK_FROM_ALBUM:
            {
                mImageCaptureUri = data.getData();
                Log.d("Finding_Kid_Location", mImageCaptureUri.getPath().toString());
            }

            case PICK_FROM_CAMERA:
            {

                this.grantUriPermission("com.android.camera", mImageCaptureUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Intent intent = new Intent("com.android.camera.action.CROP");

                try {
                    intent.setDataAndType(mImageCaptureUri, "image/*");
                } catch (Exception e){
                    e.printStackTrace();
                }

                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                intent.putExtra("outputX", 200);
                intent.putExtra("outputY", 200);
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                
                startActivityForResult(intent, CROP_FROM_IMAGE);
                break;
            }

            case CROP_FROM_IMAGE:
            {
                if(resultCode != RESULT_OK){
                    return;
                }

                Bundle extras = null;
                try {
                    extras = data.getExtras();
                } catch (Exception e){
                    e.printStackTrace();
                }
                
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Finding_Kid_Location/Crop_my_kid_" + System.currentTimeMillis() + ".jpg";
                photo = null;

                if(extras != null)
                {

                    photo = extras.getParcelable("data");
                    img.setImageBitmap(photo);

                    storeCropImage(photo, filePath);
                    absolutePath = filePath;
                    break;
                } else {
                    photo = BitmapFactory.decodeFile(mImageCaptureUri.getPath());
                    img.setImageBitmap(photo);
                    storeCropImage(photo, url);
                    break;
                }
            }
        }
    }


    /**
     * Crop 한 이미지 저장
     */

    private void storeCropImage(Bitmap bitmap, String filePath){
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Finding_Kid_Location";
        File directory_FKL = new File(dirPath);
        if(!directory_FKL.exists())
            directory_FKL.mkdir();

        File copyFile = new File(filePath);
        BufferedOutputStream out = null;

        try{
            copyFile.createNewFile();
            out = new BufferedOutputStream((new FileOutputStream(copyFile)));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);

            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(copyFile)));

            out.flush();
            out.close();
        } catch(Exception e){
            e.printStackTrace();
        }

    }

}
