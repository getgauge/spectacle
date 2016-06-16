package json

import (
	"encoding/json"
	"fmt"
	"os"
	"path/filepath"
	"strings"

	"github.com/getgauge/M2H/constant"
	"github.com/getgauge/M2H/gauge_messages"
	"github.com/golang/protobuf/proto"
)

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
	f, _ := os.Create(outDir + string(filepath.Separator) + "data.js")
	f.WriteString(fmt.Sprintf("%s\n%s", tags, specScenariosMap))
	f.Close()
	f, _ = os.Create(outDir + string(filepath.Separator) + "index.js")
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
		spec, ok := specsMap[file]
		if !ok {
			spec = &gauge_messages.ProtoSpec{FileName: proto.String(file), Tags: make([]string, 0), Items: make([]*gauge_messages.ProtoItem, 0)}
		}
		sortedSpecs = append(sortedSpecs, spec)
	}
	return sortedSpecs
}

func getSpecsScenariosMap(specs []*gauge_messages.ProtoSpec, fileExtn string) (string, string) {
	var specsInfos []specInfo
	tags := make(map[string]bool)
	for _, spec := range specs {
		fileName := strings.TrimSuffix(filepath.Base(spec.GetFileName()), filepath.Ext(spec.GetFileName()))
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
	json, err := json.Marshal(specsInfos)
	if err != nil {
		fmt.Println("Cannot convert specs to HTML. Reason: %s", err.Error())
		return "", ""
	}
	return fmt.Sprintf("var specs = %s", string(json)), fmt.Sprintf("var tags = [%s]", strings.Join(getUniqueTags(tags), ", "))
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
