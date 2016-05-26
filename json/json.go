package json

import (
	"encoding/json"
	"fmt"
	"os"
	"path/filepath"
	"strings"

	"github.com/getgauge/M2H/constant"
	"github.com/getgauge/M2H/gauge_messages"
)

type specInfo struct {
	Path string `json:"path"`
	Name string `json:"name"`
}

type scenarioInfo struct {
	Name string   `json:"name"`
	Tags []string `json:"tags"`
}

func WriteJS(specs []*gauge_messages.ProtoSpec, outDir string, fileExtn string) {
	specScenariosMap, tags := getSpecsScenariosMap(specs, fileExtn)
	f, _ := os.Create(outDir + string(filepath.Separator) + "data.js")
	f.WriteString(fmt.Sprintf("%s\n%s", tags, specScenariosMap))
	f.Close()
	f, _ = os.Create(outDir + string(filepath.Separator) + "index.js")
	f.WriteString(constant.IndexJSContent)
	f.Close()
}

func getSpecsScenariosMap(specs []*gauge_messages.ProtoSpec, fileExtn string) (string, string) {
	specScenarioMap := make(map[string][]scenarioInfo)
	tags := make(map[string]bool)
	for _, spec := range specs {
		fileName := strings.TrimSuffix(filepath.Base(spec.GetFileName()), filepath.Ext(spec.GetFileName()))
		si := specInfo{Path: fileName + fileExtn, Name: spec.GetSpecHeading()}
		specBytes, err := json.Marshal(si)
		specJSON := string(specBytes)
		if err != nil {
			fmt.Println("Skipping spec %s. Reason: %s", spec.GetFileName(), err.Error())
			continue
		}
		specScenarioMap[specJSON] = make([]scenarioInfo, 0)
		addTags(spec.Tags, tags)
		for _, item := range spec.Items {
			if item.GetItemType() == gauge_messages.ProtoItem_Scenario {
				addTags(item.Scenario.Tags, tags)
				scnInfo := scenarioInfo{Name: item.Scenario.GetScenarioHeading(), Tags: append(item.Scenario.Tags, spec.Tags...)}
				if len(scnInfo.Tags) < 1 {
					scnInfo.Tags = make([]string, 0)
				}
				specScenarioMap[specJSON] = append(specScenarioMap[specJSON], scnInfo)
			}
		}
	}
	json, err := json.Marshal(specScenarioMap)
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
