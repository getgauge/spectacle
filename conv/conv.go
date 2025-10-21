/*----------------------------------------------------------------
 *  Copyright (c) ThoughtWorks, Inc.
 *  Licensed under the Apache License, Version 2.0
 *  See LICENSE in the project root for license information.
 *----------------------------------------------------------------*/
package conv

import (
	"fmt"
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
	input, err := os.ReadFile(file)
	util.Fatal(fmt.Sprintf("Error while reading %s file", file), err)
	input = normalizeTables(input)
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
	f.Write([]byte("<article class='markdown-body'><meta charset=\"utf-8\">" + output + "</article>"))
	f.Close()
}

func GetSpecs(m *gauge_messages.SpecDetails) []*gauge_messages.ProtoSpec {
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

// Insert a blank line before a table start (lines beginning with '|')
// when the previous line is non-empty and not itself a table line
func normalizeTables(input []byte) []byte {
	s := strings.ReplaceAll(string(input), "\r\n", "\n")
	lines := strings.Split(s, "\n")
	out := make([]string, 0, len(lines)+4)
	for i := 0; i < len(lines); i++ {
		trim := strings.TrimLeft(lines[i], " \t")
		if strings.HasPrefix(trim, "|") && len(out) > 0 {
			prevTrim := strings.TrimSpace(out[len(out)-1])
			// if previous line is not empty and not a table row, add a blank line
			if prevTrim != "" && !strings.HasPrefix(prevTrim, "|") {
				out = append(out, "")
			}
		}
		out = append(out, lines[i])
	}
	return []byte(strings.Join(out, "\n"))
}
