<?php
define('DADAPATH', '/var/upload');

// ★ポイント7★ トークンが必要なAPIでは最初にトークンの有効性を確認し、無効ならば一切の処理をしない
function check_token($token) {
  // 渡されたトークンの形式をチェック
  if(! preg_match('/\A[0-9a-f]{64}\z/', $token) ) {
    die('invalid token');
  }
}

function delete_token($token) {
  $file = DADAPATH . '/' . $token;
  unlink($file);
}

// cookieで送信されたトークンをチェックする
$token = $_COOKIE['token'];
check_token($token);

delete_token($token);
setcookie('token', '', 0, '/');
// HTTPS 接続の場合にのみ、トークンを送信する
//setcookie('token', '', 0, '/', 'easylogin.android.jssec.org', true);
?>
