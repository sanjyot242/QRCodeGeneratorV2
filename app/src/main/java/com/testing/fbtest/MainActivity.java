package com.testing.fbtest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.tozny.crypto.android.AesCbcWithIntegrity.CipherTextIvMac;
import static com.tozny.crypto.android.AesCbcWithIntegrity.SecretKeys;
import static com.tozny.crypto.android.AesCbcWithIntegrity.encrypt;
import static com.tozny.crypto.android.AesCbcWithIntegrity.generateKey;
import static com.tozny.crypto.android.AesCbcWithIntegrity.generateKeyFromPassword;
import static com.tozny.crypto.android.AesCbcWithIntegrity.keyString;

public class MainActivity extends AppCompatActivity {

    private EditText input;
    private Button generate;
    private ImageView QRimage;
    private FirebaseAuth auth;
    private Button Share;
    private DatabaseReference databaseReference;
    private Button Save;
    private static final String TAG = "MyActivity";
    //Initializing String to Take Public Key from Firebase
    final String[] value = {""};
    String usrInput1;
    String upToNCharacters;
    String Sprivatekey2;
    String ciphertextString;
    Boolean val=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        input = findViewById(R.id.Input);
        generate = findViewById(R.id.Generate);
        QRimage = findViewById(R.id.qrimage);
        Share = findViewById(R.id.share);
        Save = findViewById(R.id.Save);



