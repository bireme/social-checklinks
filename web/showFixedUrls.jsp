<%-- 
    Document   : showFixedUrls
    Created on : 06/08/2013, 11:53:39
    Author     : Heitor Barbieri
--%>

<%@page language="java"%>
<%@page session="true" %>
<%@page import="java.util.Set,com.mongodb.DBCollection,br.bireme.scl.*" %>

<% 
    final String user = (String)session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("index.html");
    }

    final String url = (String)session.getAttribute("url");
    final Set<IdUrl> fixed = (Set<IdUrl>)session.getAttribute("IdUrls");
    final int fixedUrls = fixed.size();
    final int group = (Integer)session.getAttribute("group");
    final int lastGroup = (fixedUrls / 15);
    final int initGroup = (group <= 1) ? 0 : (group >= lastGroup - 2) 
                                                 ? (lastGroup -5) : (group - 2);
    final String firstActive = "enabled";
    final String lastActive = "enabled";    
    final DBCollection coll = (DBCollection)getServletContext()
                                                    .getAttribute("collection");
    final String centerId = (String)session.getAttribute("centerId");
    final int mUrls = MongoOperations.getCenterUrlsNum(coll, centerId);
    
    session.setAttribute("maxUrls", mUrls);
%>

<!-- ================================================== -->

<@%page contentType="text/html" pageEncoding="UTF-8"%>

<!doctype html>
<html>
<head>
	<title>BIREME Social Checklinks</title>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1" />
	<meta name="HandheldFriendly" content="true">
	<link href="css/bootstrap.css" rel="stylesheet">
	<link href="css/bootstrap-responsive.css" rel="stylesheet">
	<link href="css/styles.css" rel="stylesheet">
	<style type="text/css">
		html, body {
			height: 100%; /* The html and body elements cannot have any padding or margin. */
		}
    </style>

	<!--[if (lt IE 9)&(!IEMobile)]>
	<link rel="stylesheet" type="text/css" href="css/ie.css" />
	<![endif]-->
	<script type="text/javascript" src="js/modernizr.js"></script>
</head>
<body>
	<div id="wrap">
		<div class="navbar navbar-inverse navbar-fixed-top">
			<div class="navbar-inner">
				<div class="container">
					<button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
					</button>
					<a class="brand" href="#">BIREME Social CheckLinks</a>
					<div class="nav-collapse collapse">
						<ul class="nav">
							<li class="active"><a href="list.jsp?group=0">Home</a></li>
							<li><a href="#about">About</a></li>
							<li><a href="http://reddes.bvsalud.org/">Contact</a></li>
						</ul>
						<ul class="nav pull-right">
							<li class="dropdown">

								<a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="icon-user icon-white"></i> <%=user%> <b class="caret"></b></a>
								<ul class="dropdown-menu">
									<li><a href="#">Personal info</a></li>
									<li><a href="#">Another action</a></li>
									<li><a href="#">Something else here</a></li>
									<li class="divider"></li>
									<li><a href="index.html"><i class="icon-off"></i> Logout</a></li>
								</ul>
							</li>
						</ul>
					</div><!--/.nav-collapse -->
				</div>
			</div>
		</div>

		<div class="container">
			<h1>URL Changes</h1>
			<div class="urlEditor">
				<div class="urlLine">
					<div class="seg-q">
						<div class="URL-tested"><%=url%></div>
						
					</div>
				</div>
				<div class="accordion">
					<div class="accordion-heading">
						<a href="#URL-list" class="accordion-toggle" data-toggle="collapse"><i class="icon-list"></i><%=fixedUrls%> URLs were affected by the changes. Check the list.</a>
					</div>
					<div id="URL-list" class="accordion-body collapse out">
						<table class="table table-condensed">
							<thead>
								<tr>
									<th>#</th>
									<th>URL</th>
								</tr>
							</thead>
							<tbody>
                                                            <%
                                                                int idxx = 0;                                                                
                                                                for (IdUrl idUrl : fixed) {
                                                            %>
                                                                <tr>
									<td><%=++idxx%></td>
									<td><a href="<%=idUrl.url%>" title="view document"><%=idUrl.url%></a></td>
								</tr>
                                                            <%    
                                                                }
                                                            %>
							</tbody>
						</table>
						<div class="pagination pagination-centered">
                                                    <ul>
                                                        <li class=<%=firstActive%>><a href="?group=0">«</a></li>
                                                        <%                                        
                                                        for (int idx = initGroup; idx < initGroup+5; idx++) {
                                                            if (idx == group) {
                                                        %>
                                                        <li class="active"><a href=""><%=idx+1%></a></li>
                                                        <%
                                                            } else if (idx <= lastGroup) {
                                                        %>
                                                        <li class="enabled"><a href="?group=<%=idx%>"><%=idx+1%></a></li>
                                                        <%
                                                            } else {
                                                        %>
                                                            <!-- li class="disabled"><a href=""><%=idx+1%></a></li -->
                                                        <%
                                                            }
                                                        }    
                                                        %>
                                                        <li class=<%=lastActive%>><a href="?group=<%=lastGroup%>">»</a></li>
                                                    </ul>
						</div>
					</div>
				</div>
			</div>
		</div> <!-- /container -->
		<div id="push"></div>
	</div>
	<footer id="footer">
		<div class="container">
			<strong>BIREME Social CheckLinks - V0.1 - 2013</strong><br/>
			Source code <a href="https://github.com/bireme/">https://github.com/bireme/</a>
		</div>
	</footer>
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