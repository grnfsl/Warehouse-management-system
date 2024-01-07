<?php
$dsn      = 'mysql:dbname=my_oauth2_db;host=localhost';
$username = 'root';
$password = 'root123';

// error reporting (this is a demo, after all!)
ini_set('display_errors',1);error_reporting(E_ALL);

// Autoloading (composer is preferred, but for this example let's just do this)
require_once('oauth2-server-php/src/OAuth2/Autoloader.php');
OAuth2\Autoloader::register();

// $dsn is the Data Source Name for your database, for exmaple "mysql:dbname=my_oauth2_db;host=localhost"
$storage = new OAuth2\Storage\Pdo(array('dsn' => $dsn, 'username' => $username, 'password' => $password));

// configure the server for OpenID Connect
$config['use_openid_connect'] = true;
$config['issuer'] = '192.168.0.104';

// Pass a storage object or array of storage objects to the OAuth2 server class
$server = new OAuth2\Server($storage, $config);

$publicKey  = file_get_contents('/home/goran/test/pub.pem');
$privateKey = file_get_contents('/home/goran/test/server.pem');
// create storage
$keyStorage = new OAuth2\Storage\Memory(array('keys' => array(
    'public_key'  => $publicKey,
    'private_key' => $privateKey,
)));

$server->addStorage($keyStorage, 'public_key');

// Add the "Client Credentials" grant type (it is the simplest of the grant types)
$server->addGrantType(new OAuth2\GrantType\ClientCredentials($storage));

// Add the "Authorization Code" grant type (this is where the oauth magic happens)
$server->addGrantType(new OAuth2\GrantType\AuthorizationCode($storage));
?>
