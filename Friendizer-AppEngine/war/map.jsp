<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.teamagly.friendizer.model.User" %>
<%@ page import="com.teamagly.friendizer.PMF" %>
<%@ page import="javax.jdo.*" %>
<%@ page import="java.util.List" %>
<html>
	<head>
  <meta charset="utf-8">
		<title>Map</title>
		<meta charset="utf-8">
  <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.6.4/jquery.min.js"></script>
  <script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=true"></script>
  <script type="text/javascript" src="gmaps.js"></script>
  <style type="text/css">
        html, body {
                height: 100%;
                margin: 0;
        }

        #map {
                min-height: 100%; 
        }
    </style>
		<script type="text/javascript">
	var map;
	$(document).ready(function(){
	  map = new GMaps({
		div: '#map',
		lat: 32.0735931396,
		lng: 34.8553276062,
		zoomControl : true,
		zoomControlOpt: {
			style : 'SMALL',
			position: 'TOP_LEFT'
		},
		zoom: 4,
		panControl : false,
		streetViewControl : false,
		mapTypeControl: false,
		overviewMapControl: false
	  });
		<% 
		PersistenceManager pm = PMF.get().getPersistenceManager();
		Query query = pm.newQuery(User.class);
		List<User> users = (List<User>) query.execute();
		query.closeAll();
		for (User user : users){
			if (user.getLatitude() != -1 && user.getLongitude() != -1) {
%>

	  map.addMarker({
		lat: <%=user.getLatitude()%>,
		lng: <%=user.getLongitude()%>
	  });
	  <%
	  }
		}
		pm.close();
		%>
  });
		</script>
	</head>
	<body>
	  <div id="map"></div>
	</body>
</html>