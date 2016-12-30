package conv

import (
	"fmt"
	"io/ioutil"
	"os"
	"path/filepath"
	"strings"

	"github.com/getgauge/spectacle/gauge_messages"
	"github.com/getgauge/spectacle/util"
	"github.com/golang-commonmark/markdown"
)

const (
	dotHtml      = ".html"
	dotCSS       = ".css"
	quotes       = `""''`
	linkTemplate = "<li class=\"nav\"><a href=\"%s\">%s</a></li>"
	htmlTemplate = `%s%s<ul id="navigation"><center>%s%s%s</center>`
	includeIndex = "<li class=\"nav\"><a href=\"%s\">=</a></li>"
	IncludeCSS   = `<link rel="stylesheet" type="text/css" href="%s">`
	indexHtml    = "index.html"
	StyleCSS     = "style.css"
)

var outDir = util.GetOutDir()
var projectRoot = util.GetProjectRoot()

func ConvertFile(file string, files []string, index int) {
	md := markdown.New(markdown.XHTMLOutput(true), markdown.Nofollow(true), markdown.Quotes(quotes), markdown.Typographer(false))
	input, err := ioutil.ReadFile(file)
	util.Fatal(fmt.Sprintf("Error while reading %s file", file), err)
	output := md.RenderToString(input)
	var next, prev string
	if index+1 < len(files) {
		next = fmt.Sprintf(linkTemplate, getRelFilePath(file, files[index+1], dotHtml), ">")
	}
	if index != 0 {
		prev = fmt.Sprintf(linkTemplate, getRelFilePath(file, files[index-1], dotHtml), "<")
	}
	toc := fmt.Sprintf(includeIndex, getRelFilePath(file, filepath.Join(projectRoot, indexHtml), dotHtml))
	style := fmt.Sprintf(IncludeCSS, getRelFilePath(file, filepath.Join(projectRoot, StyleCSS), dotCSS))
	output = fmt.Sprintf(htmlTemplate, style, output, prev, toc, next)
	relPath := getRelPath(projectRoot, file)
	util.CreateDirectory(filepath.Join(outDir, filepath.Dir(relPath)))
	name := strings.TrimSuffix(relPath, filepath.Ext(relPath)) + dotHtml
	f, err := os.Create(filepath.Join(outDir, name))
	util.Fatal(fmt.Sprintf("Error while creating %s file", filepath.Join(outDir, name)), err)
	f.Write([]byte(output))
	f.Close()
}

func GetSpecs(m *gauge_messages.SpecsResponse) []*gauge_messages.ProtoSpec {
	specs := make([]*gauge_messages.ProtoSpec, 0)
	for _, d := range m.Details {
		if d.Spec != nil {
			specs = append(specs, d.Spec)
		}
	}
	return specs
}

func getRelFilePath(f, f1, ext string) string {
	base := getRelPath(filepath.Dir(f), filepath.Dir(f1)) + string(filepath.Separator) + filepath.Base(f1)
	return strings.TrimSuffix(base, filepath.Ext(f1)) + ext
}

func getRelPath(f, f1 string) string {
	p, err := filepath.Rel(f, f1)
	util.Fatal("Cannot get relative path", err)
	return p
}
