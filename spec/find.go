package spec

import (
	"os"
	"path/filepath"
)

func init() {
	AcceptedExtensions[".spec"] = true
	AcceptedExtensions[".md"] = true
}

var AcceptedExtensions = make(map[string]bool)

func IsValidSpecExtension(path string) bool {
	return AcceptedExtensions[filepath.Ext(path)]
}

func FindFilesInDir(dirPath string, isValidFile func(path string) bool) []string {
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
	absRoot, _ := filepath.Abs(dirRoot)
	files := FindFilesInDir(absRoot, isValidFile)
	return files
}

func DirExists(dirPath string) bool {
	stat, err := os.Stat(dirPath)
	if err == nil && stat.IsDir() {
		return true
	}
	return false
}

func FileExists(path string) bool {
	_, err := os.Stat(path)
	if err == nil {
		return true
	}
	return !os.IsNotExist(err)
}

func GetFiles(path string) []string {
	var specFiles []string
	if DirExists(path) {
		specFiles = append(specFiles, findFilesIn(path, IsValidSpecExtension)...)
	} else if FileExists(path) && IsValidSpecExtension(path) {
		f, _ := filepath.Abs(path)
		specFiles = append(specFiles, f)
	}
	return specFiles
}
