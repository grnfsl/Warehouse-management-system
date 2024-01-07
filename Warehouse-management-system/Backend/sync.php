<?php

require_once __DIR__ . '/resource.php';

$access_token = $_POST['access_token'];
$android_id = $_POST['android_id'];
$device_timestamp = $_POST['timestamp'];
$delta_num_rows = $_POST['num_rows'];
$device_num_operations = $_POST['num_operations'];
$data = $_POST['data'];
$version = $_POST['version'];
$decoded_data = html_entity_decode($data);
$data_arr = json_decode($decoded_data, true);

$scopeOf = checkToken($access_token);

$servername = "localhost";
$username = "root";
$password = "root123";
$dbname = "warehouse";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    echo json_encode(array('error' => "1", 'message' => $conn->connect_error));
    die;
}
$message = "test";

//check device id, if not exist, add it to db

$sql = "SELECT device_id FROM devices_id WHERE device_id='$android_id'";
$result = mysqli_query($conn, $sql);
$num_rows = mysqli_num_rows($result);

if ($num_rows == 0) {
    if (!mysqli_query($conn, "INSERT INTO devices_id (device_id) VALUES ('$android_id')")) {
        echo json_encode(array('error' => "1", 'message' => $conn->error));
        die;
    }
    $device_id = $android_id;
} else {
    $device_id = $result->fetch_assoc()['device_id'];
}

mysqli_free_result($result);
if(!isset($version)){
    $sql = "SELECT id FROM gdansk_sync WHERE device_id='$device_id'";
}
else if($version == "v2"){
    $sql = "SELECT id FROM gdansk_sync WHERE device_id='$device_id'"
            . " UNION ALL "
            . "SELECT id FROM warsaw_sync WHERE device_id='$device_id'";
}

$result = mysqli_query($conn, $sql);
$num_ops = mysqli_num_rows($result);

