<html>
<head>
    <title>${applicationName}: Login</title>
    <link href="/css/base.css" type="text/css" rel="stylesheet" />
    <link href="/css/bootstrap.min.css" type="text/css" rel="stylesheet" />
</head>

<body onload='document.f.username.focus();' class="container">
<div class="center">
    <h1>${applicationName}</h1>
</div>
<div class="center">

    <h3>Login with Username and Password</h3>

    <form name='f' action='/login' method='POST'>

        <table>
            <tr>
                <td>User:</td>
                <td><input type='text' name='username' value=''></td>
            </tr>
            <tr>
                <td>Password:</td>
                <td><input type='password' name='password'/></td>
            </tr>
            <tr>
                <td colspan='2'><input name="submit" type="submit" value="Login"/></td>
            </tr>

            <input name="_csrf" type="hidden" value="b916cd47-9533-4daa-a4ca-f5c9940003ca"/>

        </table>
    </form>
</div>
</body>
</html>
