package com.felipesantos.barcodeandqrscannerapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int STORAGE_REQUEST_CODE = 101;
    private static final String TAG = "MAIN_TAG";

    private MaterialButton cameraBtn;
    private MaterialButton galleryBtn;
    private ImageView imageIv;
    private MaterialButton scanBtn;
    private TextView resultTv;

    // arrays of permissions required to pick image from CAMERA/GALLERY
    private String[] cameraPermissions;
    private String[] storagePermissions;

    // URI of the image that will take from camera/gallery
    private Uri imageUri = null;

    private BarcodeScannerOptions barcodeScannerOptions;
    private BarcodeScanner barcodeScanner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraBtn = findViewById(R.id.cameraBtn);
        galleryBtn = findViewById(R.id.galleryBtn);
        imageIv = findViewById(R.id.imageIv);
        scanBtn = findViewById(R.id.scanBtn);
        resultTv = findViewById(R.id.resultTv);

        // init the arrays of permissions required to pick image from Camera/Gallery
        cameraPermissions = new String[] {
                Manifest.permission.CAMERA, // image from camera
                Manifest.permission.WRITE_EXTERNAL_STORAGE // writ
        };
        storagePermissions = new String[] {
                Manifest.permission.WRITE_EXTERNAL_STORAGE // image from gallery > WRITE_EXTERNAL_STORAGE permission only
        };

        /** init/setup BarcodeScannerOptions, put coomma separated types in .setBarcodeFormats
         * following formats are supported: Code 128, Code 39, Code 93, Codabar, EAN-13,
         * EAN-8, ITF, UPC-A, UPC-E, QR Code, PDF417, Aztec, Data Matrix
         */
        barcodeScannerOptions = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .build();

        barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions);

        // handle cameraBtn click, check permissions related to Camera (ie. WRITE STORAAGE & CAMERA), and take image from Camera
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkCameraPermission()) {
                    // permission required for camera already granted, launch camera intent
                    pickImageCamera();
                } else {
                    // permission required for camera was not already granted, request permissions
                    requestCameraPermission();
                }
            }
        });


        // handle galleryBtn click, check permissions related to Gallery, (i.e WRITE STORAGE) and take image from Camera
        galleryBtn.setOnClickListener(view -> {
            if (checkStoragePermission()) {
                // permission required for gallery already granted, launch gallery intent
                pickImageGallery();
            } else {
                // permission required for gallery was not already granted, request permissions
                requestStoragePermission();
            }
        });

        // handle scanBtn, scan the Barcode/QR Code from image picked from Camera/Gallery
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (imageUri == null) {
                    Toast.makeText(MainActivity.this, "Pick image first...", Toast.LENGTH_SHORT).show();
                } else {
                    detectResultFromImage();
                }
            }
        });

    }

    private void detectResultFromImage() {
        try {
            // prepare image from image uri
            InputImage inputImage = InputImage.fromFilePath(this, imageUri);
            // start scanning the barcode/ QR code data from image
            Task<List<Barcode>> barcodeResult = barcodeScanner.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            // task completed successfully, we can get detailed info now
                            extractBarcodeQrCodeInfo(barcodes);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // task failed with an exception, we can't get any detail
                            Toast.makeText(MainActivity.this, "Failed scanning due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch(Exception e) {
            // failed with an exception either due to preparing InputImage or issue in BarcodeScanner init
            Toast.makeText(this, "Failed due to " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void extractBarcodeQrCodeInfo(List<Barcode> barcodes) {
        // get information from barcodes
        for (Barcode barcode: barcodes) {
            Rect bounds = barcode.getBoundingBox();
            Point[] corners = barcode.getCornerPoints();

            // raw info scanned from the Barcode/QR Code
            String rawValue = barcode.getRawValue();
            Log.d(TAG, "extractBarcodeQrCodeInfo: rawValue: " + rawValue);

            /**
             * the following types are supported:
             * Barcode.TYPE_UNKNOWN, Barcode.TYPE_CONTACT_INFO, Barcode.TYPE_EMAIL, Barcode.TYPE_ISBN, Barcode.TYPE_PHONE,
             * Barcode.TYPE_PRODUCT, Barcode.TYPE_SMS, Barcode.TYPE_TEXT, Barcode.TYPE_URL, Barcode.TYPE_WIFI,
             * Barcode.TYPE_GEO, Barcode.TYPE_CALENDAR_EVENT, Barcode.TYPE_DRIVER_LICENSE
             */
            int valueType = barcode.getValueType();

            switch (valueType) {
                case Barcode.TYPE_WIFI: {
                    Barcode.WiFi typeWifi = barcode.getWifi();
                    String ssid = "" + typeWifi.getSsid();
                    String password = "" + typeWifi.getPassword();
                    String encryptionType = "" + typeWifi.getEncryptionType();

                    Log.d(TAG, "extractBarcodeQrCodeInfo: ssid: " + ssid);
                    Log.d(TAG, "extractBarcodeQrCodeInfo: password: " + password);
                    Log.d(TAG, "extractBarcodeQrCodeInfo: encryptionType: " + encryptionType);

                    // set to textView
                    resultTv.setText("TYPE: TYPE_WIFI \nssid: " + ssid + "\npassword: " + password + "\nencryption: " + encryptionType + "\nraw value: " + rawValue);
                }
                break;
                case Barcode.TYPE_URL: {
                    Barcode.UrlBookmark typeUrl = barcode.getUrl();
                    String title = "" + typeUrl.getTitle();
                    String url = "" + typeUrl.getUrl();

                    Log.d(TAG, "extractBarcodeQrCodeInfo: TYPE_URL: ");
                    Log.d(TAG, "extractBarcodeQrCodeInfo: title: " + title);
                    Log.d(TAG, "extractBarcodeQrCodeInfo: url: " + url);

                    // set to textView
                    resultTv.setText("TYPE: TYPE_URL \ntitle: " + title + "\nurl:" + url + "\nraw value: " + rawValue);
                }
                break;
                case Barcode.TYPE_EMAIL: {
                    Barcode.Email typeEmail = barcode.getEmail();
                    String address = "" + typeEmail.getAddress();
                    String body = "" + typeEmail.getBody();
                    String subject = "" + typeEmail.getSubject();

                    Log.d(TAG, "extractBarcodeQrCodeInfo: TYPE_EMAIL: ");
                    Log.d(TAG, "extractBarcodeQrCodeInfo: title: " + address);
                    Log.d(TAG, "extractBarcodeQrCodeInfo: url: " + body);
                    Log.d(TAG, "extractBarcodeQrCodeInfo: url: " + subject);

                    // set to textView
                    resultTv.setText("TYPE: TYPE_EMAIL \naddress: " + address + "\nbody:" + body + "\nsubject" + subject + "\nraw value: " + rawValue);
                }
                break;
                case Barcode.TYPE_CONTACT_INFO: {
                    Barcode.ContactInfo typeContactInfo = barcode.getContactInfo();
                    String title = "" + typeContactInfo.getTitle();
                    String organization = "" + typeContactInfo.getOrganization();
                    String name = "" + typeContactInfo.getName().getFirst() + " " + typeContactInfo.getName().getLast();
                    String phones = "" + typeContactInfo.getPhones().get(0).getNumber();

                    Log.d(TAG, "extractBarcodeQrCodeInfo: TYPE_CONTACT_INFO: ");
                    Log.d(TAG, "extractBarcodeQrCodeInfo: title: " + title);
                    Log.d(TAG, "extractBarcodeQrCodeInfo: organization: " + organization);
                    Log.d(TAG, "extractBarcodeQrCodeInfo: name: " + name);
                    Log.d(TAG, "extractBarcodeQrCodeInfo: phones: " + phones);

                    // set to textView
                    resultTv.setText("TYPE: TYPE_CONTACT_INFO \ntitle: " + title + "\norganization:" + organization + "\nname" + name + "\nphones" + phones + "\nraw value: " + rawValue);
                }
                break;
                default: {
                    resultTv.setText("raw value: " + rawValue);
                }
            }
        }
    }

    private void pickImageGallery() {
        // intent to pick image from gallery, will show all resources from where we can pick the image
        Intent intent = new Intent(Intent.ACTION_PICK);
        // set type of file we want to pick i.e image
        intent.setType("image/*");

    }

    private final ActivityResultLauncher<Intent> galleryActivityResultLaucher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // here will receive the image, if picked from gallery
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // image picked, get the URI of the image picked
                        Intent data = result.getData();
                        imageUri = data.getData();
                        Log.d(TAG, "onActivityResult: imageURI: " + imageUri);
                        // set to imageView
                        imageIv.setImageURI(imageUri);
                    } else {
                        // cancelled
                        Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void pickImageCamera() {
        // get ready the image data to store in MediaStore
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "Sample Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION, "Sample Image Description");
        // imageUri
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        // Intent to launch camera
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLaucher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> cameraActivityResultLaucher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // here we will receive the image, if taken from camera
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // image is taken from camera
                        Intent data = result.getData();
                        // we already have the image in imageURI using function pickImageCamera()
                        Log.d(TAG, "onActivityResult: imageURI: " + imageUri);
                        // set to imageView
                        imageIv.setImageURI(imageUri);
                    } else {
                        // cancelled
                        Toast.makeText(MainActivity.this, "Cancelled", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    // storage permission is allowed, return true, denied is false
    private boolean checkStoragePermission() {
        boolean resultStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        return resultStorage;
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermissions, STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {
        boolean resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
        boolean resultStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        return resultCamera && resultStorage;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this, cameraPermissions, CAMERA_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode) {
            case CAMERA_REQUEST_CODE: {
                // check if some action from permission dialog performed or not Allow/Deny
                if (grantResults.length > 0) {
                    // check if camera, storage permissions grated, contains boolean results either true or false
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    // check if both permissions are granted or not
                    if (cameraAccepted && storageAccepted) {
                        // both permissions (Camera & Gallery) are granted, we cant launch camera intent
                        pickImageCamera();
                    } else {
                        // one or both permissions are denied, can't launch camera intent
                        Toast.makeText(this, "Camera & Storage permissions are required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE: {
                // check if some action from permission dialog performed or not Allow/Deny
                if (grantResults.length > 0) {
                    // check if storage permissions granted, contains boolean results either true or false
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    // check if storage permission is granted or not
                    if (storageAccepted) {
                        // storage permission granted, we can launch gallery intent
                        pickImageGallery();
                    } else {
                        // storage permission denied, can't launch gallery intent
                        Toast.makeText(this, "Storage permissions is required", Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }
    }
}