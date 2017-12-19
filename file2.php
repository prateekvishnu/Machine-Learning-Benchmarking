<?php


$firstline= `head -n1 trainsetcommand.txt`;
echo $firstline;

$output=shell_exec($firstline);

echo $output;

?>