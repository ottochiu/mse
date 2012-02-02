<?
// This script accepts POST data for populating the database with heartbeat intervals.
// It does not display anything unless $_GET['manual'] is set. 
$server = "mysql.ottochiu.vaelen.org";
$user = "ottochiu";
$password = "qtEciLiz";
$database = "ottochiu";

if ($_SERVER['REQUEST_METHOD'] == 'POST' &&
		array_key_exists('start_time', $_POST) &&
		array_key_exists('intervals', $_POST)) {
	// need the following information:
	// 1. session start time
	// 2. string of heartbeat intervals, comma separated
	
	mysql_connect($server, $user, $password) or die("Cannot connect to localhost");
	mysql_select_db($database) or die("Cannot select database");
	
	$session_query = "INSERT INTO `mse_heartrate_session` (`start_time`) VALUES ('%s')";
	$values_query = "INSERT INTO `mse_heartrate_interval` (`session_id`, `interval`) VALUES ";

	// MySQL MyISAM does not support transactions. Therefore, do the validation manually before sending off the queries.
	strtotime($_POST['start_time']) or die("Bad time format");
	
	$values = array();
	
	// validate each value and append to $values_query. The session_id has to be filled in later.
	foreach (explode(',', $_POST['intervals']) as $v) {
		is_numeric($v) or die("Bad heartrate interval format");
		
		$values[] = sprintf('(%%1$d, %d)', $v);
	}
	
	mysql_query(sprintf($session_query, mysql_real_escape_string($_POST['start_time'])));
	mysql_query(sprintf($values_query.implode(',', $values), mysql_insert_id()));
	
} else if ($_SERVER['REQUEST_METHOD'] == 'GET' && array_key_exists('manual', $_GET)) {

	// output the HTML form
	$start_time = strftime('%F %T');
?>

	<html>
	<head>
		<title>Create Heartrate Session</title>
	</head>
	
	<body>

		<form method="post" action="heartrate_input.php">
		<table>
		<tr>
			<td>Session start time:</td>
			<td><? echo $start_time ?></td>
		<tr>
			<td>Heartbeat intervals (ms)<br><i>Comma separated</i></td>
			<td><input type="text" name="intervals" /></td>
		</tr>
		</table>
		
		<input type="hidden" name="start_time" value="<? echo $start_time ?>" />
		<input type="submit" />
		
		</form>
		
	</body>
	</html>
		
<?
	
}

?>
