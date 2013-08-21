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
<%@page import="java.util.*,com.mongodb.DBCollection,br.bireme.scl.*,br.bireme.scl.MongoOperations" %>

<% 
    final String user = (String)session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("index.html");
        return;
    }
    final ServletContext context = getServletContext();
    final DBCollection coll = (DBCollection)context.getAttribute("collection");
    final String centerId = (String)request.getSession().getAttribute("centerId");    
    final int group = Integer.parseInt(request.getParameter("group"));
    final int groupSize = 15;
    final List<IdUrl> lst = MongoOperations.getCenterUrls(coll, centerId, 
                                    (group * groupSize) + 1, groupSize);
    final int maxUrls = MongoOperations.getCenterUrlsNum(coll, centerId);
    final int lastGroup = (maxUrls / groupSize);
    final int initGroup = (group <= 1) ? 0 : (group >= lastGroup - 2) 
                                                ? (lastGroup - 4) : (group - 2);
    final int from = (group * groupSize);
%>

<!-- ================================================== -->

<!doctype html>
<html>
<head>
	<title>BIREME Social Checklinks</title>
	<meta charset="utf-8"/>
	<meta name="viewport" content="width=device-width, initial-scale=1" />
	<meta name="HandheldFriendly" content="true"/>
	<link href="css/bootstrap.css" rel="stylesheet"/>
	<link href="css/bootstrap-responsive.css" rel="stylesheet"/>
	<link href="css/styles.css" rel="stylesheet"/>
	<style type="text/css">
		html, body {
			height: 100%; /* The html and body elements cannot have any padding or margin. */
			padding-top: 30px;
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

								<a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="icon-user icon-white"></i> <%=user %> <b class="caret"></b></a>
								<ul class="dropdown-menu">
									<li><a href="index.html"><i class="icon-off"></i> Logout</a></li>
								</ul>
							</li>
						</ul>
					</div><!--/.nav-collapse -->
				</div>
			</div>
		</div>

		<div class="container">
			<h1>Broken Links</h1>
			<p>The list is composed of broken links.</p>
			
			<table class="table table-condensed">
				<thead>
					<tr>
						<th>#</th>
						<th>URL</th>
						<th>Actions</th>
					</tr>
				</thead>
				<tbody>
                                    <%
                                        int cur = from + 1;
                                        for (IdUrl iu : lst) {
                                            final String nurl = iu.url.replace("&","<<amp;>>");
                                    %>                                                                        
					<tr>
                                                <td><%=cur%></td>
						<td><%=iu.url%></td>
						<td><a href="editRecord.jsp?id=<%=iu.id%>&url=<%=nurl%>&furl=<%=nurl%>&status=-1" class="btn btn-mini btn-primary"><i class="icon-pencil icon-white"></i> Edit</a></td>
					</tr>
                                    <%
                                            cur++;
                                        }
                                    %>
				</tbody>
			</table>
			<div class="pagination pagination-centered">
				<ul>
					<li class="enabled"><a href="?group=0">«</a></li>
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
					<li class="enabled"><a href="?group=<%=lastGroup%>">»</a></li>
				</ul>
			</div>
		</div> <!-- /container -->
		<div id="push"></div>
	</div>
	<footer id="footer" class="footer">
		<div class="container">
			<strong>BIREME Social CheckLinks - V0.1 - 2013</strong><br/>
			Source code <a href="https://github.com/bireme/">https://github.com/bireme/social-checklinks</a>
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