        //Taking public key from Firebase
        databaseReference.child("publicKey").addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //String
                value[0] = dataSnapshot.getValue(String.class);
//                byte[] decodedKey = Base64.getDecoder().decode(value[0]);
//                // rebuild key using SecretKeySpec
//                SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
                System.out.println(value[0]);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
            }

        });

        //OnClick Listner
        generate.setOnClickListener(v -> {

            authBottomSheet bottomSheet = new authBottomSheet();
            bottomSheet.show(getSupportFragmentManager(),"bottsheet");
            //Taking UserInput
            String usrInput = input.getText().toString().trim();
            //Appending String to user input
            String usrInput1 = usrInput + "Check";
            //System.out.println(usrInput1);


            //Condition if user field is empty
            if (TextUtils.isEmpty(usrInput)) {
                Toast.makeText(this, "Enter a String Before proceeding", Toast.LENGTH_SHORT).show();
            } else {
                //SecretKeys key, privatekey2;
                try {
                   // Generating Private Key Through Library
//                    privatekey2 = generateKey();
//                    //converting private key into String
//                    String Sprivatekey2 = keyString(privatekey2);
//                    // Creating a String(private key generated randomly + public key from database) to encrypt using salt
//                    String EXAMPLE_PASSWORD = Sprivatekey2 + value[0];
//                    //Salt string
//                    String salt = "RightWatchmanRightWatchman";
//
//                    System.out.println("SAlt: " + salt);
//                    // You can store the salt, it's not secret. Don't store the key. Derive from password every time
//                    //Log.i(TAG, "Salt: " + salt);
//
//                    //Generating Key
//                    key = generateKeyFromPassword(EXAMPLE_PASSWORD, salt);
//
//
//                    //Performing Encryption
//                    CipherTextIvMac cipherTextIvMac = encrypt(usrInput1, key);
//                    //store or send to server
//                    String ciphertextString = cipherTextIvMac.toString().trim();
//                    //Taking 10 characters from ciphertext to be document name
//                    String upToNCharacters = ciphertextString.substring(0, Math.min(ciphertextString.length(), 10));
//
//                    //String Values in Firebase in Format documentname->ncharacters  and value->PrivateKey
//                    databaseReference.child("keyName").child(upToNCharacters).setValue(Sprivatekey2).addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Toast.makeText(MainActivity.this, "Finally!!!!", Toast.LENGTH_SHORT).show();
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            System.out.println("Error 123 : " + e.getMessage());
//                        }
//                    }).addOnCanceledListener(new OnCanceledListener() {
//                        @Override
//                        public void onCanceled() {
//                            System.out.println("Error 456");
//                        }
//                    });
//
//
//                    //Creating QRcode
//                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
//                    Bitmap bitmap = barcodeEncoder.encodeBitmap(ciphertextString, BarcodeFormat.QR_CODE, 350, 350);
//                    QRimage.setImageBitmap(bitmap);
                    Encrypt(value,usrInput1);
                    System.out.println("n characters:"+upToNCharacters);
                    check(upToNCharacters);



                    //System.out.println("Cipher Text:"+ciphertextString);
                    //System.out.println("First n Characters > Document name : "+upToNCharacters);
//                    System.out.println("Keys "+keys);
//                    System.out.println("Key "+key);


                    //Decryption
//                    databaseReference.child("publicKey").addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            String value = dataSnapshot.getValue(String.class);
//                            System.out.println(value);
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//                            // ...
//                        }
//                    });
                    // SecretKeys dkey = keys(Sprivatekey2);
//                    String EXAMPLE_PASSWORD1 = Sprivatekey2+value[0] ;// Get password from user input
//                    // You can store the salt, it's not secret. Don't store the key. Derive from password every time
//                    //Log.i(TAG, "Salt: " + salt);
//                    dkey = generateKeyFromPassword(EXAMPLE_PASSWORD1, salt);
//                    System.out.println(dkey);
//                    CipherTextIvMac cipherTextIvMac1 = new CipherTextIvMac(ciphertextString);
////                    String plainText = decryptString(cipherTextIvMac1, dkey);
//                    //textView.setText(plainText);
//                    System.out.println("Decrypted Text: "+plainText);

//                    try {
//                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
//                        Bitmap bitmap = barcodeEncoder.encodeBitmap(ciphertextString, BarcodeFormat.QR_CODE, 350, 350);
//                        QRimage.setImageBitmap(bitmap);
//                    } catch(Exception e) {
//                        e.printStackTrace();
//                    }
                    //System.out.println(keys);
                    //System.out.println(ciphertextString);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });

        Share.setOnClickListener(v1 -> {
            OnClickShare(QRimage);
        });

        Save.setOnClickListener(v1 -> {
            OnClickSave(QRimage);
        });

    }

    //Onclick method
    private void OnClickShare(ImageView view) {
        //taking Bitmap from image view
        Bitmap bitmap = ((BitmapDrawable) view.getDrawable()).getBitmap();
        try {
            //getting location and saving image as qrcode.png
            File file = new File(this.getExternalCacheDir(), "qrcode.png");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true, false);
            //starting send intent
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setType("image/png");
            //to avoid "exposed beyond app through ClipData.Item.getUri"
            Uri apkURI = FileProvider.getUriForFile(
                    this,
                    this.getApplicationContext()
                            .getPackageName() + ".provider", file);
            intent.setDataAndType(apkURI, "image/png");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share image via"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void OnClickSave(ImageView view) {
        Bitmap bitmap = ((BitmapDrawable) view.getDrawable()).getBitmap();
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();

        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    /**
     * Create a File for saving an image or video
     */
    private File getOutputMediaFile() {
    // To be safe, you should check that the SDCard is mounted
    // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new
                File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");

// This location works best if you want the created images to be shared
// between applications and persist after your app has been uninstalled.

// Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {

            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
// Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName = "MI_" + timeStamp + ".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    private String Encrypt( String value[], String usrInput1) {
        SecretKeys privatekey2 = null;
        try {
            SecretKeys key;
            privatekey2 = generateKey();
            //converting private key into String
            Sprivatekey2 = keyString(privatekey2);
            // Creating a String(private key generated randomly + public key from database) to encrypt using salt
            String EXAMPLE_PASSWORD = Sprivatekey2 + value[0];
            //Salt string
            String salt = "RightWatchmanRightWatchman";

            System.out.println("SAlt: " + salt);
            // You can store the salt, it's not secret. Don't store the key. Derive from password every time
            //Log.i(TAG, "Salt: " + salt);

            //Generating Key
            key = generateKeyFromPassword(EXAMPLE_PASSWORD, salt);


            //Performing Encryption
            CipherTextIvMac cipherTextIvMac = encrypt(usrInput1, key);
            //store or send to server
             ciphertextString = cipherTextIvMac.toString().trim();
            //Taking 10 characters from ciphertext to be document name
             upToNCharacters = ciphertextString.substring(0, Math.min(ciphertextString.length(), 10));

 // Simulating Run condition in case of contradicing document name
//             if(val){
//                upToNCharacters = "yOR5bajdBW";
//            }else{
//                upToNCharacters = ciphertextString.substring(0, Math.min(ciphertextString.length(), 10));
//            }
//
//            check(upToNCharacters);

        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
       return upToNCharacters;
    }
    //Checking in database function
    private void check(String upToNCharacters){

        databaseReference.child("keyName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(upToNCharacters).exists()) {
                    System.out.println(upToNCharacters);
                    System.out.println("I am getting executed because of same 10::");
                    val =false;
                    Encrypt(value,usrInput1);
                }else{
                    Enterintodb();
                    try {
                        generateqr();
                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void Enterintodb(){
        databaseReference.child("keyName").child(upToNCharacters).setValue(Sprivatekey2).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(MainActivity.this, "Finally!!!!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println("Error 123 : " + e.getMessage());
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                System.out.println("Error 456");
            }
        });
    }

    private void generateqr() throws WriterException {
        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
        Bitmap bitmap = barcodeEncoder.encodeBitmap(ciphertextString, BarcodeFormat.QR_CODE, 350, 350);
        QRimage.setImageBitmap(bitmap);
    }
}


