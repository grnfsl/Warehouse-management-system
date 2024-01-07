<?php
$client_id = $_POST['client_id'];
$client_secret = $_POST['client_secret'];


require_once __DIR__.'/authenticate.php';
require_once __DIR__.'/resource.php';


$accessToken = getAccessToken($client_id, $client_secret);


$scopeOf = checkToken($accessToken);
echo json_encode(array('accessToken' => $accessToken, 'scope' => $scopeOf));

?>


