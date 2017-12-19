package com.example.group17.mlbenchmarking;


/*
References of the code used throughout the project:
Android codes provided by the Prof. Ayan Banerjee.
https://stackoverflow.com/
https://androidexample.com/
Developer.android.com
www.cs.waikato.ac.nz
Weka.sourceforge.net
 */


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import weka.core.Instances;


public class FirstActivity extends AppCompatActivity {

    //Defining various Seekbars and Textviews

    SeekBar barValue;
    static TextView textView03;

    //Variable used to set the split percentage
    static int percent;

    ProgressDialog dialog = null;

    String upLoadServerUri = null;

    //File Path
    final String uploadFilePath = (Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "/MLBenchmarking/");
    final String uploadFileName = "youtube1.arff";

    //for spinner
    Spinner spinner;
    String[] SPINNERVALUES = {"NaiveBayes","SupportVectorMachine","RandomForest","DecisionTree"};
    String SpinnerValue;
    Button spinnerValue;
    Intent intent;

    //OnCreate to set the Main UI
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_first);
        seekvalue();

        //Creating the correct directory
        File directory = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"/MLBenchmarking");
        if (!directory.exists()) {
            directory.mkdir();
        }



        spinner =(Spinner)findViewById(R.id.spinner1);

        spinnerValue = (Button)findViewById(R.id.button1);
        spinnerValue.setEnabled(false);

        //Defining all algorithms using arraylist

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(FirstActivity.this, android.R.layout.simple_list_item_1, SPINNERVALUES);

        spinner.setAdapter(adapter);

        //Adding setOnItemSelectedListener method on spinner.
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                SpinnerValue = (String)spinner.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });

        //Selecting the algorithm using spinner
        spinnerValue.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                switch(SpinnerValue){

                    case "SupportVectorMachine":
                        intent = new Intent(FirstActivity.this, SupportVectorMachine.class);
                        startActivity(intent);
                        break;

                    case "NaiveBayes":
                        intent = new Intent(FirstActivity.this, Bayes.class);
                        startActivity(intent);
                        break;

                    case "RandomForest":
                        intent = new Intent(FirstActivity.this, RandomForest.class);
                        startActivity(intent);
                        break;

                    case "DecisionTree":
                        intent = new Intent(FirstActivity.this, DT.class);
                        startActivity(intent);
                        break;




                }
            }
        });

        //Defining Upload Database button and disabling it.. Since user needs to split the database first
        final Button UploadDatabase = (Button) findViewById(R.id.UploadDatabase);
        UploadDatabase.setEnabled(false);


        /************* Php script path ****************/
        upLoadServerUri = "http://10.0.2.2:80/MLBenchmarking/index.php";
        Log.d("upload",upLoadServerUri);

        File file = new File(uploadFilePath+"youtube1.arff");
        if(file.exists()){
        }
        else{
            Toast.makeText(FirstActivity.this,
                    "Dataset Missing", Toast.LENGTH_SHORT).show();
        }


        UploadDatabase.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                spinnerValue.setEnabled(true);

                new UploadService.UploadFile().execute(uploadFilePath,uploadFileName);

                TextView theFact = (TextView) findViewById(R.id.textView03);
                String shareFact = theFact.getText().toString();

                String filename = "dividesetcommand";
                String filecontent = "java -cp \"wekaSTRIPPED.jar\" weka.filters.unsupervised.instance.RemovePercentage -P " + shareFact +" -i \"youtube1.arff\" -o train.arff";

                FileOperations fop = new FileOperations();
                fop.write(filename, filecontent);
                if(fop.write(filename, filecontent)){
                    Toast.makeText(getApplicationContext(), "Dataset uploaded and Splited", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "I/O error", Toast.LENGTH_SHORT).show();

                }

                new UploadService.UploadFile().execute(uploadFilePath,filename+".txt");


            }
        });


        Button SecondPageButton = (Button)findViewById(R.id.button);
        SecondPageButton.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                try {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(getAssets().open("youtube1.arff")));

                    //Using instance classes
                    Instances instance = new Instances(reader);
                    percent =  Integer.valueOf(textView03.getText().toString());

                    //Finding the Size of Dataset
                    int trainSize = (int) Math.round(instance.numInstances() * percent / 100);
                    int testSize = instance.numInstances() - trainSize;
                    //Splitting the Dataset
                    Instances train = new Instances(instance, 0, trainSize);
                    Instances test = new Instances(instance, trainSize, testSize);
                    int number1 = train.numInstances();
                    int number = test.numInstances();

                    TextView txtview = (TextView)findViewById(R.id.textView04);
                    txtview.setText("Training Instances:"+ Integer.toString(number1) + "\nTesting Instances " + Integer.toString(number));

                    //Create the file on local storage
                    File train_location= new File(Environment.getExternalStorageDirectory().getPath() + "/MLBenchmarking/train.arff");
                    BufferedWriter writer1 = new BufferedWriter(new FileWriter(new File(String.valueOf(train_location))));
                    writer1.write(train.toString());

                    File test_location= new File(Environment.getExternalStorageDirectory().getPath() + "/MLBenchmarking/test.arff");
                    BufferedWriter writer2 = new BufferedWriter(new FileWriter(new File(String.valueOf(test_location))));
                    writer2.write(train.toString());

                } catch (Exception e) { // TODO Auto-generated catch block
                    e.printStackTrace();

                }

                UploadDatabase.setEnabled(true);



            }
        });



        //Log button to show all the log of the application

        final Button LogButton = (Button) findViewById(R.id.LogButton);
        LogButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Intent activityChangeIntent = new Intent(FirstActivity.this, LogFile.class);

                // currentContext.startActivity(activityChangeIntent);

                FirstActivity.this.startActivity(activityChangeIntent);
            }
        });



    }

    //function to extract the value from seekbar
    public void seekvalue() {
        barValue = (SeekBar) findViewById(R.id.seekBar1);

        textView03 = (TextView) findViewById(R.id.textView03);
        barValue.setMax(100);
        barValue.setProgress(0);

        barValue.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {

                    int progress_value;

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        progress_value = progress;
                        textView03.setText(String.valueOf(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }

                }
        );

    }
    @Override
    public void onResume()
    {  // After a pause OR at startup
        super.onResume();
        //Refresh your stuff here

    }


}