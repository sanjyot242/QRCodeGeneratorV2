package com.testing.fbtest;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static com.tozny.crypto.android.AesCbcWithIntegrity.*;
import static com.tozny.crypto.android.AesCbcWithIntegrity.keyString;
import static com.tozny.crypto.android.AesCbcWithIntegrity.keys;

public class MainActivity extends AppCompatActivity {

//    public void encryptData(String plaintext,SecretKey originaltext) {
//        System.out.println("-------Encrypting data using AES algorithm-------");
//        try {
//            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
//            keyGenerator.init(128);
//            SecretKey secretKey = keyGenerator.generateKey();
//            byte[] plaintTextByteArray = plaintext.getBytes("UTF8");
//
//            Cipher cipher = Cipher.getInstance("AES");
//            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
//            byte[] cipherText = cipher.doFinal(plaintTextByteArray);
//
//            System.out.println("Original data: " + plaintext);
//            System.out.println("Encrypted data:");
//
////            for (int i = 0; i < cipherText.length; i++) {
////                System.out.print(cipherText[i] + " ");
////
////            }
//        }
//        catch(Exception ex){
//            ex.printStackTrace();
//        }
//    }

    private EditText input;
    private Button  generate;
    private ImageView QRimage;
    private FirebaseAuth auth;
    private TextView textView;
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
       // textView=findViewById(R.id.textView);
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
            String usrInput1=usrInput+"Check";
           //System.out.println(usrInput1);


            //Condition if user field is empty
            if(TextUtils.isEmpty(usrInput))
            {
                Toast.makeText(this, "Enter a String Before proceeding", Toast.LENGTH_SHORT).show();
            }
            else
            {
                SecretKeys key ;
                try {
                    //Generating Private Key Through Library
                    String EXAMPLE_PASSWORD = value[0];// Get password from user input
                    String salt = saltString(generateSalt());
                    // You can store the salt, it's not secret. Don't store the key. Derive from password every time
                    //Log.i(TAG, "Salt: " + salt);
                    key = generateKeyFromPassword(EXAMPLE_PASSWORD, salt);
                   // System.out.println("String : "+keys);
                    //Combining Private Key and Public Key as Mainkey

                    //Typecasting String value To SecretKeys

                    //Performing Encryption

                    CipherTextIvMac cipherTextIvMac = encrypt(usrInput1,key);
                    //store or send to server
                    String ciphertextString = cipherTextIvMac.toString().trim();

                    //Taking 10 characters from ciphertext to be document name
                    String upToNCharacters = ciphertextString.substring(0, Math.min(ciphertextString.length(), 10));
                    String key1=keyString(key);

                    //String Values in Firebase in Format documentname->ncharacters  and value->PrivateKey
                    databaseReference.child("keyName").child(upToNCharacters).setValue(key1).addOnSuccessListener(new OnSuccessListener<Void>() {
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
//                    SecretKeys dkey = keys(Mainkey);
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
}
