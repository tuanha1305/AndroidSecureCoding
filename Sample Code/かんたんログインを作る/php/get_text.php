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

function read_file($file) {
  $fp = fopen($file, 'rb');
  if ($fp === FALSE) die('read file error.');

  if( filesize($file) != 0) {
    $text = fread($fp, filesize($file));
    if( $text === FALSE) die('read file error.');
  } else {
    $text = '';
  }

  fclose($fp);
  return $text;
}

function put_text($token) {
  // ファイルからデータを読み取る
  $text= read_file(DADAPATH . '/' . $token);
  $arr = array('text' => $text);

  // JSONとして出力
  header('Content-type: application/json');  
  echo json_encode($arr); 
}

// cookieで送信されたトークンをチェックする
$token = $_COOKIE['token'];
check_token($token);

put_text($token);
?>
