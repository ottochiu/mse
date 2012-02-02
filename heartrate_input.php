<?
echo $_SERVER['REQUEST_METHOD'];
// This script accepts POST data for populating the database with heartbeat intervals.
// It does not display anything unless $_GET['manual'] is set. 

if ($_SERVER['REQUEST_METHOD'] == 'POST') {
	// need the following information:
	// 1. session start time
	// 2. string of heartbeat intervals, comma separated
	
	
} else if ($_SERVER['REQUEST_METHOD'] == 'GET' && array_key_exists('manual', $_GET)) {

	// output the HTML form
	$start_time = strftime('%x');
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
