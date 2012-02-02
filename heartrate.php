<?php 

$server = "mysql.ottochiu.vaelen.org";
$user = "ottochiu";
$password = "qtEciLiz";
$database = "ottochiu";

mysql_connect($server, $user, $password) or die("Cannot connect to localhost");
mysql_select_db($database) or die("Cannot select database");

?>

<html>
<head>
	<title>Heart rate history</title>
</head>

<body>

<form method="post" action="heartrate.php">

  Please enter record number (0 to view all): <input type="text" size="3" name="record_number"/>
  <br />
  <input type="submit">
</form>

<?php 

// A form has been submitted. Query database with input.
if ($_SERVER['REQUEST_METHOD'] == "POST") {

	$id = $_POST['record_number'];
	
	if (filter($id, FILTER_VALIDATE_INT) && $id > 0) {
		echo "GOOD";
	}
}
	
?>

</body>
</html>

