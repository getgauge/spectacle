package main

import (
	"fmt"
	"io/ioutil"
	"os"
	"path/filepath"
	"strings"

	"github.com/getgauge/spectacle/constant"
	"github.com/getgauge/spectacle/gauge_messages"
	"github.com/getgauge/spectacle/json"
	"github.com/getgauge/spectacle/processor"
	"github.com/getgauge/spectacle/util"
	"github.com/golang-commonmark/markdown"
)

const (
	html             = ".html"
	out              = "html"
	docs             = "docs"
	localhost        = "localhost"
	gaugeSpecsDir    = "GAUGE_SPEC_DIRS"
	gaugeApiPort     = "GAUGE_API_PORT"
	space            = " "
	indexFile        = "index.html"
	quotes           = `""''`
	linkTemplate     = "<li class=\"nav\"><a href=\"%s\">%s</a></li>"
	gaugeProjectRoot = "GAUGE_PROJECT_ROOT"
	htmlTemplate     = `%s%s<ul id="navigation"><center>%s%s%s</center>`
	styleCSS         = "style.css"
)

func init() {
	projectRoot = os.Getenv(gaugeProjectRoot)
	docsDir = filepath.Join(projectRoot, docs)
	outDir = filepath.Join(projectRoot, docs, out)
}

var docsDir string
var outDir string
var projectRoot string

func main() {
	var files []string
	for _, arg := range strings.Split(os.Getenv(gaugeSpecsDir), space) {
		files = append(files, util.GetFiles(arg)...)
	}
	p, _ := processor.NewMessageProcessor(localhost, os.Getenv(gaugeApiPort))
	msg, _ := p.GetSpecsResponse()
	p.Connection.Close()
	os.Mkdir(docsDir, 0755)
	os.Mkdir(outDir, 0755)
	json.WriteJS(getSpecs(msg.SpecsResponse), files, outDir, html)
	writeCSS()
	var lastSpec string
	for i, file := range files {
		convertFile(file, files, i, &lastSpec)
	}
	createIndex()
	fmt.Println("Succesfully converted specs to html. Open docs/html/index.html")
}
func getSpecs(m *gauge_messages.SpecsResponse) []*gauge_messages.ProtoSpec {
	specs := make([]*gauge_messages.ProtoSpec, 0)
	for _, d := range m.Details {
		if d.Spec != nil {
			specs = append(specs, d.Spec)
		}
	}
	return specs
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
	output = fmt.Sprintf(htmlTemplate, constant.IncludeCSS, output, previous, constant.IncludeIndex, next)
	name := strings.TrimSuffix(filepath.Base(file), filepath.Ext(file))
	f, _ := os.Create(outDir + string(filepath.Separator) + name + html)
	f.Write([]byte(output))
	f.Close()
	*lastSpec = name + html
}

func createIndex() {
	f, _ := os.Create(outDir + string(filepath.Separator) + indexFile)
	input := constant.IncludeCSS + constant.DataFile + fmt.Sprintf(constant.IndexContent, filepath.Base(projectRoot)) + constant.IndexJS
	f.WriteString(input)
	f.Close()
}

func writeCSS() {
	f, _ := os.Create(outDir + string(filepath.Separator) + styleCSS)
	f.Write([]byte(constant.CSS))
	f.Close()
}
