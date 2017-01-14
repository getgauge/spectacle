package json

import (
	"encoding/json"
	"fmt"
	"os"
	"path/filepath"
	"strings"

	"github.com/getgauge/spectacle/constant"
	"github.com/getgauge/spectacle/gauge_messages"
	"github.com/getgauge/spectacle/util"
)

var projectRoot = util.GetProjectRoot()

type specInfo struct {
	Path      string         `json:"path"`
	Name      string         `json:"name"`
	Scenarios []scenarioInfo `json:"scenarios"`
}

type scenarioInfo struct {
	Name string   `json:"name"`
	Tags []string `json:"tags"`
}

func WriteJS(specs []*gauge_messages.ProtoSpec, files []string, outDir string, fileExtn string) {
	specs = sortSpecs(specs, files)
	specScenariosMap, tags := getSpecsScenariosMap(specs, fileExtn)
	f, err := os.Create(outDir + string(filepath.Separator) + "data.js")
	util.Fatal("Unable to create data.js file", err)
	f.WriteString(fmt.Sprintf("%s\n%s", tags, specScenariosMap))
	f.Close()
	f, err = os.Create(outDir + string(filepath.Separator) + "index.js")
	util.Fatal("Unable to create index.js file", err)
	f.WriteString(constant.IndexJSContent)
	f.Close()
}

func sortSpecs(specs []*gauge_messages.ProtoSpec, files []string) []*gauge_messages.ProtoSpec {
	var sortedSpecs []*gauge_messages.ProtoSpec
	specsMap := make(map[string]*gauge_messages.ProtoSpec)
	for _, spec := range specs {
		specsMap[spec.GetFileName()] = spec
	}
	for _, file := range files {
		if util.IsConceptFile(file) {
			continue
		}
		spec, ok := specsMap[file]
		if !ok {
			spec = &gauge_messages.ProtoSpec{FileName: file, Tags: make([]string, 0), Items: make([]*gauge_messages.ProtoItem, 0)}
		}
		sortedSpecs = append(sortedSpecs, spec)
	}
	return sortedSpecs
}

func getSpecsScenariosMap(specs []*gauge_messages.ProtoSpec, fileExtn string) (string, string) {
	var specsInfos []specInfo
	tags := make(map[string]bool)
	for _, spec := range specs {
		relPath, _ := filepath.Rel(projectRoot, spec.GetFileName())
		fileName := strings.TrimSuffix(relPath, filepath.Ext(spec.GetFileName()))
		si := specInfo{Path: fileName + fileExtn, Name: spec.GetSpecHeading(), Scenarios: make([]scenarioInfo, 0)}
		addTags(spec.Tags, tags)
		for _, item := range spec.Items {
			if item.GetItemType() == gauge_messages.ProtoItem_Scenario {
				addTags(item.Scenario.Tags, tags)
				scnInfo := scenarioInfo{Name: item.Scenario.GetScenarioHeading(), Tags: append(item.Scenario.Tags, spec.Tags...)}
				if len(scnInfo.Tags) < 1 {
					scnInfo.Tags = make([]string, 0)
				}
				si.Scenarios = append(si.Scenarios, scnInfo)
			}
		}
		specsInfos = append(specsInfos, si)
	}
	j, err := json.Marshal(specsInfos)
	if err != nil {
		fmt.Printf("Cannot convert specs to HTML. Reason: %s\n", err.Error())
		return "", ""
	}
	return fmt.Sprintf("var specs = %s", string(j)), fmt.Sprintf("var tags = [%s]", strings.Join(getUniqueTags(tags), ", "))
}

func addTags(tags []string, tagsMap map[string]bool) {
	for _, tag := range tags {
		tagsMap[fmt.Sprintf(`"%s"`, tag)] = true
	}
}

func getUniqueTags(tagsMap map[string]bool) []string {
	var uniqueTags []string
	for tag, _ := range tagsMap {
		uniqueTags = append(uniqueTags, tag)
	}
	return uniqueTags
}
