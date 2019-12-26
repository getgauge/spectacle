package main

import (
	"context"
	"fmt"
	"net"
	"os"
	"path/filepath"
	"strings"

	"github.com/getgauge/spectacle/constant"
	"github.com/getgauge/spectacle/conv"
	"github.com/getgauge/spectacle/gauge_messages"
	"github.com/getgauge/spectacle/json"
	"github.com/getgauge/spectacle/util"
	"google.golang.org/grpc"
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

const tenGB = 1024 * 1024 * 1024 * 10

type handler struct {
	server *grpc.Server
}

func (h *handler) GenerateDocs(c context.Context, m *gauge_messages.SpecDetails) (*gauge_messages.Empty, error) {
	var files []string
	for _, arg := range strings.Split(os.Getenv(gaugeSpecsDir), fileSeparator) {
		files = append(files, util.GetFiles(arg)...)
	}
	util.CreateDirectory(outDir)
	json.WriteJS(conv.GetSpecs(m), files, outDir, dotHtml)
	writeCSS()
	for i, file := range files {
		conv.ConvertFile(file, files, i)
	}
	createIndex()
	fmt.Printf("Succesfully converted specs to html => %s\n", filepath.Join(outDir, indexFile))
	return &gauge_messages.Empty{}, nil
}

func (h *handler) Kill(c context.Context, m *gauge_messages.KillProcessRequest) (*gauge_messages.Empty, error) {
	defer h.stopServer()
	return &gauge_messages.Empty{}, nil
}

func (h *handler) stopServer() {
	h.server.Stop()
	os.Exit(0)
}

func main() {
	os.Chdir(projectRoot)
	address, err := net.ResolveTCPAddr("tcp", "127.0.0.1:0")
	if err != nil {
		util.Fatal("failed to start server.", err)
	}
	l, err := net.ListenTCP("tcp", address)
	if err != nil {
		util.Fatal("failed to start server.", err)
	}
	server := grpc.NewServer(grpc.MaxRecvMsgSize(1024 * 1024 * 10))
	h := &handler{server: server}
	gauge_messages.RegisterDocumenterServer(server, h)
	fmt.Println(fmt.Sprintf("Listening on port:%d", l.Addr().(*net.TCPAddr).Port))
	server.Serve(l)
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
