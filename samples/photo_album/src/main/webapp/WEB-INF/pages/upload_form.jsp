<!doctype html>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@taglib uri="http://www.springframework.org/tags/form" prefix="form" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%@include file="pre.jsp"%>
<!-- A standard form for uploading images to your server -->
<div id='backend_upload'>
    <h1>New Photo</h1>
    <h2>Image file is uploaded through the server</h2>
    <form:form method="post" action="upload" commandName="photoUpload" enctype="multipart/form-data">
        <div class="form_line">
            <form:label path="title">Title:</form:label>
            <div class="form_controls">
                <form:input path="title"/>
                <form:errors path="title" extraClasses="error" />
            </div>
        </div>

        <c:if test="${empty photoUpload.publicId}">
            <div class="form_line">
                <label for="file">Image:</label>
                <div class="form_controls">
                    <input type="file" name="file" id="file"/>
                </div>
            </div>
        </c:if>
        <c:if test="${!empty photoUpload.publicId}">
            <c:if test="${photoUpload.isImage}">
                <div class="form_line">
                    <label>Image:</label>
                    <div class="form_controls">
                        <img src="${photoUpload.thumbnailUrl}"/>
                    </div>
                </div>
            </c:if>
            <c:if test="${!photoUpload.isImage}">
                <div class="form_line">
                    <label>Raw file:</label>
                    <div class="form_controls">
                        <a href="<cl:url storedSrc="${photoUpload}"/>">${photoUpload.publicId}</a>
                    </div>
                </div>
            </c:if>
        </c:if>
        <div class="form_line">
            <div class="form_controls">
                <input type="submit" value="Submit Photo"/>
            </div>
        </div>
        <form:hidden path="preloadedFile"/>
        <form:errors path="signature" extraClasses="error" />
    </form:form>

</div>

<a href="<c:url value="/"/>" class="back_link">Back to list</a>
<%@include file="post.jsp"%>

