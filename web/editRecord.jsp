<%@page language="java"%>
<%@page session="true" %>
<%@page import="java.util.Set,com.mongodb.DBCollection,br.bireme.scl.*,br.bireme.scl.MongoOperations" %>

<% 
    if (session.getAttribute("user") == null) {
        response.sendRedirect("index.html");
    }
    
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
%>

<!-- ================================================== -->

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
        
        
        <script LANGUAGE="JavaScript" TYPE="text/javascript">            
        function replaceAll(string, token, newtoken) {
            while (string.indexOf(token) !== -1) {
 		string = string.replace(token, newtoken);
            }
            return string;
        }           
        function callUrl(id, url) {
           var nurl = url.replace(/\&/g, '<<amp;>>');
           var nurl2 = document.getElementById('input-1').value
                                                    .replace(/\&/g, '<<amp;>>');
           var turl = 'CheckOneLinkServlet?id=' + id + '&url=' + nurl + '&furl=' 
                                                                        + nurl2;
           //alert(turl);
           window.open(turl,"_self");
        }
        function callUrl2(id, url) {
           var nurl = url.replace(/\&/g, '<<amp;>>');
           var nurl2 = document.getElementById('input-1').value
                                                    .replace(/\&/g, '<<amp;>>');
           var turl = 'CheckManyLinksServlet?id=' + id + '&url=' + nurl 
                                                             + '&furl=' + nurl2;
           //alert(turl);
           window.open(turl,"_self");
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
					<a class="brand" href="#">BIREME Social CheckLinks</a>
					<div class="nav-collapse collapse">
						<ul class="nav">
							<li class="active"><a href="list.html">Home</a></li>
							<li><a href="#about">About</a></li>
							<li><a href="http://reddes.bvsalud.org/">Contact</a></li>
						</ul>
						<ul class="nav pull-right">
							<li class="dropdown">

								<a href="#" class="dropdown-toggle" data-toggle="dropdown"><i class="icon-user icon-white"></i> <%=user %> <b class="caret"></b></a>
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
		    <ul class="breadcrumb">
			    <li><a href="list.html">Home</a> <span class="divider">/</span></li> 
			    <li class="active">Edit</li>
		    </ul>
			<h1>Edit URL</h1>
			<div class="urlEditor">
				<div class="urlLine">
                                    <div class="seg-q">
                                        <div class="URL-tested"><%=brokenUrl%></div>
					<input type="url" id="input-1" class="span8" value="<%=fixedUrl%>" /> &nbsp;<a href="http://pesquisa.bvsalud.org/regional/?lang=en&q=++%28id%3A%28LIL-<%=id2%>%29%29" target="_blank">see source</a>
                                        <%
                                            if (!isNew) {
                                                if (isBroken) {
                                         %>
                                                    <div class="alert alert-danger fade in">
                                                            <button data-dismiss="alert" class="close" type="button">×</button>
                                                            <strong>Bad news!</strong> The url is broken. Try to edit and save again.
                                                    </div>
                                         <%
                                                } else {
                                         %>
                                                    <div class="alert alert-success fade in">
                                                            <button data-dismiss="alert" class="close" type="button">×</button>
                                                            <strong>URL fixed!</strong> Press save to store the fixed url.
                                                    </div>
                                         <%
                                                }
                                            }
                                         %>   

                                        <div class="ctrl">
                                            <%
                                                if (isNew || isBroken) {
                                             %>
                                                    <a href="javascript:callUrl('<%=id%>','<%=brokenUrl%>');" class="btn btn-mini" title="Test your changes">TEST</a>
                                                    <a class="btn btn-mini disabled">SAVE</a>
                                            <%                                                            
                                                } else {
                                             %>       
                                                    <a class="btn btn-mini disabled" title="Test your changes">TEST</a>
                                                    <a href="javascript:callUrl2('<%=id%>','<%=brokenUrl%>');" class="btn btn-mini enabled">SAVE</a>
                                            <%       
                                                }
                                             %>                                                           							

					</div>
				</div>				
			</div>
		</div> <!-- /container -->
		<div id="push"></div>
	</div>
	<footer id="footer">
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
