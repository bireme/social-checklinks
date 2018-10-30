<%--
  =========================================================================

    social-checklinks © Pan American Health Organization, 2018.
    See License at: https://github.com/bireme/social-checklinks/blob/master/LICENSE.txt

  ==========================================================================
--%>

<%@page language="java"%>
<%@page session="true" %>
<%@page import="java.util.*,br.bireme.scl.*" %>
<%@page contentType="text/html;charset=UTF-8"%>

<%
    request.setCharacterEncoding("UTF-8");

    String lang = (String)request.getParameter("lang");
    if (lang == null) {
        lang = "en";
        final String requestLang = request.getHeader("Accept-Language");
        if (requestLang != null) {
            if (requestLang.startsWith("es")) {
                lang = "es";
            } else if (requestLang.startsWith("pt")) {
                lang = "pt";
            } else if (requestLang.startsWith("fr")) {
                lang = "fr";
            }
        }
    }
    final String errMsg = (String)request.getParameter("errMsg");
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
                <input style="min-height: 37px;" type="email" class="input-block-level" placeholder="<%=messages.getString("email_address")%>" name="email"/>
                <input type="password" class="input-block-level" placeholder="<%=messages.getString("password")%>" name="password"/>
                <button class="btn btn-large btn-primary" type="submit"><%=messages.getString("sign_in")%></button>
            </form>
            <%
            if (errMsg != null) {
            %>
                <div class="alert alert-danger fade in">
                    <strong><%=messages.getString("bad_news")%></strong> <%=errMsg%>
                </div>
            <%
            }
            %>
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
