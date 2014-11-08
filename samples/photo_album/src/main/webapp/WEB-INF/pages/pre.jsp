<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://cloudinary.com/jsp/taglib" prefix="cl" %>
<html>
<head>
    <title>Photo Album</title>
    <link type="text/css" rel="stylesheet" media="all" href="<c:url value="/stylesheets/application.css"/>">
    <link rel="shortcut icon"
          href="<cl:url src="http://cloudinary.com/favicon.png" type="fetch" effect="sepia"/>" />
    <script src="http://code.jquery.com/jquery-1.10.1.min.js"></script>
</head>
<body>

<div id="logo">
    <!-- This will render the image fetched from a remote HTTP URL using Cloudinary -->
    <cl:image src="http://cloudinary.com/images/logo.png" type="fetch" secure="true" signed="true" />
</div>

<div class="content">