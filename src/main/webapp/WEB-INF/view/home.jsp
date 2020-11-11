<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%--
  Created by IntelliJ IDEA.
  User: SANS_CDM_DB_2
  Date: 2020-07-29
  Time: 오후 3:02
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="en">

<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta name="description" content="">
    <meta name="author" content="">

    <title>SB Admin - Login</title>

    <!-- Custom fonts for this template-->
    <link href="${pageContext.request.contextPath}/vendor/fontawesome-free/css/all.min.css" rel="stylesheet" type="text/css">

    <!-- Custom styles for this template-->
    <link href="${pageContext.request.contextPath}/css/sb-admin.css" rel="stylesheet">

</head>

<body class="bg-dark">

<div class="container">
    <div class="card card-login mx-auto mt-5">
        <div class="card-header">Login</div>
        <div class="card-body">
<%--            <form>--%>
            <form:form modelAttribute="custodianDto" action="${pageContext.request.contextPath}/sign/in" method="post">
                <div class="form-group">
                    <div class="form-label-group">
<%--                        <input type="email" id="inputEmail" class="form-control" placeholder="Email address" required="required" autofocus="autofocus">--%>
<%--                        <label for="inputEmail">Email address</label>--%>
                        <form:input type="text" path="name" id="inputId" class="form-control" placeholder="ID" required="required" autofocus="autofocus"></form:input>
                        <form:label path="name">ID</form:label>
                    </div>
                </div>
                <div class="form-group">
                    <div class="form-label-group">
<%--                        <input type="password" id="inputPassword" class="form-control" placeholder="Password" required="required">--%>
<%--                        <label for="inputPassword">Password</label>--%>
                        <form:input type="password" path="password" id="inputPassword" class="form-control" placeholder="Password" required="required"></form:input>
                        <form:label path="password">Password</form:label>
                    </div>
                </div>
                <div class="form-group">
                    <div class="checkbox">
                        <label>
                            <input type="checkbox" value="remember-me">
                            Remember Password
                        </label>
                    </div>
                </div>
<%--                <a class="btn btn-primary btn-block" href="index.html">SignIn</a>--%>
                <input type="submit" class="btn btn-primary btn-block" value="SignIn">
            </form:form>
<%--            </form>--%>
            <div class="text-center">
                <a class="d-block small mt-3" href="register.html">Register an Account</a>
                <a class="d-block small" href="forgot-password.html">Forgot Password?</a>
            </div>
        </div>
    </div>
</div>

<!-- Bootstrap core JavaScript-->
<script src="${pageContext.request.contextPath}/vendor/jquery/jquery.min.js"></script>
<script src="${pageContext.request.contextPath}/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>

<!-- Core plugin JavaScript-->
<script src="${pageContext.request.contextPath}/vendor/jquery-easing/jquery.easing.min.js"></script>

</body>

</html>
