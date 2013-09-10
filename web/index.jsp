<%--

    Copyright © 2013 BIREME/PAHO/WHO

    This file is part of SocialCheckLinks.

    SocialCheckLinks is free software: you can redistribute it and/or 
    modify it under the terms of the GNU Lesser General Public License as 
    published by the Free Software Foundation, either version 2.1 of 
    the License, or (at your option) any later version.

    SocialCheckLinks is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public 
    License along with SocialCheckLinks. If not, see 
    <http://www.gnu.org/licenses/>.

--%>

<%@page language="java"%>
<%@page session="true" %>
<%@page import="java.util.*,br.bireme.scl.*" %>
<%@page contentType="text/html;charset=UTF-8"%>

<% 
    final String lang = (String)request.getParameter("lang");
    final ResourceBundle messages = Tools.getMessages(lang);
%>

<!-- ================================================== -->

<!doctype html>
<html>
<head>
	<title>LOGIN - <%=messages.getString("bireme_social_checklinks")%></title>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1" />
	<meta name="HandheldFriendly" content="true">
	<link href="css/bootstrap.css" rel="stylesheet">
	<link href="css/styles.css" rel="stylesheet">
	<style type="text/css">
		body {padding-top:40px; padding-bottom:40px; background-color: #f5f5f5;}
		.loginTitle {max-width: 500px; margin: 0 auto 20px auto; text-align: center;}
    </style>

	<!--[if (lt IE 9)&(!IEMobile)]>
	<link rel="stylesheet" type="text/css" href="css/ie.css" />
	<![endif]-->
	<script type="text/javascript" src="js/modernizr.js"></script>
</head>
<body>
	<div class="container">
		<div class="loginTitle">
			<img src="img/logo.png" alt="BIREME Logo" />
			<h1><%=messages.getString("bireme_social_checklinks")%></h1>
		</div>
		<form class="form-signin" action="authenticate?lang=<%=lang%>" method="post">
			<h2 class="form-signin-heading"><%=messages.getString("please_sign_in")%></h2>
			<input type="email" class="input-block-level" placeholder="<%=messages.getString("email_address")%>" name="email"/>
			<input type="password" class="input-block-level" placeholder="<%=messages.getString("password")%>" name="password"/>
			<button class="btn btn-large btn-primary" type="submit"><%=messages.getString("sign_in")%></button>
		</form>
        </div> <!-- /container -->
   

	<!-- javascript
    ================================================== -->
    <script src="js/jquery.js"></script>
    <script src="js/bootstrap-transition.js"></script>
    <script src="js/bootstrap-alert.js"></script>
    <script src="js/bootstrap-modal.js"></script>
    <script src="js/bootstrap-dropdown.js"></script>
    <script src="js/bootstrap-scrollspy.js"></script>
    <script src="js/bootstrap-tab.js"></script>
    <script src="js/bootstrap-tooltip.js"></script>
    <script src="js/bootstrap-popover.js"></script>
    <script src="js/bootstrap-button.js"></script>
    <script src="js/bootstrap-collapse.js"></script>
    <script src="js/bootstrap-carousel.js"></script>
    <script src="js/bootstrap-typeahead.js"></script>	
</body>
</html>