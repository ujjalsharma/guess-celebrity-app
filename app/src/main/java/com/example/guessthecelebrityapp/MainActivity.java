package com.example.guessthecelebrityapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ImageView celebrityImage;
    Button button1;
    Button button2;
    Button button3;
    Button button4;

    ArrayList<String> celebrityNames = new ArrayList<String>();
    ArrayList<String> celebrityImageUrls = new ArrayList<String>();
    ArrayList<String> answers = new ArrayList<String>();
    int guessLengthURLs;
    int guessLengthNames;
    int indexCorrect;


    public void answerClick(View view){

        if (Integer.toString(indexCorrect).equals(view.getTag().toString())){
            Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
            nextGuess();
        } else {
            Toast.makeText(this, "Wrong Answer! Try Again!", Toast.LENGTH_SHORT).show();
        }

    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String result="";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }

                return result;




            } catch (Exception e) {
                e.printStackTrace();
                return "Failed";
            }

        }
    }

    public void downloadImage(String imageUrl) {
        ImageDownloader task = new ImageDownloader();

        Bitmap myImage;

        try {
            myImage = task.execute(imageUrl).get();
            celebrityImage.setImageBitmap(myImage);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public void nextGuess() {

        Random rand = new Random();
        int correctIndex = rand.nextInt(guessLengthNames);
        String imageUrl = celebrityImageUrls.get(correctIndex);
        String correctAnswer = celebrityNames.get(correctIndex);

        downloadImage(imageUrl);

        indexCorrect = rand.nextInt(4);

        for (int i=0; i<4; i++) {
            if (i==indexCorrect){
                answers.add(i, correctAnswer);
            } else {

                String wrongAnswer = celebrityNames.get(rand.nextInt(guessLengthNames));

                while (wrongAnswer.equals(correctAnswer)) {
                    wrongAnswer = celebrityNames.get(rand.nextInt(guessLengthNames));
                }
                answers.add(i, wrongAnswer);

            }

        }

        button1.setText(answers.get(0));
        button2.setText(answers.get(1));
        button3.setText(answers.get(2));
        button4.setText(answers.get(3));


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        celebrityImage = (ImageView) findViewById(R.id.imageView2);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);

        DownloadTask task = new DownloadTask();
        String result = null;
        try {
            result = task.execute("https://svenskainfluencers.nu").get();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Pattern p = Pattern.compile("<figure class=\"wp-block-image size-large\"><img src=\"(.*?)\"");
        Matcher m = p.matcher(result);

        while (m.find()) {
            celebrityImageUrls.add(m.group(1));
        }

        p = Pattern.compile("figcaption>Instagram: (.*?)</figcaption>");
        m = p.matcher(result);

        while (m.find()) {
            celebrityNames.add(m.group(1));
        }

        guessLengthURLs = celebrityImageUrls.size();
        guessLengthNames = celebrityNames.size();

        nextGuess();






    }

    public class ImageDownloader extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... urls) {

            try {

                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                InputStream in = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(in);
                return myBitmap;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

}
