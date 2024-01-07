<?php
// include our OAuth2 Server object

function checkToken($at) {
	require_once __DIR__.'/server.php';

	//request from access token
	$requestToken = new OAuth2\Request(array(), array(
 	   'access_token' => $at),
 	   array(), array(), array(),
 	   array(
 	       'REQUEST_METHOD' => 'POST'
	));
	// Handle a request to a resource and authenticate the access token
	if (!$server->verifyResourceRequest($requestToken)) {
	    print "invalid_access_token";
 	    die;
	}
	$token = $server->getAccessTokenData($requestToken);

	if ($token['scope'] == 'manager')
		return "manager";
	else 
		return "employee";
}

/*
// Handle a request to a resource and authenticate the access token
if (!$server->verifyResourceRequest(OAuth2\Request::createFromGlobals())) {
    $server->getResponse()->send();
    die;
}
echo json_encode(array('success' => true, 'message' => 'You accessed my APIs!'));*/
?>
