<?php 

$server = "mysql.ottochiu.vaelen.org";
$user = "ottochiu";
$password = "qtEciLiz";
$database = "ottochiu";

mysql_connect($server, $user, $password) or die("Cannot connect to localhost");
mysql_select_db($database) or die("Cannot select database");

// A form has been submitted. Query database with input.
$query = "SELECT * FROM mse_heartrate_interval AS vti LEFT JOIN mse_heartrate_session AS session ON session_id = session.id ";
$id = 0;

if ($_SERVER['REQUEST_METHOD'] == "POST") {

	$val = $_POST['record_number'];

	if (filter_var($val, FILTER_VALIDATE_INT) && $val > 0) {
		$id = $val;

		// Has been validated to be an int. Don't need to escape
		$query .= sprintf("WHERE session_id=%d ", $id);
	}
}

$query .= "ORDER BY session_id, vti.id";

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

?>

<table border="1" cellspacing="0" cellpadding="2">
<tr>
	<td>Session #</td>
	<td>Heartbeat interval</td>
	<td>Session start time</td>
</tr>


<?php
while ($row = mysql_fetch_assoc($result)) {
	echo "<tr>\n";
	printf("<td>%d</td>\n", $row['session_id']);
	printf("<td>%d</td>\n", $row['interval']);
	printf("<td>%s</td>\n", $row['start_time']);
	echo "</tr>\n";
}
?>

</table>

<?php

}

?>

</body>
</html>


