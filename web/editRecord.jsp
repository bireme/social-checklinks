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
    final ResourceBundle messages = Tools.getMessages(lang);
    
    if (session.getAttribute("user") == null) {
        response.sendRedirect("index.jsp?lang=" + lang
                                + "&errMsg=" + messages.getString("timed_out"));
        return;
    }
    
    // status = -1 (new), 1 (broken) and 0 (not broken) 
    final int status = Integer.parseInt(request.getParameter("status"));
    final boolean isNew = (status == -1);
    final boolean isBroken = (status == 1);
    final String user = (String)session.getAttribute("user");
    final String id = (String)request.getParameter("id");
    final String id2 = id.substring(0, id.lastIndexOf('_'));
    final String url = (String)request.getParameter("url");
    final String brokenUrl = url.replace("<<amp;>>", "&");
    final String furl = (String)request.getParameter("furl");
    final String fixedUrl = furl.replace("<<amp;>>", "&");
    final String lang2 = lang.equals("null") ? "en" : lang.equals("fr") 
                                                    ? "en" :lang;    
%>

<!-- ================================================== -->

<!doctype html>
<html>
<head>
	<title><%=messages.getString("bireme_social_checklinks")%></title>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1" />
	<meta name="HandheldFriendly" content="true">
	<link href="css/bootstrap.css" rel="stylesheet">
	<link href="css/bootstrap-responsive.css" rel="stylesheet">
	<link href="css/styles.css" rel="stylesheet">
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
            
        function replaceAll(string, token, newtoken) {
            while (string.indexOf(token) !== -1) {
 		string = string.replace(token, newtoken);
            }
            return string;
        }           
        function callUrl(id, url, lang) {
           var nurl = url.replace(/\&/g, '<<amp;>>');
           var nurl2 = document.getElementById('input-1').value
                                                    .replace(/\&/g, '<<amp;>>');
                                            
           postToUrl('CheckOneLinkServlet', {id: id, url: nurl, furl: nurl2, 
                                                                   lang: lang});                                                        
           //var turl = 'CheckOneLinkServlet?id=' + id + '&url=' + nurl + '&furl=' 
           //                                           + nurl2 + '&lang=' + lang;
           //alert(turl);
           //window.open(turl,"_self");
        }
        function callUrl2(id, url, lang) {
           var nurl = url.replace(/\&/g, '<<amp;>>');
           var nurl2 = document.getElementById('input-1').value
                                                    .replace(/\&/g, '<<amp;>>');
           postToUrl('CheckManyLinksServlet', {id: id, url: nurl, furl: nurl2, 
                                                                   lang: lang});                                                        
                                            
          // var turl = 'CheckManyLinksServlet?id=' + id + '&url=' + nurl 
          //                                 + '&furl=' + nurl2 + '&lang=' + lang;
           //alert(turl);
          // window.open(turl,"_self");
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
							<li class="active"><a href="javascript:postToUrl('list.jsp', {group:'0', lang:'<%=lang%>',cc:'All'});"><%=messages.getString("home")%></a></li>
							<li><a href="http://wiki.bireme.org/pt/index.php/Social_Check_Links" target="_blank"><%=messages.getString("about")%></a></li>
							<li><a href="http://reddes.bvsalud.org/" target="_blank"><%=messages.getString("contact")%></a></li>
						</ul>
						<ul class="nav pull-right">
                                                    <li class="dropdown">
                                                            <a href="http://reddes.bvsalud.org/" class="dropdown-toggle" data-toggle="dropdown"><%=messages.getString("language")%> <b class="caret"></b></a>
                                                            <ul class="dropdown-menu">                                                                
								<li><a href="javascript:postToUrl('editRecord.jsp', {id:'<%=id%>',url:'<%=url%>',furl:'<%=furl%>',status:'<%=status%>',lang:'en'});">English</a></li>
                                                                <li><a href="javascript:postToUrl('editRecord.jsp', {id:'<%=id%>',url:'<%=url%>',furl:'<%=furl%>',status:'<%=status%>',lang:'pt'});">Português</a></li>
                                                                <li><a href="javascript:postToUrl('editRecord.jsp', {id:'<%=id%>',url:'<%=url%>',furl:'<%=furl%>',status:'<%=status%>',lang:'es'});">Español</a></li>
                                                                <!--li><a href="javascript:postToUrl('editRecord.jsp', {id:'<%=id%>',url:'<%=url%>',furl:'<%=furl%>',status:'<%=status%>',lang:'fr'});">Francés</a></li-->
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
			<h1><%=messages.getString("edit_url")%></h1>
			<div class="urlEditor">
				<div class="urlLine">
                                    <div class="seg-q">
                                        <div class="URL-tested"><%=brokenUrl%></div>
					<input type="url" id="input-1" class="span8" value="<%=fixedUrl%>" <%= ((!isNew) && (!isBroken)) ? "DISABLED" : ""%>/> &nbsp;
                                        <!--a href="http://pesquisa.bvsalud.org/regional/?lang=<%=lang2%>&q=++%28id%3A%28LIL-<%=id2%>%29%29" title="<%=messages.getString("see_bibliographic_record")%>" target="_blank" class="btn btn-mini btn-primary"><i class="icon-eye-open icon-white"></i> <%=messages.getString("see")%></a-->
                                        <a href="http://pesquisa.bvsalud.org/regional/?lang=<%=lang2%>&q=++(id:(LIL-<%=id2%>))" title="<%=messages.getString("see_bibliographic_record")%>" target="_blank" class="btn btn-mini btn-primary"><i class="icon-eye-open icon-white"></i> <%=messages.getString("see")%></a>
                                        <%
                                            if (!isNew) {
                                                if (isBroken) {
                                         %>
                                                    <div class="alert alert-danger fade in">
                                                            <button data-dismiss="alert" class="close" type="button">×</button>
                                                            <strong><%=messages.getString("bad_news")%></strong> <%=messages.getString("url_is_broken")%>
                                                    </div>
                                         <%
                                                } else {
                                         %>
                                                    <div class="alert alert-success fade in">
                                                            <button data-dismiss="alert" class="close" type="button">×</button>
                                                            <strong><%=messages.getString("url_fixed")%></strong> <%=messages.getString("press_save")%>
                                                    </div>
                                         <%
                                                }
                                            }
                                         %>   

                                        <div class="ctrl">
                                            <%
                                                if (isNew || isBroken) {
                                             %>
                                                    <a href="javascript:callUrl('<%=id%>','<%=brokenUrl%>','<%=lang%>');" class="btn btn-mini" title="Test your changes"><%=messages.getString("test")%></a>
                                                    <a class="btn btn-mini disabled" title=<%=messages.getString("save_your_changes")%>><%=messages.getString("save")%></a>
                                            <%                                                            
                                                } else {
                                             %>       
                                                    <a class="btn btn-mini disabled" title="<%=messages.getString("test_your_changes")%>"><%=messages.getString("test")%></a>
                                                    <a href="javascript:callUrl2('<%=id%>','<%=brokenUrl%>','<%=lang%>');" class="btn btn-mini enabled"><%=messages.getString("save")%></a>
                                            <%       
                                                }
                                             %>                                                           							

					</div>
                                    </div>          
				</div>				
			</div>
		</div> <!-- /container -->
		<div id="push"></div>
	</div>

	<footer id="footer" class="footer">
		<div class="container">
			<strong><%=messages.getString("bireme_social_checklinks")%> - <%= BrokenLinks.VERSION %> - 2013</strong><br/>
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
