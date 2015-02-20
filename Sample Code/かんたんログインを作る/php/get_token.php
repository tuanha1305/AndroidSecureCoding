<?php
define('APIKEYVALUE', '0123456789');
define('DADAPATH', '/var/upload');

// ★ポイント6★ 発行するトークンはユニーク性と推測不可能性を保証する
function getToken() {
  $count = 0;
  do {
    // 暗号学的に推測不可能とされる乱数を生成し、トークンとして利用する。
    $s = file_get_contents('/dev/urandom', false, NULL, 0, 32);
    $token = bin2hex($s);
    
    // 既存トークンとの競合確認を行い、トークンのユニーク性を保証する。
    $file = DADAPATH . '/' . $token;
    $fp = @fopen($file, 'x');
  } while ($fp === FALSE && ++$count < 10);
  if ($fp === FALSE) die('cannot generate token.');
  fclose($fp);
  return $token;
}

// 正しいAPI KEYを持った相手からのみアクセスを許可する
$api_key = $_COOKIE['api_key'];
if (! $api_key || $api_key != APIKEYVALUE) {
  die('invalid api key');
}
$token = getToken();
// HTTPS 接続の場合にのみ、トークンを送信する
setcookie('token', $token, 0, '/', $_SERVER['SERVER_NAME'], true); 
?>
