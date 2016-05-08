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
	path string
	name string
}

func (s specInfo) MarshalJSON() string {
	return fmt.Sprintf(`{"path": "%s", "name": "%s"}`, s.path, s.name)
}

type scenarioInfo struct {
	name string
	tags []string
}

func (s scenarioInfo) MarshalJSON() ([]byte, error) {
	json := fmt.Sprintf(`{"name": "%s", "tags": ["%s"]}`, strings.Replace(s.name, "\\", "\\\\", -1), strings.Join(s.tags, "\", \""))
	return []byte(json), nil
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
		si := specInfo{path: fileName + fileExtn, name: spec.GetSpecHeading()}
		specJSON := si.MarshalJSON()
		specScenarioMap[specJSON] = make([]scenarioInfo, 0)
		addTags(spec.Tags, tags)
		for _, item := range spec.Items {
			if item.GetItemType() == gauge_messages.ProtoItem_Scenario {
				addTags(item.Scenario.Tags, tags)
				scnInfo := scenarioInfo{name: item.Scenario.GetScenarioHeading(), tags: append(item.Scenario.Tags, spec.Tags...)}
				specScenarioMap[specJSON] = append(specScenarioMap[specJSON], scnInfo)
			}
		}
	}
	json, err := json.Marshal(specScenarioMap)
	if err != nil {
		fmt.Println(err.Error())
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
