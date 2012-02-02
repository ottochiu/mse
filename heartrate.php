<?php 

$server = "mysql.ottochiu.vaelen.org";
$user = "ottochiu";
$password = "qtEciLiz";
$database = "ottochiu";

mysql_connect($server, $user, $password) or die("Cannot connect to localhost");
mysql_select_db($database) or die("Cannot select database");

// A form has been submitted. Query database with input.
$query = "SELECT * FROM mse_heartrate_interval ";
$id = 0;

if ($_SERVER['REQUEST_METHOD'] == "POST") {

	$val = $_POST['record_number'];

	if (filter_var($val, FILTER_VALIDATE_INT) && $val > 0) {
		$id = $val;

		// Has been validated to be an int. Don't need to escape
		$query .= sprintf(" WHERE session_id=%d ORDER BY id", $id);
	}
}

?>

<html>
<head>
	<title>Heart rate history</title>
</head>

<body>

<form method="post" action="heartrate.php">

  Please enter record number (0 to view all): <input type="text" size="3" name="record_number" value="<? echo $id ?>"/>
  <br />
  <input type="submit">
</form>

<?php 

$result = mysql_query($query);

if (!$result || mysql_num_rows($result) == 0) {
	echo "Record not found.";
} else {

	// Fetch session data
	($session = mysql_query("SELECT start_time FROM mse_heartrate_session WHERE id = $id")) and mysql_num_rows($session) > 0 or die("Inconsistent database.".mysql_num_rows($session));

	echo "Session record start time: ". mysql_result($session, 0);
	
?>

<table border="0" cellspacing="1" cellpadding="1">
<tr>
	<td>Heartbeat interval</td>
</tr>


<?php
while ($row = mysql_fetch_assoc($result)) {
	echo "<tr>\n";
	printf("<td>%d</td>\n", $row['interval']);
	echo "</tr>\n";
}
?>

</table>

<?php

}

?>

</body>
</html>

