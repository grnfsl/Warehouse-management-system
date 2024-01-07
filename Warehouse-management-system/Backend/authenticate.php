<?php
// include our OAuth2 Server object


function getAccessToken($user, $pass) {

	require_once __DIR__.'/Server.php';

	$user_id = $user;

	//var_dump($server->getStorage('access_token')->getUser($user));
	// create a request object to mimic an authorization code request
	$request1 = new OAuth2\Request(array(
 	   'client_id'     => $user,
 	   'response_type' => 'code',
 	   'state'         => 'xyz'
	));

	$response1 = new OAuth2\Response();
	$server->handleAuthorizeRequest($request1,$response1, true, $user_id);
	if(!$server->validateAuthorizeRequest($request1, $response1)){
		print "failed_validation";	
		die;
	}

	// parse the returned URL to get the authorization code
	$parts = parse_url($response1->getHttpHeader('Location'));
	parse_str($parts['query'], $query);

	// pull the code from storage and verify an "id_token" was added
	$code = $server->getStorage('authorization_code') ->getAuthorizationCode($query['code']);
		
	$request2 = new OAuth2\Request(array(), array(
   		 'grant_type' => 'client_credentials',
   		 'code'       => $code['authorization_code']
	), array(), array(), array(),
	array(
  		  'REQUEST_METHOD' => 'POST',
 		  'PHP_AUTH_USER'  => $user,
 		  'PHP_AUTH_PW'    => $pass
	));

	//request for access token
	$response2 = new OAuth2\Response();
	$server->handleTokenRequest($request2,$response2);

	if($response2->getStatusCode()!=200){
	     print "failed_token_request";
	     die;
	}
	return $response2->getParameters()['access_token'];
}
?>
