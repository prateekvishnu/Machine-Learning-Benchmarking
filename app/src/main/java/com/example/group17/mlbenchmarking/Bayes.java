package com.example.group17.mlbenchmarking;

/**
 * Created by Vishnu Prateek  on 11/27/17.
 */

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

import static java.lang.Thread.sleep;


public class Bayes extends FirstActivity {
    //upload file path
    final String uploadFilePath = (Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/MLBenchmarking/");
    String upLoadServerUri = "http://10.0.2.2:80/MLBenchmarking/file2.php";

    //Timing and date format for timestamp
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
    String format = simpleDateFormat.format(new Date());

    long totalTime1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bayes);

        final Button BDownload = (Button) findViewById(R.id.BDownload);
        final Button BTest = (Button)findViewById(R.id.BTest);


        BDownload.setEnabled(false);
        BTest.setEnabled(false);

        final Button BTrain = (Button) findViewById(R.id.BTrain);
        BTrain.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText param1 = (EditText) findViewById(R.id.BParameter1value);
                EditText param2 = (EditText) findViewById(R.id.BParameter2value);

                long startTime = System.currentTimeMillis();

                String x = param1.getText().toString();
                String s = param2.getText().toString();


                String filename1 = "trainsetcommand";

                InputMethodManager hidekeyboard = (InputMethodManager) getSystemService(RandomForest.INPUT_METHOD_SERVICE);

                //Hide the keyboard
                hidekeyboard.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                //validating parameter inputs
                if(x.matches("")||s.matches("")||s.matches("")){ Toast.makeText(Bayes.this,
                        "Please enter parameters", Toast.LENGTH_SHORT).show();
                    return;}
                    else{
                    //Sending the command by appending to string and sending the file to the server
                    String filecontent1 = "java -cp \"wekaSTRIPPED.jar\" weka.classifiers.bayes.NaiveBayes -x "+ x +" -s " + s + " -t \"youtube1.arff\" -d youtube.model";

                FileOperations fop1 = new FileOperations();
                fop1.write(filename1, filecontent1);
                if(fop1.write(filename1, filecontent1)){
                    Toast.makeText(getApplicationContext(), "Generating Model on Server", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "I/O error", Toast.LENGTH_SHORT).show();

                }
                String filename3 = "trainsetcommand.txt";

                //uploading the parameters
                new UploadService.UploadFile().execute(uploadFilePath,filename3);

                //executing the PHP script
                new PHPFileExecutor.updateData().execute(upLoadServerUri);

                long endTime = System.currentTimeMillis();
                totalTime1 = endTime - startTime;
                    BDownload.setEnabled(true);

            }}
        });




        BDownload.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                new PHPFileExecutor.updateData().execute(upLoadServerUri);
                try {
                    sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                new DownloadService.DownloadFile().execute();

                File file = new File(uploadFilePath+"youtube.model");
                try {
                    sleep(200);
                    if(file.exists()){
                        Toast.makeText(Bayes.this,
                                "Download Succesful", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(Bayes.this,
                                "Model not ready! Please Re-download", Toast.LENGTH_SHORT).show();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                BTest.setEnabled(true);



            }
        });



        BTest.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                String answer="";
                try {
                    long startTime = System.currentTimeMillis();

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(getAssets().open("youtube1.arff")));
                    Instances inst = new Instances(reader);
                    int trainSize = (int) Math.round(inst.numInstances() * percent / 100);
                    int testSize = inst.numInstances() - trainSize;
                    Instances test = new Instances(inst, trainSize, testSize);




                    TextView txtview = (TextView)findViewById(R.id.BAnswers);
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(Environment.getExternalStorageDirectory().getPath() + "/MLBenchmarking/youtube.model"));
                    //Classifying based on Naive Bayes downloaded model
                    Classifier cls = (weka.classifiers.bayes.NaiveBayes) ois.readObject();
                    ois.close();
                    reader.close();
                    test.setClassIndex(test.numAttributes() - 1);

                    //evaluating the model
                    Evaluation eval = new Evaluation(test);
                    eval.evaluateModel(cls, test);

                    //finding various parameters
                    double tpr= eval.truePositiveRate(0);
                    double tnr= eval.trueNegativeRate(0);
                    double fpr= eval.falsePositiveRate(0);
                    double fnr= eval.falseNegativeRate(0);
                    double hter=(fpr+fnr)/2;

                    //finding the total time of execution for testing
                    long endTime   = System.currentTimeMillis();
                    long totalTime = endTime - startTime;

                    Log.d("RandomForest", "Current Timestamp: " + format);

                    answer = eval.toSummaryString("\nResults:\nCurrent Timestamp: "+format+"\nTPR: "+tpr+ "\nTNR: "+tnr+ "\nFPR: "+ fpr + "\nFNR: "+ fnr + " \nHTER: "+ hter +"\nTotal time training: "+ (totalTime1) + " milliseconds" + "\nTotal time testing: "+ (totalTime) + " milliseconds", false);


                    System.out.println(answer);

                    txtview.setMovementMethod(new ScrollingMovementMethod());
                    txtview.setText(answer);

                } catch (Exception e) { // TODO Auto-generated catch block
                    e.printStackTrace();

                }
                String filecontent2 = "Classifier Used: Naive Bayes \n"+ answer;
                String filename="Log";

                FileOperations fop1 = new FileOperations();

                if(fop1.append(filename, filecontent2)){
                    Toast.makeText(getApplicationContext(), filename+".txt created", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "I/O error", Toast.LENGTH_SHORT).show();

                }


            }

        });


    }
    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, FirstActivity.class));
    }



}


