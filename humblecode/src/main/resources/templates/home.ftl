<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>${applicationName}: Learn how to code</title>
    <link href="/css/base.css" type="text/css" rel="stylesheet" />
    <link href="/css/bootstrap.min.css" type="text/css" rel="stylesheet" />
    <link href="/css/prettify.css" type="text/css" rel="stylesheet" />
    <link href="/css/asciidoctor.css" type="text/css" rel="stylesheet" />

    <script src="/js/jquery-1.11.1.min.js" type="application/javascript"></script>
    <script src="/js/bootstrap.min.js" type="application/javascript"></script>
    <script src="/js/prettify.js" type="application/javascript"></script>

    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="/js/html5shiv.min.js"></script>
    <![endif]-->
    <script src="/js/main.js" type="application/javascript"></script>

    <style>
        .categories,.courses { font-size: 2em; }
    </style>

</head>
<body onload="">
 <div class="container">

     <#if name == ""><a href="#" onclick="signUp(); return false">Sign up today!</a></#if>

     <div class="navbar">Hello ${name}!</div>

    <div class="page-header">
        <h1>Welcome to ${applicationName}!</h1>
    </div>

    <article id="content" class="jumbotron center"></article>

    <script type="application/javascript">
        jQuery(document).ready(HC.loadCourses);
    </script>

     <form method="post" action="/api/course">
         Name: <input id="name" name="name" type="text">
         Price: <input id="price" name="price" type="number">
         <button onclick="postCourse()">Save Course</button>
     </form>

 </div>
</body>
</html>