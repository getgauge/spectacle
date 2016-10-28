package util

import (
	"fmt"
	"os"
	"path/filepath"
)

var docsDir string
var outDir string
var projectRoot string

const (
	out              = "html"
	docs             = "docs"
	gaugeProjectRoot = "GAUGE_PROJECT_ROOT"
	spectacleOutDir  = "spectacle_out_dir"
)

func init() {
	projectRoot = os.Getenv(gaugeProjectRoot)
	err := os.Chdir(projectRoot)
	if err != nil {
		fmt.Printf("Failed to Change working dir to project root %s: %s\n", projectRoot, err)
		os.Exit(1)
	}
	docsDir = filepath.Join(projectRoot, docs)
	p := os.Getenv(spectacleOutDir)
	if p != "" {
		path, err := filepath.Abs(p)
		if err == nil {
			docsDir = path
		} else {
			fmt.Printf("Cannot set %s as output dir. Error: %s\n", p, err.Error())
		}
	}
	outDir = filepath.Join(docsDir, out)
}

func GetOutDir() string {
	return outDir
}

func GetProjectRoot() string {
	return projectRoot
}

func Fatal(message string, err error) {
	if err != nil {
		fmt.Printf("%s. Error: %s", message, err.Error())
		os.Exit(1)
	}
}
