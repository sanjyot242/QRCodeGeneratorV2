package com.testing.fbtest;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.tozny.crypto.android.AesCbcWithIntegrity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static com.tozny.crypto.android.AesCbcWithIntegrity.*;
import static com.tozny.crypto.android.AesCbcWithIntegrity.keyString;
import static com.tozny.crypto.android.AesCbcWithIntegrity.keys;

public class MainActivity extends AppCompatActivity {

    private EditText input;
    private Button  generate;
    private ImageView QRimage;
    private FirebaseAuth auth;
    private Button Share;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        input=findViewById(R.id.Input);
        generate=findViewById(R.id.Generate);
        QRimage=findViewById(R.id.qrimage);
        Share=findViewById(R.id.share);
        //Initializing String to Take Public Key from Firebase
        final String[] value = {""};



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
        generate.setOnClickListener(v ->{
            //Taking UserInput
            String usrInput=input.getText().toString().trim();
            //Appending String to user input
            String usrInput1=usrInput+"Check";
           //System.out.println(usrInput1);


            //Condition if user field is empty
            if(TextUtils.isEmpty(usrInput))
            {
                Toast.makeText(this, "Enter a String Before proceeding", Toast.LENGTH_SHORT).show();
            }
            else
            {
                SecretKeys key,privatekey2 ;
                try {
                    //Generating Private Key Through Library
                    privatekey2=generateKey();
                    //converting private key into String
                    String Sprivatekey2=keyString(privatekey2);
                    // Creating a String(private key generated randomly + public key from database) to encrypt using salt
                    String EXAMPLE_PASSWORD = Sprivatekey2+value[0] ;
                    //Salt string
                    String salt = "RightWatchmanRightWatchman";

                    System.out.println("SAlt: "+salt);
                    // You can store the salt, it's not secret. Don't store the key. Derive from password every time
                    //Log.i(TAG, "Salt: " + salt);

                    //Generating Key
                    key = generateKeyFromPassword(EXAMPLE_PASSWORD, salt);



                    //Performing Encryption
                    CipherTextIvMac cipherTextIvMac = encrypt(usrInput1,key);
                    //store or send to server
                    String ciphertextString = cipherTextIvMac.toString().trim();
                    //Taking 10 characters from ciphertext to be document name
                    String upToNCharacters = ciphertextString.substring(0, Math.min(ciphertextString.length(), 10));
                    //String key1=keyString(key);

                    //String Values in Firebase in Format documentname->ncharacters  and value->PrivateKey
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


                    //Creating QRcode
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.encodeBitmap(ciphertextString, BarcodeFormat.QR_CODE, 350, 350);
                    QRimage.setImageBitmap(bitmap);

                    Share.setOnClickListener(v1 -> {
                        OnClickShare(QRimage);
                    });

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
//                    String plainText = decryptString(cipherTextIvMac1, dkey);
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
                } catch (GeneralSecurityException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (WriterException e) {
                    e.printStackTrace();
                }catch(Exception e)
                {
                    e.printStackTrace();
                }
            }

        });



    }
//Onclick method
    private void OnClickShare(ImageView view) {
        //taking Bitmap from image view
        Bitmap bitmap = ((BitmapDrawable)view.getDrawable()).getBitmap();
        try {
            //getting location and saving image as qrcode.png
            File file = new File(this.getExternalCacheDir(),"qrcode.png");
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
}
