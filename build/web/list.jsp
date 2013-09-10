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
<%@page contentType="text/html;charset=UTF-8"%>

<% 
    final String lang = (String)request.getParameter("lang");
    final String user = (String)session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("index.jsp?lang=" + lang);
        return;
    }
    final String collCenterFilter = 
                               (String)session.getAttribute("collFilterCenter");
    final ServletContext context = getServletContext();
    final DBCollection coll = (DBCollection)context.getAttribute("collection");
    final Set<String> centerIds = (Set<String>)request.getSession()
                                                     .getAttribute("centerIds");         
    final int group = Integer.parseInt(request.getParameter("group"));
    final int groupSize = 17;
    final List<IdUrl> lst = MongoOperations.getCenterUrls(coll, centerIds, 
                          collCenterFilter, (group * groupSize) + 1, groupSize);
    final int maxUrls = MongoOperations.getCentersUrlsNum(coll, centerIds,             
                                                              collCenterFilter);
    final int mod = (maxUrls % groupSize);
    int lastGroup = (maxUrls / groupSize);
    lastGroup = ((maxUrls > 0) && (mod == 0)) ? lastGroup - 1 : lastGroup;
    final int initGroup = (group <= 1) ? 0 : (group >= lastGroup - 2) 
                                                ? (lastGroup - 4) : (group - 2);
    final int from = (group * groupSize);        
    final boolean showCenters = (centerIds.size() > 1);
    final ResourceBundle messages = Tools.getMessages(lang);
%>

<!-- ================================================== -->

