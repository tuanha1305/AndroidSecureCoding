<?php
define('DADAPATH', '/var/upload');

// ★ポイント7★ トークンが必要なAPIでは最初にトークンの有効性を確認し、無効ならば一切の処理をしない
function check_token($token) {
  // 渡されたトークンの形式をチェック
  if(! preg_match('/\A[0-9a-f]{64}\z/', $token) ) {
    die('invalid token');
  }
  
  // トークンに紐付いたデータ有無をチェック
  $file = DADAPATH . '/' . $token;
  if(! is_readable($file)) {
    die('invalid token');
  }
}

function write_file($token, $text) {
  $file = DADAPATH . '/' . $token;
  $fp = fopen($file, 'wb');
  if ($fp === FALSE) die('write file error.');
  
  $len = fwrite($fp, $text, strlen($text));
  if ($len === FALSE) die('write file error.');

  fclose($fp);
}

function parse_json() {
  // リクエストBODYを取得し、JSONデータとしてパースする
  $json = file_get_contents('php://input');
  $obj = json_decode($json);

  $text = $obj->{'text'};
  echo($text);
  
  return $text;
}

// cookieで送信されたトークンをチェックする
$token = $_COOKIE['token'];
check_token($token);

$text = parse_json();
write_file($token, $text);
?>
