<?php
require_once 'vendor/autoload.php';

// Get $id_token via HTTPS POST.
$id_token = $_POST['idToken'];
//$CLIENT_ID = $_POST['email'];

$client = new Google_Client(['client_id' => $CLIENT_ID]);  // Specify the CLIENT_ID of the app that accesses the backend
$payload = $client->verifyIdToken($id_token);

require_once __DIR__.'/authenticate.php';
require_once __DIR__.'/server.php';

if ($payload) {
  $userid = $payload['sub'];
  $client_id = $payload['email'];

  $client_secret = $server->getStorage('authorization_code')->getClientDetails($client_id)['client_secret'];
  $scopeOf = $server->getStorage('authorization_code')->getClientDetails($client_id)['scope'];

  if($client_secret == null){
	print "user_does_not_exist";
	die;
  } 
  $accessToken = getAccessToken($client_id, $client_secret);
  echo json_encode(array('accessToken' => $accessToken, 'scope' => $scopeOf));
} else {
  print "invalid_id_token";
}



?>
