package main

import (
	"fmt"
	"os"
	"path/filepath"
	"strings"

	"github.com/getgauge/spectacle/constant"
	"github.com/getgauge/spectacle/conv"
	"github.com/getgauge/spectacle/json"
	"github.com/getgauge/spectacle/processor"
	"github.com/getgauge/spectacle/util"
)

const (
	dotHtml       = ".html"
	localhost     = "localhost"
	gaugeSpecsDir = "GAUGE_SPEC_DIRS"
	gaugeApiPort  = "GAUGE_API_PORT"
	space         = " "
	fileSeparator = "||"
	indexFile     = "index.html"
	styleCSS      = "style.css"
)

var outDir = util.GetOutDir()
var projectRoot = util.GetProjectRoot()

func main() {
	var files []string
	for _, arg := range strings.Split(os.Getenv(gaugeSpecsDir), fileSeparator) {
		files = append(files, util.GetFiles(arg)...)
	}
	p, err := processor.NewMessageProcessor(localhost, os.Getenv(gaugeApiPort))
	util.Fatal("Cannot connect to Gauge API", err)
	msg, err := p.GetSpecsResponse()
	util.Fatal("Cannot connect to Gauge API", err)
	p.Connection.Close()
	util.CreateDirectory(outDir)
	json.WriteJS(conv.GetSpecs(msg.SpecsResponse), files, outDir, dotHtml)
	writeCSS()
	for i, file := range files {
		conv.ConvertFile(file, files, i)
	}
	createIndex()
	fmt.Printf("Succesfully converted specs to html => %s\n", filepath.Join(outDir, indexFile))
}

func createIndex() {
	f, err := os.Create(outDir + string(filepath.Separator) + indexFile)
	util.Fatal("Unable to create index.html", err)
	style := fmt.Sprintf(conv.IncludeCSS, conv.StyleCSS)
	header := fmt.Sprintf(constant.IndexContent, strings.Title(filepath.Base(projectRoot)))
	input := style + constant.DataFile + header + constant.IndexJS
	f.WriteString("<article class='markdown-body'><meta charset=\"utf-8\">" + input + "</article>")
	f.Close()
}

func writeCSS() {
	f, err := os.Create(outDir + string(filepath.Separator) + styleCSS)
	util.Fatal("Error while creating style.css file", err)
	f.Write([]byte(constant.CSS))
	f.Close()
}
