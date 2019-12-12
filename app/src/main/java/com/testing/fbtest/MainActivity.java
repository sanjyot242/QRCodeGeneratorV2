package com.testing.fbtest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
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

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import javax.crypto.SecretKey;

import static com.tozny.crypto.android.AesCbcWithIntegrity.*;
import static com.tozny.crypto.android.AesCbcWithIntegrity.keyString;
import static com.tozny.crypto.android.AesCbcWithIntegrity.keys;

public class MainActivity extends AppCompatActivity {

    private EditText input;
    private Button  generate;
    private ImageView QRimage;
    private FirebaseAuth auth;
    private TextView textView;
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
        textView=findViewById(R.id.textView);
        final String[] value = {""};

        databaseReference.child("publicKey").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                value[0] = dataSnapshot.getValue(String.class);
                System.out.println(value[0]);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
            }

        });



        generate.setOnClickListener(v ->{

            String usrInput=input.getText().toString().trim();
            String usrInput1=usrInput+"Check";
            System.out.println(usrInput1);

            //Getting Public key from database

            if(TextUtils.isEmpty(usrInput))
            {
                Toast.makeText(this, "Enter a String Before proceeding", Toast.LENGTH_SHORT).show();
            }
            else
            {
                SecretKeys keys ;
                try {
                    keys = generateKey();
                    String Mainkey=keys+ value[0];
                    System.out.println("String : "+Mainkey);

                    SecretKeys keys1 = keys(Mainkey);
                    System.out.println("MainKey : "+keys1);

                    CipherTextIvMac cipherTextIvMac = encrypt(usrInput1,keys1);
                    //store or send to server
                    String ciphertextString = cipherTextIvMac.toString().trim();
                    String upToNCharacters = ciphertextString.substring(0, Math.min(ciphertextString.length(), 10));
                    String key=keyString(keys);
                    databaseReference.child("keyName").child(upToNCharacters).setValue(Mainkey).addOnSuccessListener(new OnSuccessListener<Void>() {
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

                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.encodeBitmap(ciphertextString, BarcodeFormat.QR_CODE, 350, 350);
                    QRimage.setImageBitmap(bitmap);
                    System.out.println("Cipher Text:"+ciphertextString);
                    System.out.println("First n Characters > Document name : "+upToNCharacters);
//                    System.out.println("Keys "+keys);
//                    System.out.println("Key "+key);

                    //Decryption
                    databaseReference.child("publicKey").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String value = dataSnapshot.getValue(String.class);
                            System.out.println(value);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            // ...
                        }
                    });
                    SecretKeys dkey = keys(Mainkey);
                    CipherTextIvMac cipherTextIvMac1 = new CipherTextIvMac(ciphertextString);
                    String plainText = decryptString(cipherTextIvMac1, dkey);
                    textView.setText(plainText);
                    System.out.println("Decrypted Text: "+plainText);

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
                }
            }




        });

    }
}
