package com.GoDutch.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public List<String> napis;
    public List<String> liczby_nasze;
    Button captureImageBtn, detectTextBtn, tempBut;
    ImageView imageView;
    TextView textView, textView2;
    Bitmap productBitmap;
    Bitmap priceBitmap;
    Uri mainPic;
    Uri productPic = null;
    Uri pricePic = null;
    Uri temp = null;
    int helper = 0;
    static final int REQUEST_IMAGE_CAPTURE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        captureImageBtn = findViewById(R.id.capture_image);
        detectTextBtn = findViewById(R.id.detect_text_image);
        imageView = findViewById(R.id.image_view);
        textView = findViewById(R.id.text_display);
        textView2 = findViewById(R.id.text_display2);
        tempBut = findViewById(R.id.button);

        tempBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropProduct();
                cropPrice();
                //openOsobyDoPodzialu();
            }
        });

        captureImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //Toast.makeText(getBaseContext(), "Dokonaj wstępnego przycięcia zdjęcia!", Toast.LENGTH_LONG).show(); -- nie bangla
                dispatchTakePictureIntent();
                textView.setText("");
                textView2.setText("");
            }
        });

        detectTextBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                detectTextFromImage();
            }
        });
    }

    private void openOsobyDoPodzialu()
    {
        Intent intent = new Intent(this, OsobyDoPodzialu.class);
        startActivity(intent);
    }

    private void dispatchTakePictureIntent()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
        {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);
        }
    }

    private void cropProduct() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
        {
            CropImage.activity(mainPic)
                    .start(this);
        }
    }

    private void cropPrice() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
        {
            CropImage.activity(mainPic)
                    .start(this);
        }
    }

    @Override

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK)
            {
                if(helper == 0) //jesli main jest pusty
                {
                    temp = result.getUri();
                    mainPic = temp;
                    imageView.setImageURI(mainPic);
                    helper = 1;
                }
                else if (helper == 1) // jesli nie ma wycietych produktow
                {
                    temp = result.getUri();
                    productPic = temp;
                    try {
                        productBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), productPic);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    helper = 2;
                }
                else if(helper == 2) //jezeli nie ma wycietych cen
                {
                    temp = result.getUri();
                    pricePic = temp;
                    helper = 0;
                    try {
                        priceBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), pricePic);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error  = result.getError();
            }
        }
    }

    private void detectTextFromImage()
    {
        FirebaseVisionImage firebaseVisionProduct = FirebaseVisionImage.fromBitmap(productBitmap);
        FirebaseVisionImage firebaseVisionPrice = FirebaseVisionImage.fromBitmap(priceBitmap);
        FirebaseVisionTextRecognizer firebaseVisionProductDetector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        FirebaseVisionTextRecognizer firebaseVisionPriceDetector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        Task<FirebaseVisionText> resultProduct =
            firebaseVisionProductDetector.processImage(firebaseVisionProduct)
                    .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                        @Override
                        public void onSuccess(FirebaseVisionText firebaseVisionText) {
                            // Task completed successfully
                            displayTextFromImage(firebaseVisionText);
                        }
                    })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Task failed with an exception
                                    Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                    Log.d("Error: ", e.getMessage());
                                }
                            });

        Task<FirebaseVisionText> resultPrice =
                firebaseVisionPriceDetector.processImage(firebaseVisionPrice)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText2) {
                                // Task completed successfully
                                displayTextFromImage2(firebaseVisionText2);

                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                        Log.d("Error: ", e.getMessage());
                                    }
                                });
    }


    //napis
    private void displayTextFromImage(FirebaseVisionText firebaseVisionText) {
        textView.setText(null);
        textView.setMovementMethod(new ScrollingMovementMethod());
        napis = new ArrayList<>();
        if (firebaseVisionText.getTextBlocks().size() == 0) {
            Toast.makeText(this, "No Text Found", Toast.LENGTH_LONG).show();
            return;
        }

        for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
			for (FirebaseVisionText.Line line: block.getLines()) {
			    if(!line.getText().toUpperCase().contains("RABAT") )
			    {
                    textView.append(line.getText() + "\n ");
                    System.out.print(line.getText());
                    napis.add(line.getText());

                }
            }
        }
    }


    //kwota
    private void displayTextFromImage2(FirebaseVisionText firebaseVisionText) {
        textView2.setText(null);
        textView2.setMovementMethod(new ScrollingMovementMethod());
        liczby_nasze = new ArrayList<>();
        if (firebaseVisionText.getTextBlocks().size() == 0) {
            Toast.makeText(this, "No Text Found", Toast.LENGTH_LONG).show();
            return;
        }

        String temp;
        String tmp;
        String tempo;
        String wynik;
        Boolean czy_przekroczono=false;
        Integer ilosc_kwot=0;
        double tmpI1,tmpI2,wynikIntow;
        List<String> temp_liczbowy = new ArrayList<>();


        for (FirebaseVisionText.TextBlock block : firebaseVisionText.getTextBlocks()) {
            for (FirebaseVisionText.Line line : block.getLines()) {
//                textView2.append( line.getText() + "\n");
                temp_liczbowy.add(line.getText());
                ilosc_kwot++;
            }
        }

        for(int i=0;i<ilosc_kwot-1;i++)
        {
            wynik=temp_liczbowy.get(i);
            temp=temp_liczbowy.get(i+1);
            tmp=temp;
            tmp=tmp.replaceAll("[^\\.0123456789,-]","");

            if(temp.contains("-"))
            {
                if (!tmp.equals(temp)) // mamy do czynienia z lidem
                {
                    wynik=wynik.replaceAll("[^\\.0123456789,-]", "");

                    tmpI1=Double.parseDouble( wynik.replace(",",".") ); //zamiana przecinka na kropke
                    tmpI2=Double.parseDouble( tmp.replace(",",".") );
                    wynikIntow=Math.round(tmpI1*100)+Math.round(tmpI2*100 ); //zaokrągalnie doubla
                    tempo=String.valueOf(wynikIntow/100);
                    tempo=tempo.replace(".",",");//zamiana kropki na przecienek

                    textView2.append( tempo + "\n");
                    liczby_nasze.add(tempo);
                    i++;

                }
                else // biedronka
                {
                    wynik=temp_liczbowy.get(i+2);
                    wynik = wynik.replaceAll("[^\\.0123456789,-]", "");
                    textView2.append(wynik+"\n");
                    liczby_nasze.add(wynik);
                    i=i+2;
                    if(i+1>=ilosc_kwot){
                        czy_przekroczono=true;
                    }
                }
            }
            else // jezeli nie ma - to wypsiuje normalnie
            {
                wynik = wynik.replaceAll("[^\\.0123456789,-]", ""); // wyrzucenie jakichkolwiek liter
//                wynik = wynik.substring(0,wynik.length()-1); // usunięcie ostatniego znaku z obliczenia
                textView2.append(wynik+"\n");
                liczby_nasze.add(wynik);

            }
        }
        if(!czy_przekroczono)
        {
            wynik=temp_liczbowy.get(ilosc_kwot-1);
            wynik = wynik.replaceAll("[^\\.0123456789,-]", "");
            textView2.append(wynik+"\n");
            liczby_nasze.add(wynik);
        }
    }

}

