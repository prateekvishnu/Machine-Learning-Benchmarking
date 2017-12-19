<?php
  
    $file_path = “uploads/”;
     
    $file_path = $file_path . basename( $_FILES['uploaded_file']['name']);
    if(move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $file_path)) {
        echo "success";
    } else{
        echo "fail";
    }
    system("./AppFinder.o"); 




$firstline= `head -n1 dividesetcommand.txt`;
Echo $firstline;

$output=shell_exec($firstline);

echo "<pre>$output</pre>";


?>