<!doctype html>
<html>
<head>
	<title><%=messages.getString("bireme_social_checklinks")%></title>
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
        
        <script LANGUAGE="JavaScript" TYPE="text/javascript">
            
        function postToUrl(path, params) {
            var form = document.createElement("form");
            form.setAttribute("method", "post");
            form.setAttribute("action", path);

            for(var key in params) {
                if (params.hasOwnProperty(key)) {
                    var hiddenField = document.createElement("input");
                    hiddenField.setAttribute("type", "hidden");
                    hiddenField.setAttribute("name", key);
                    hiddenField.setAttribute("value", params[key]);

                    form.appendChild(hiddenField);
                 }
            }

            document.body.appendChild(form);
            form.submit();
        }
        
        </script>
        
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
					<a class="brand" href="#"><%=messages.getString("bireme_social_checklinks")%></a>
					<div class="nav-collapse collapse">
						<ul class="nav">
							<li class="active"><a href="javascript:postToUrl('list.jsp', {group:'0',lang:'<%=lang%>});"><%=messages.getString("home")%></a></li>
							<li><a href="http://wiki.bireme.org/pt/index.php/Social_Check_Links" target="_blank"><%=messages.getString("about")%></a></li>
							<li><a href="http://reddes.bvsalud.org/" target="_blank"><%=messages.getString("contact")%></a></li>
						</ul>
						<ul class="nav pull-right">
                                                        <li class="dropdown">
                                                            <a href="http://reddes.bvsalud.org/" class="dropdown-toggle" data-toggle="dropdown"><%=messages.getString("language")%> <b class="caret"></b></a>
                                                            <ul class="dropdown-menu">
                                                                <li><a href="javascript:postToUrl('list.jsp', {group:'<%=group%>',lang:'en'});">English</a></li>
                                                                <li><a href="javascript:postToUrl('list.jsp', {group:'<%=group%>',lang:'pt'});">Português</a></li>
                                                                <li><a href="javascript:postToUrl('list.jsp', {group:'<%=group%>',lang:'es'});">Español</a></li>
                                                                <!--li><a href="javascript:postToUrl('list.jsp', {group:'<%=group%>',lang:'fr'});">Francés</a></li-->
                                                            </ul>
                                                        </li>
							<li class="dropdown">
								<a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="icon-user icon-white"></i> <%=user %> <b class="caret"></b></a>
								<ul class="dropdown-menu">
									<li><a href="javascript:postToUrl('index.jsp', {lang:'<%=lang%>'});"><i class="icon-off"></i> <%=messages.getString("logout")%></a></li>
								</ul>
							</li>
						</ul>
					</div><!--/.nav-collapse -->
				</div>
			</div>
		</div>

		<div class="container">
			<h1><%=messages.getString("broken_links")%></h1>
			<p><%=messages.getString("the_list")%></p>
                        
                        <% if (showCenters) { %>
                        <div class="btn-group pull-right">
                          <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">
                            <%=messages.getString("show")%> <%=(collCenterFilter == null) ? messages.getString("all") : collCenterFilter%>
                            <span class="caret"></span>
                          </button>
                          <ul class="dropdown-menu">
                            <li><a href="javascript:postToUrl('CenterFilterServlet', {group:'<%=group%>',lang:'<%=lang%>'});">All</a></li>
                           <%
                             for (String id : centerIds)  {                               
                           %>    
                            <li><a href="javascript:postToUrl('CenterFilterServlet', {group:'<%=group%>',lang:'<%=lang%>',collFilterCenter:'<%=id%>'});"><%=id%></a></li>
                           <%
                             } 
                           %>
                          </ul>
                        </div>			       
                        <%}%>
                          
			<table class="table table-condensed">
				<thead>
					<tr>
						<th>#</th>
						<th>URL</th>
                                                <%if (showCenters) {%><th>CCs</th><%}%>
						<th><%=messages.getString("action")%></th>
					</tr>
				</thead>
				<tbody>
                                    <%
                                        int cur = from + 1;
                                        for (IdUrl iu : lst) {
                                            final String nurl = iu.url.replace("&","<<amp;>>");
                                            boolean first = true;
                                    %>
                                         <tr>                                    
                                                <td><%=cur%></td>
						<td><%=iu.url.trim()%></td>  
                                    <%
                                            if (showCenters) {
                                    %>
                                                 <td>
                                    <%
                                                for (String cc : iu.ccs) {
                                                    if (centerIds.contains(cc)) {
                                                        if (first) {
                                                            first = false;
                                                        } else {
                                                            out.print(", ");
                                                        }
                                                        out.print(cc);
                                                    }
                                                }
                                    %>             
                                                </td>
                                    <%            
                                            }
                                    %>

						<td><a href="javascript:postToUrl('editRecord.jsp', {id:'<%=iu.id%>',url:'<%=nurl%>',furl:'<%=nurl%>',status:'-1',lang:'<%=lang%>'});" title="Edit broken url" class="btn btn-mini btn-primary"><i class="icon-pencil icon-white"></i> <%=messages.getString("edit")%></a></td>
					</tr>
                                    <%
                                            cur++;
                                        }
                                    %>
				</tbody>
			</table>
			<div class="pagination pagination-centered">
				<ul>
					<!--li class="enabled"><a href="?group=0">«</a></li-->
                                        <li class="enabled"><a href="javascript:postToUrl('list.jsp', {group:'0',lang:'<%=lang%>'});">&laquo;</a></li>
                                        <%                                        
                                        for (int idx = initGroup; idx < initGroup+5; idx++) {
                                            if (idx == group) {
                                        %>
                                            <li class="active"><a><%=idx+1%></a></li>
                                        <%
                                            } else if (idx <= lastGroup) {
                                        %>
                                            <li class="enabled"><a href="javascript:postToUrl('list.jsp', {group:'<%=idx%>',lang:'<%=lang%>'});"><%=idx+1%></a></li>
                                        <%
                                            }
                                        }    
                                        %>
                                        <li class="enabled"><a href="javascript:postToUrl('list.jsp', {group:'<%=lastGroup%>',lang:'<%=lang%>'});">&raquo;</a></li>
				</ul>
			</div>
		</div> <!-- /container -->
		<div id="push"></div>
	</div>
	<footer id="footer" class="footer">
		<div class="container">
			<strong>BIREME Social CheckLinks - <%= BrokenLinks.VERSION %> - 2013</strong><br/>
			<%=messages.getString("source_code")%>: <a href="https://github.com/bireme/">https://github.com/bireme/social-checklinks</a>
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