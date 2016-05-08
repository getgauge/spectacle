package main

import (
	"fmt"
	"io/ioutil"
	"os"
	"path/filepath"
	"strings"

	"github.com/getgauge/M2H/constant"
	"github.com/getgauge/M2H/json"
	"github.com/getgauge/M2H/processor"
	"github.com/getgauge/M2H/spec"
	"github.com/golang-commonmark/markdown"
)

const (
	html             = ".html"
	out              = "html"
	localhost        = "localhost"
	gaugeSpecsDir    = "GAUGE_SPEC_DIRS"
	gaugeApiPort     = "GAUGE_API_PORT"
	space            = " "
	indexFile        = "index.html"
	quotes           = `""''`
	linkTemplate     = "<li class=\"nav\"><a href=\"%s\">%s</a></li>"
	gaugeProjectRoot = "GAUGE_PROJECT_ROOT"
	htmlTemplate     = `%s%s<ul id="navigation"><center>%s%s%s</center>%s`
	styleCSS         = "style.css"
)

func init() {
	outDir = filepath.Join(os.Getenv(gaugeProjectRoot), out)
}

var outDir string

func main() {
	var files []string
	for _, arg := range strings.Split(os.Getenv(gaugeSpecsDir), space) {
		files = append(files, spec.GetFiles(arg)...)
	}
	p, _ := processor.NewMessageProcessor(localhost, os.Getenv(gaugeApiPort))
	msg, _ := p.GetSpecs()
	p.Connection.Close()
	os.Mkdir(outDir, 0755)
	json.WriteJS(msg.AllSpecsResponse.Specs, outDir, html)
	writeCSS()
	var lastSpec string
	for i, file := range files {
		convertFile(file, files, i, &lastSpec)
	}
	createIndex()
}

func convertFile(file string, files []string, index int, lastSpec *string) {
	md := markdown.New(markdown.XHTMLOutput(true), markdown.Nofollow(true), markdown.Quotes(quotes))
	input, _ := ioutil.ReadFile(file)
	output := md.RenderToString(input)
	var next, previous string
	if index+1 < len(files) {
		next = fmt.Sprintf(linkTemplate, strings.TrimSuffix(filepath.Base(files[index+1]),
			filepath.Ext(files[index+1]))+html, ">")
	}
	if index != 0 {
		previous = fmt.Sprintf(linkTemplate, *lastSpec, "<")
	}
	output = fmt.Sprintf(htmlTemplate, constant.IncludeCSS, output, previous, constant.IncludeIndex, next, constant.JS)
	name := strings.TrimSuffix(filepath.Base(file), filepath.Ext(file))
	f, _ := os.Create(outDir + string(filepath.Separator) + name + html)
	f.Write([]byte(output))
	f.Close()
	*lastSpec = name + html
}

func createIndex() {
	f, _ := os.Create(outDir + string(filepath.Separator) + indexFile)
	input := constant.IncludeCSS + constant.DataFile + constant.IndexContent + constant.IndexJS
	f.WriteString(input)
	f.Close()
}

func writeCSS() {
	f, _ := os.Create(outDir + string(filepath.Separator) + styleCSS)
	f.Write([]byte(constant.CSS))
	f.Close()
}
