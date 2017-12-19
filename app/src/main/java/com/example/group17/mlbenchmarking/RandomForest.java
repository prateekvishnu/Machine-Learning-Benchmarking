package com.example.group17.mlbenchmarking;



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


public class RandomForest extends FirstActivity {

    //Upload String path
    final String uploadFilePath = (Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/MLBenchmarking/");
    String upLoadServerUri = "http://10.0.2.2:80/MLBenchmarking/file2.php";

    //Total time for training
    long totalTime1;

    //Total time for testing
    long totalTime;

    //timestamp when algorithm ran
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy-hh-mm-ss");
    String format = simpleDateFormat.format(new Date());


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.randomforest);

        final Button RFTrain = (Button) findViewById(R.id.RFTrain);
        final Button RFDownload = (Button) findViewById(R.id.RFDownload);
        final Button RFTest = (Button)findViewById(R.id.RFTest);


        RFDownload.setEnabled(false);
        RFTest.setEnabled(false);


        RFTrain.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                long startTime = System.currentTimeMillis();

                String filename1 = "trainsetcommand";
                EditText param1 = (EditText) findViewById(R.id.RFParameter1value);
                EditText param2 = (EditText) findViewById(R.id.RFParameter2value);
                EditText param3 = (EditText) findViewById(R.id.RFparameter3value);


                String i = param1.getText().toString();
                String k = param2.getText().toString();
                String s = param3.getText().toString();

                //Hide Keyboard
                InputMethodManager hideKeyboard = (InputMethodManager) getSystemService(RandomForest.INPUT_METHOD_SERVICE);
                hideKeyboard.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                //Validation check of all parameters if input is correct and entered

                if(i.matches("")||k.matches("")||s.matches("")) {

                    Toast.makeText(RandomForest.this,
                            "Please enter parameters", Toast.LENGTH_SHORT).show();
                return;
                }else{


                    Toast.makeText(RandomForest.this,
                            "Sending Parameters", Toast.LENGTH_SHORT).show();

                    String filecontent1 = "java -cp \"wekaSTRIPPED.jar\" weka.classifiers.trees.RandomForest -I " + i + " -K " + k + " -S " + s + " -t \"train.arff\" -d youtube.model";

                    FileOperations fop1 = new FileOperations();
                    fop1.write(filename1, filecontent1);
                    if (fop1.write(filename1, filecontent1)) {
                        Toast.makeText(getApplicationContext(), "Generating Model on Server", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "I/O error", Toast.LENGTH_SHORT).show();

                    }
                    String filename3 = "trainsetcommand.txt";
                    new UploadService.UploadFile().execute(uploadFilePath, filename3);

                    new PHPFileExecutor.updateData().execute(upLoadServerUri);

                    long endTime = System.currentTimeMillis();
                    totalTime1 = endTime - startTime;
                    RFDownload.setEnabled(true);


                }
            }
        });




        RFDownload.setOnClickListener(new View.OnClickListener() {
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
                        Toast.makeText(RandomForest.this,
                                "Download Succesful", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(RandomForest.this,
                                "Model not ready! Please Re-download", Toast.LENGTH_SHORT).show();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                RFTest.setEnabled(true);




            }
        });



        RFTest.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                String answer="";
                try {
                    long startTime = System.currentTimeMillis();

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(getAssets().open("youtube1.arff")));
                    Instances inst = new Instances(reader);
                    int trainSize = (int) Math.round(inst.numInstances() * percent / 100);
                    //Getting the test data ready
                    int testSize = inst.numInstances() - trainSize;
                    Instances test = new Instances(inst, trainSize, testSize);

                    TextView txtview = (TextView)findViewById(R.id.RFAnswers);

                    //finding the model from downloaded model local storage
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(Environment.getExternalStorageDirectory().getPath() + "/MLBenchmarking/youtube.model"));

                    //Running the classifier of random forest
                    Classifier cls = (weka.classifiers.trees.RandomForest) ois.readObject();
                    ois.close();
                    reader.close();
                    test.setClassIndex(test.numAttributes() - 1);

                    //evaluating the model using the classifier
                    Evaluation eval = new Evaluation(test);
                    eval.evaluateModel(cls, test);

                    //defining other parameters
                    double tpr= eval.truePositiveRate(0);
                    double tnr= eval.trueNegativeRate(0);
                    double fpr= eval.falsePositiveRate(0);
                    double fnr= eval.falseNegativeRate(0);
                    double hter=(fpr+fnr)/2;

                    long endTime   = System.currentTimeMillis();
                    totalTime = endTime - startTime;

                    Log.d("RandomForest", "Current Timestamp: " + format);

                    answer = eval.toSummaryString("\nResults:\nCurrent Timestamp: "+format+"\nTPR: "+tpr+ "\nTNR: "+tnr+ "\nFPR: "+ fpr + "\nFNR: "+ fnr + " \nHTER: "+ hter +"\nTotal time training: "+ (totalTime1) + " milliseconds" + "\nTotal time testing: "+ (totalTime) + " milliseconds", false);


                    System.out.println(answer);

                    txtview.setMovementMethod(new ScrollingMovementMethod());
                    txtview.setText(answer);

                } catch (Exception e) { // TODO Auto-generated catch block
                    e.printStackTrace();

                }
                String filecontent2 = "Classifier Used: Random Forest \n"+ answer;
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


