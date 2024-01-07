<?php

require_once __DIR__.'/resource.php';

$access_token = $_POST['access_token'];
$operation = $_POST['operation'];

$manufacturer = $_POST['manufacturer'];
$model = $_POST['model'];
$price = floatval($_POST['price']);
$quantity = intval($_POST['quantity']);
$table_dest = $_POST['table'];

$scopeOf = checkToken($access_token);

$servername = "localhost";
$username = "root";
$password = "root123";
$dbname = "warehouse";

if(isset($table_dest)){
    if($table_dest == "gdansk_warehouse"){
        $table = "gdansk_products";
    }
    else if($table_dest == "warsaw_warehouse"){
        $table = "warsaw_warehouse";
    }
    else {
        exit();
    }
}
else{
    $table = "gdansk_products";
}

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
} 

if($operation == "add"){
	$sql = "INSERT INTO '$table' (manufacturer_name, model_name, price, quantity) VALUES ('$manufacturer', '$model', '$price', '$quantity')";

	if ($conn->query($sql) === TRUE) {
	    print "successfull";
	} else {
    		print "Error: " . $sql . "<br>" . $conn->error;
	}
}
elseif ($operation == "increase"){
	$sql1 = "select quantity from '$table' where manufacturer_name='$manufacturer' AND model_name='$model'";

	$result = $conn->query($sql1);
	$newValue = intval($result->fetch_assoc()['quantity'])+$quantity;	
	
	$sql = "UPDATE '$table' SET quantity=$newValue WHERE manufacturer_name='$manufacturer' AND model_name='$model'";

	if ($conn->query($sql) === TRUE) {
	    echo "Record updated successfully";
	} else {
	    echo "Error updating record: " . $conn->error;
	}
}
elseif ($operation == "decrease"){
	$sql1 = "select quantity from '$table' where manufacturer_name='$manufacturer' AND model_name='$model'";

	$result = $conn->query($sql1);	
	$newValue = intval($result->fetch_assoc()['quantity'])-$quantity;
	if($newValue < 0){
		print "less_than_zero";	
		die;
	}
	$sql = "UPDATE '$table' SET quantity=$newValue WHERE manufacturer_name='$manufacturer' AND model_name='$model'";

	if ($conn->query($sql) === TRUE) {
 	   print "successfull";
	} else {
	    print "Error updating record: " . $conn->error;
	}
}
elseif ($operation == "remove" && $scopeOf == "manager"){
	$sql = "DELETE FROM '$table' WHERE manufacturer_name='$manufacturer' AND model_name='$model'";
	if ($conn->query($sql) === TRUE) {
    		print "successfull";
	} else {
  		print "Error deleting record: " . $conn->error;
	}
}
else{
	print "undefined_operation";
	die;
}

$conn->close();

?>
