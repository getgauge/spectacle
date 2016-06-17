package constant

var CSS string
var IndexJS string
var DataFile string
var IndexJSContent string
var IndexContent string
var IncludeIndex string
var IncludeCSS string

func init() {
	CSS = `
body {
	 font:13.34px helvetica,arial,freesans,clean,sans-serif;
	 color:black;
	 line-height:1.4em;
	 background-color: #F8F8F8;
	 padding: 0.7em;
}
a {
	text-decoration: inherit;
	font-style: inherit;
	color: inherit;
}
a:link, a:visited {
	border:none;
	text-decoration:underline;
	font-style:normal;
	color:black;
}
a:hover {
	text-decoration: none;
	color: #a3211f;
}
p {
	margin:1em 0;
	line-height:1.5em;
}
table {
	font-family: verdana,arial,sans-serif;
	font-size:11px;
	color:#333333;
	border-width: 1px;
	border-color: #666666;
	border-collapse: collapse;
}
table th {
	border-width: 1px;
	padding: 8px;
	border-style: solid;
	border-color: #666666;
	background-color: #dedede;
}
table td {
	border-width: 1px;
	padding: 8px;
	border-style: solid;
	border-color: #666666;
}
input[type=text],input[type=password],input[type=image],textarea {
	font:99% helvetica,arial,freesans,sans-serif;
}
select,option {
	padding:0 .25em;
}
optgroup {
	margin-top:.5em;
}
pre,code {
	font:12px Menlo, Monaco, "DejaVu Sans Mono", "Bitstream Vera Sans Mono",monospace;
}
pre {
	 margin:1em 0;
	 font-size:12px;
	 background-color:#eee;
	 border:1px solid #ddd;
	 padding:5px;
	 line-height:1.5em;
	 color:#444;
	 overflow:auto;
	 -webkit-box-shadow:rgba(0,0,0,0.07) 0 1px 2px inset;
	 -webkit-border-radius:3px;
	 -moz-border-radius:3px;border-radius:3px;
}
pre code {
	 padding:0;
	 font-size:12px;
	 background-color:#eee;
	 border:none;
}
code {
	 font-size:12px;
	 background-color:#f8f8ff;
	 color:#444;
	 padding:0 .2em;
	 border:1px solid #dedede;
}
img {
	border:0;
	max-width:100%;
}
abbr {
	border-bottom:none;
}
a {
	color:#4183c4;
	text-decoration:none;
}
a:hover {
	text-decoration:underline;
}
a code,a:link code,a:visited code {
	color:#4183c4;
}
h2,h3 {
	margin:1em 0;
}
h1,h2,h3,h4,h5,h6 {
	border:0;
}
h1 {
	font-size:170%;
	border-top:4px solid #aaa;
	padding-top:.5em;
	margin-top:1.5em;
}
h1:first-child {
	margin-top:0;
	padding-top:.25em;
	border-top:none;
}
h2 {
	font-size:150%;
	margin-top:1.5em;
	border-top:4px solid #e0e0e0;
	padding-top:.5em;
}
h3 {
	margin-top:1em;
}
hr {
	border:1px solid #ddd;
}
ul {
	margin:1em 0 1em 2em;
}
ol {
	margin:1em 0 1em 2em;
}
ul li,ol li {
	margin-top:.5em;
	margin-bottom:.5em;
}
ul ul,ul ol,ol ol,ol ul {
	margin-top:0;
	margin-bottom:0;
}
blockquote {
	margin:1em 0;
	border-left:5px solid #ddd;
	padding-left:.6em;
	color:#555;
}
dt {
	font-weight:bold;
	margin-left:1em;
}
dd {
	margin-left:2em;
	margin-bottom:1em;
}
sup {
   font-size: 0.83em;
   vertical-align: super;
   line-height: 0;
}
* {
	 -webkit-print-color-adjust: exact;
}
@media screen and (min-width: 914px) {
   body {
      width: 854px;
      margin:0 auto;
   }
}
@media print {
	 table, pre {
		  page-break-inside: avoid;
	 }
	 pre {
		  word-wrap: break-word;
	 }
}
ul#navigation {
	list-style-type: none;
	margin: 0;
	padding: .5em 0;
	border-top: 1px solid #666;
}

ul#navigation li a {
	display: block;
	width: 5em;
	color: #FFF;
	background-color: #666666;
	padding: .2em 0;
	text-align: center;
	text-decoration: none;
}

ul#navigation li a:hover {
	color: #FFF;
	background-color: #878686;
}
.tags {
	border: 1px solid #848484;
	-webkit-border-radius: 30px;
	-moz-border-radius: 30px;
	border-radius: 30px;
	outline:0;
	height:25px;
	width: 100%;
	padding-left:10px;
	padding-right:10px;
}
ul#navigation .nav {
	display: inline-block;
	margin: 5px;
}
`
	IndexJS = `<script src="index.js"></script>`

	IndexJSContent = `
var statsTemplate = '<p><table style="text-align:center;margin-left: auto;margin-right: auto;border-collapse: separate;">\
	<th style="border: none !important;">Specifications</th>\
	<th style="border: none !important;">Scenarios</th>\
	<tr>\
		<td style="border: none !important;">SPEC_NUMBER</td>\
		<td style="border: none !important;">SCENARIO_NUMBER</td>\
	</tr>\
</table></p>\
';
var populateIndex = function(specs) {
	if (Object.keys(specs).length == 0) {
		document.getElementsByClassName("specs")[0].innerHTML = "<p>No Specifications found that matches the given tag expression...<p>";
		return;
	}
	var text = "<ul>";
	var scenarioNumber = 0;
	specs.forEach(function(spec) {
		text += "<li><p><b><a href=\"" + spec["path"] + "\">" + spec["name"]  + "</a></b><ol>";
		spec.scenarios.forEach(function(scn) {
			text += "<li>" + scn["name"] + "</li>";
			scenarioNumber++;
		});
		text += "</ol></p></li>";
	});
	text += "</ul></div>";
	var stats = statsTemplate.replace("SPEC_NUMBER", specs.length).replace("SCENARIO_NUMBER", scenarioNumber);
	document.getElementsByClassName("specs")[0].innerHTML = stats + text;
}
function handle(e){
        if(e.keyCode === 13) {
        	if (document.getElementsByClassName("tags")[0].value.trim() === "")	{
        		populateIndex(specs);
        		return false;
        	}
   			populateIndex(filterSpecs(document.getElementsByClassName("tags")[0].value));
        }
        return false;
}
var filterSpecs = function(tagExp) {
	tags = getTagsWithoutOperators(tagExp).map(function(e) {
		return e.replace("!", "")
	});
	var newSpecs = [];
	specs.forEach(function(spec) {
		var scenarios = [];
		spec.scenarios.forEach(function(scn) {
			var newTagExp = tagExp;
			newTagExp = replace(newTagExp, scn.tags.filter(function(t) {
				return t !== "";
			}), "true");
			newTagExp = replace(newTagExp, tags, "false");
			if (eval(newTagExp)) scenarios.push(scn);
		});
		if (scenarios.length > 0) {
			spec.scenarios = scenarios;
			newSpecs.push(spec);
		}
	});
	return newSpecs;
}
var replace = function(tagExp, tags, replaceString) {
	var tagsWithOperators = getTagsWithOperators(tagExp);
	tags.forEach(function(t) {
		var index= tagsWithOperators.indexOf(t);
		if(index > -1)
			tagsWithOperators[index] = replaceString;
		index= tagsWithOperators.indexOf("!" + t);
		if(index > -1)
			tagsWithOperators[index] = "!" + replaceString;
	});
	return tagsWithOperators.join("");
}
var getTags = function(tagExp, regex) {
	return tagExp.split(regex).map(function(e) {
		return e.trim();
	});
}
var getTagsWithOperators = function(tagExp) {
	return getTags(tagExp, /(&|\|)/);
}
var getTagsWithoutOperators = function(tagExp) {
	return getTags(tagExp, /(?:&|\|)/);
}
populateIndex(specs);
`

	IndexContent = `<center>
<h1><u>%s</u></h1>
	<input type="text" class="tags" placeholder="Add tag Expression to filter specs/scenarios. Example: 'login & product | !customer'" onkeypress="handle(event)"></input>
</center>
<div class="specs"></div>
`
	DataFile = `<script src="data.js"></script>`

	IncludeIndex = "<li class=\"nav\"><a href=\"index.html\">=</a></li>"

	IncludeCSS = `<link rel="stylesheet" type="text/css" href="style.css">`
}
