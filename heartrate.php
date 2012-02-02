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



</form>

Please enter record number (0 to view all): <input type="text" size="3" name="record_number"/>
</body>
</html>

