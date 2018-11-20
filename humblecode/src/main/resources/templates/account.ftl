<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>${applicationName}: Learn how to code</title>
    <link href="/css/base.css" type="text/css" rel="stylesheet"/>
    <link href="/css/bootstrap.min.css" type="text/css" rel="stylesheet"/>
    <link href="/css/prettify.css" type="text/css" rel="stylesheet"/>
    <link href="/css/asciidoctor.css" type="text/css" rel="stylesheet"/>

    <script src="/js/jquery-1.11.1.min.js" type="application/javascript"></script>
    <script src="/js/bootstrap.min.js" type="application/javascript"></script>
    <script src="/js/prettify.js" type="application/javascript"></script>

    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="js/html5shiv.min.js"></script>
    <![endif]-->

    <style>
        .categories {
            font-size: 2em;
        }
    </style>

</head>
<body onload="">
<div class="container">

    <div class="navbar">Hello ${username}!</div>

    <div class="page-header">
        <h1>Welcome to ${applicationName}!</h1>
    </div>

    <article id="courses" class="jumbotron center"></article>

    <article id="pitch" class="jumbotron center">
        <p class="col-lg-3">Upgrade now to get access to all courses.</p>
        <p class="col-lg-3">Includes: Courses, Tests, Certificates upon completion.</p>
    </article>

    <form action="/pay" method="POST">
        <script
                src="https://checkout.stripe.com/checkout.js"
                class="stripe-button"
                data-key="pk_test_6pRNASCoBOKtIshFeQd4XMUh"
                data-image="/images/Zamia.gif"
                data-name="Humble Code"
                data-description="2 widgets ($20.00)"
                data-amount="2000">
        </script>
    </form>

</div>
</body>
</html>