//check if already synced if not, add all operations to sync table
if ($device_num_operations == $num_ops) {
    $message = "Nothing to sync";
} else {
    for ($x = 0; $x < $delta_num_rows; ++$x) {
        if(!isset($data_arr[$x]['warehouse'])){
            $current_size = 'none';
            //check if size already defined by other clients with v2 app
            if ($data_arr[$x]['operation'] == 'inc' || $data_arr[$x]['operation'] == 'dec') {
                $sql_inde = "SELECT size FROM gdansk_warehouse WHERE manufacturer_name='{$data_arr[$x]['manufacture']}' AND model_name='{$data_arr[$x]['model']}'";
                $result_inde = mysqli_query($conn, $sql_inde);
                $num_rows_inde = mysqli_num_rows($result_inde);
                if($num_rows_inde == 0){
                    echo json_encode(array('error' => "1", 'message' => "no items found in warehouse while inc/dec"));
                    die;
                }
                $current_size = $result_inde->fetch_assoc()['size'];
                if($current_size  == ''){
                    $current_size = 'none';
                }       
            }
            $sql = "INSERT INTO gdansk_sync (operation, manufacture, model, price, quantity, size, device_id, timestamp) "
                    . "VALUES ('{$data_arr[$x]['operation']}', '{$data_arr[$x]['manufacture']}', '{$data_arr[$x]['model']}', '{$data_arr[$x]['price']}',"
                    . "'{$data_arr[$x]['quantity']}', '$current_size', '{$device_id}', now())";
            if (!$conn->query($sql)) {
                echo json_encode(array('error' => "1", 'message' => "deivce_id: {$device_id}" . $conn->error));
                die;
            }
        }
        else if($data_arr[$x]['warehouse'] == "gdansk"){
            $sql = "INSERT INTO gdansk_sync (operation, manufacture, model, price, quantity, size, device_id, timestamp) "
                    . "VALUES ('{$data_arr[$x]['operation']}', '{$data_arr[$x]['manufacture']}', '{$data_arr[$x]['model']}', '{$data_arr[$x]['price']}',"
                    . "'{$data_arr[$x]['quantity']}', '{$data_arr[$x]['size']}', '{$device_id}', now())";
            if (!$conn->query($sql)) {
                echo json_encode(array('error' => "1", 'message' => "deivce_id: {$device_id}" . $conn->error));
                die;
            }
        }
        else if($data_arr[$x]['warehouse'] == "warsaw"){
            $sql = "INSERT INTO warsaw_sync (operation, manufacture, model, price, quantity, size, device_id, timestamp) "
                    . "VALUES ('{$data_arr[$x]['operation']}', '{$data_arr[$x]['manufacture']}', '{$data_arr[$x]['model']}', '{$data_arr[$x]['price']}',"
                    . "'{$data_arr[$x]['quantity']}', '{$data_arr[$x]['size']}', '{$device_id}', now())";
            if (!$conn->query($sql)) {
                echo json_encode(array('error' => "1", 'message' => "deivce_id: {$device_id}" . $conn->error));
                die;
            }
        }
        else {
            echo json_encode(array('error' => "1", 'message' => "selected warehouse invalid1"));
            exit();
        }
    }

//update the warehouses' tables with new products

    for ($x = 0; $x < $delta_num_rows; ++$x) {
        if(!isset($data_arr[$x]['warehouse']) || $data_arr[$x]['warehouse'] == "gdansk"){
            $table = "gdansk_warehouse";
        }
        else if($data_arr[$x]['warehouse'] == "warsaw"){
            $table = "warsaw_warehouse";
        }
        if ($data_arr[$x]['operation'] == 'add') {
            $sql1 = "select quantity from {$table} where manufacturer_name='{$data_arr[$x]['manufacture']}' AND model_name='{$data_arr[$x]['model']}'";
            $result = mysqli_query($conn, $sql1);
            $num_rows_q = mysqli_num_rows($result);
            if ($num_rows_q == 0) {
                if(!isset($data_arr[$x]['warehouse'])){
                    $sql = "INSERT INTO {$table} (manufacturer_name, model_name, price, quantity) VALUES ('{$data_arr[$x]['manufacture']}', '{$data_arr[$x]['model']}', '{$data_arr[$x]['price']}', '{$data_arr[$x]['quantity']}')";
                }
                elseif ($data_arr[$x]['warehouse'] == "warsaw" || $data_arr[$x]['warehouse'] == "gdansk") {
                    $sql = "INSERT INTO {$table} (manufacturer_name, model_name, price, quantity, size) VALUES ('{$data_arr[$x]['manufacture']}', '{$data_arr[$x]['model']}', '{$data_arr[$x]['price']}', '{$data_arr[$x]['quantity']}', '{$data_arr[$x]['size']}')";
                }
                else{
                    echo json_encode(array('error' => "1", 'message' => "selected warehouse invalid"));
                    exit();
                }
                if (!mysqli_query($conn, $sql)) {
                    echo json_encode(array('error' => "1", 'message' => $conn->error));
                    die;
                }
            }
        } else if ($data_arr[$x]['operation'] == 'rmv') {
            $sql1 = "select quantity from {$table} where manufacturer_name='{$data_arr[$x]['manufacture']}' AND model_name='{$data_arr[$x]['model']}'";
            $result = mysqli_query($conn, $sql1);
            $num_rows_q = mysqli_num_rows($result);
            if ($num_rows_q != 0) {
                $sql = "DELETE FROM {$table} WHERE manufacturer_name='{$data_arr[$x]['manufacture']}' AND model_name='{$data_arr[$x]['model']}'";
                if (!mysqli_query($conn, $sql)) {
                    echo json_encode(array('error' => "1", 'message' => $conn->error));
                    die;
                }
            }
        } else if ($data_arr[$x]['operation'] == 'inc') {
            $sql1 = "select quantity from {$table} where manufacturer_name='{$data_arr[$x]['manufacture']}' AND model_name='{$data_arr[$x]['model']}'";
            $result = mysqli_query($conn, $sql1);
            $num_rows_q = mysqli_num_rows($result);
            if ($num_rows_q != 0) {
                $newValue = intval($result->fetch_assoc()['quantity']) + intval($data_arr[$x]['quantity']);
                $sql = "UPDATE {$table} SET quantity=$newValue WHERE manufacturer_name='{$data_arr[$x]['manufacture']}' AND model_name='{$data_arr[$x]['model']}'";
                if (!mysqli_query($conn, $sql)) {
                    echo json_encode(array('error' => "1", 'message' => $conn->error));
                    die;
                }
            }
        } else if ($data_arr[$x]['operation'] == 'dec') {
            $sql1 = "select quantity from {$table} where manufacturer_name='{$data_arr[$x]['manufacture']}' AND model_name='{$data_arr[$x]['model']}'";
            $result = mysqli_query($conn, $sql1);
            $num_rows_q = mysqli_num_rows($result);
            if ($num_rows_q != 0) {
                $newValue = intval($result->fetch_assoc()['quantity']) - intval($data_arr[$x]['quantity']);
                if ($newValue >= 0) {
                    $sql = "UPDATE {$table} SET quantity=$newValue WHERE manufacturer_name='{$data_arr[$x]['manufacture']}' AND model_name='{$data_arr[$x]['model']}'";
                    if (!mysqli_query($conn, $sql)) {
                        echo json_encode(array('error' => "1", 'message' => $conn->error));
                        die;
                    }
                }
            }
        }
    }
    $message = "Synced";
}

//get all rows after last sync
$updated_items_r = array();
$updated_items_c = array();

if(!isset($version)){
    $sql = "SELECT * FROM gdansk_sync WHERE timestamp > '{$device_timestamp}' ORDER BY timestamp";
}
else if($version == "v2"){
    $sql = "SELECT *, 'gdansk' AS warehouse FROM gdansk_sync WHERE timestamp > '{$device_timestamp}' "
         . "UNION ALL "
         . "SELECT *, 'warsaw' AS warehouse FROM warsaw_sync WHERE timestamp > '{$device_timestamp}' ORDER BY timestamp";
}

$result_updated = mysqli_query($conn, $sql);
$num_rows_updated = mysqli_num_rows($result_updated);

if ($num_rows_updated != 0) {
    for ($x = 0; $x < $num_rows_updated; $x++) {
        $row_updated = mysqli_fetch_assoc($result_updated);
        $updated_items_c['operation'] = $row_updated['operation'];
        $updated_items_c['manufacture'] = $row_updated['manufacture'];
        $updated_items_c['model'] = $row_updated['model'];
        $updated_items_c['price'] = $row_updated['price'];
        $updated_items_c['quantity'] = $row_updated['quantity'];
        if (isset($version) && $version == "v2") {
            $updated_items_c['size'] = $row_updated['size'];
            $updated_items_c['warehouse'] = $row_updated['warehouse'];
        }
        $updated_items_r[$x] = $updated_items_c;
    }
    $server_timestamp = $row_updated['timestamp'];
    $message = "Synced";
} else {
    $server_timestamp = $device_timestamp;
}


echo json_encode(array('error' => "0", 'message' => $message, 'server_timestamp' => $server_timestamp, 'new_data' => $updated_items_r, 'num_rows_updated' => $num_rows_updated));

$conn->close();
?>
