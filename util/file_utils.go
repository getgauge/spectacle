/*----------------------------------------------------------------
 *  Copyright (c) ThoughtWorks, Inc.
 *  Licensed under the Apache License, Version 2.0
 *  See LICENSE in the project root for license information.
 *----------------------------------------------------------------*/
package util

import (
	"fmt"
	"os"
	"path/filepath"
)

func init() {
	AcceptedExtensions[".spec"] = true
	AcceptedExtensions[".md"] = true
	AcceptedExtensions[".cpt"] = true
}

var AcceptedExtensions = make(map[string]bool)

func IsConceptFile(file string) bool {
	return filepath.Ext(file) == ".cpt"
}

func isValidSpecExtension(path string) bool {
	return AcceptedExtensions[filepath.Ext(path)]
}

func findFilesInDir(dirPath string, isValidFile func(path string) bool) []string {
	var files []string
	filepath.Walk(dirPath, func(path string, f os.FileInfo, err error) error {
		if err == nil && !f.IsDir() && isValidFile(path) {
			files = append(files, path)
		}
		return err
	})
	return files
}

func findFilesIn(dirRoot string, isValidFile func(path string) bool) []string {
	absRoot := getAbsPath(dirRoot)
	files := findFilesInDir(absRoot, isValidFile)
	return files
}

func dirExists(dirPath string) bool {
	stat, err := os.Stat(dirPath)
	if err == nil && stat.IsDir() {
		return true
	}
	return false
}

func fileExists(path string) bool {
	_, err := os.Stat(path)
	if err == nil {
		return true
	}
	return !os.IsNotExist(err)
}

func GetFiles(path string) []string {
	var specFiles []string
	if dirExists(path) {
		specFiles = append(specFiles, findFilesIn(path, isValidSpecExtension)...)
	} else if fileExists(path) && isValidSpecExtension(path) {
		specFiles = append(specFiles, getAbsPath(path))
	}
	return specFiles
}

func CreateDirectory(dir string) {
	err := os.MkdirAll(dir, 0755)
	Fatal(fmt.Sprintf("Failed to create directory %s", dir), err)
}

func getAbsPath(path string) string {
	f, err := filepath.Abs(path)
	Fatal("Cannot get absolute path", err)
	return f
}
