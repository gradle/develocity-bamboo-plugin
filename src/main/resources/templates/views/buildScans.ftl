[#-- @ftlvariable name="" type="com.gradle.develocity.bamboo.BuildScansAction" --]
<html>
<head>
    <meta name="tab" content="Build scans"/>
    <meta name="decorator" content="result"/>
</head>

<body>

<h1>Build scans</h1>
<div class="section">
    [#if buildScans?has_content]
        <span>The following build scans have been published:</span>
        <ul>
            [#list buildScans as buildScan]
                <li>
                    <a target="_blank" href="${buildScan}">${buildScan}</a>
                </li>
            [/#list]
        </ul>
    [#else]
        <span>No build scans found for this build.</span>
    [/#if]
</div>

</body>
</html>
