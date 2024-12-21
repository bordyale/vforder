<? 


$ip = read_last_line('pubIp.status');

function read_last_line ($file_path){

$line = '';

$f = fopen($file_path, 'r');
$cursor = -1;

fseek($f, $cursor, SEEK_END);
$char = fgetc($f);

/**
* Trim trailing newline chars of the file
*/
while ($char === "\n" || $char === "\r") {
    fseek($f, $cursor--, SEEK_END);
    $char = fgetc($f);
}

/**
* Read until the start of file or first newline char
*/
while ($char !== false && $char !== "\n" && $char !== "\r") {
    /**
     * Prepend the new char
     */
    $line = $char . $line;
    fseek($f, $cursor--, SEEK_END);
    $char = fgetc($f);
}

return $line;
}

$url = 'https://';

$url .= $ip;

$url .= ':5001/borFinance/control/main';
//echo $url;

header("Location: $url");

?>

