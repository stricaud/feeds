package mispfetch

import "fmt"
import "io/ioutil"
import "net/http"
import "encoding/json"
import "strings"

import "gopkg.in/ini.v1"

import "../mispobjects"

func FetchTagsIds(config *ini.File) {
	var misptags mispobjects.TagForJson
	
	misp_tags_url := config.Section("DEFAULT").Key("misp_url").String() + "/tags"	

	client := &http.Client{}
	req, _ := http.NewRequest("GET", misp_tags_url, nil)
	req.Header.Set("Authorization", config.Section("DEFAULT").Key("misp_key").String())
	req.Header.Set("Content-type", "application/json")
	req.Header.Set("Accept", "application/json")	
	res, _ := client.Do(req)

	tags, err := ioutil.ReadAll(res.Body)
	res.Body.Close()
	if err != nil {
		fmt.Println("Error reading data:", err)
	}
	// fmt.Printf("%s", tags)
	err = json.Unmarshal(tags, &misptags)
	if err != nil {
		fmt.Println("Error unmarshaling:", err)
	}
	for i := 0; i < len(misptags.Tags); i++ {
		tag := misptags.Tags[i]
		escaped_name := strings.ReplaceAll(tag.Name, "\"", "\"\"")
		tag_exportable := "0"
		if tag.Exportable {
			tag_exportable = "1"
		}
		fmt.Printf("%s,%s,\"%s\",%s\n", tag.Id, tag.Colour, escaped_name, tag_exportable)
	}
	
}

func FetchEventsIds(config *ini.File) {
	// var mispevents mispobjects.EventArrayForJson
	var mispevents []mispobjects.Event
	
	misp_events_url := config.Section("DEFAULT").Key("misp_url").String() + "/events"

	client := &http.Client{}
	req, _ := http.NewRequest("GET", misp_events_url, nil)
	req.Header.Set("Authorization", config.Section("DEFAULT").Key("misp_key").String())
	req.Header.Set("Content-type", "application/json")
	req.Header.Set("Accept", "application/json")	
	res, _ := client.Do(req)

	events, err := ioutil.ReadAll(res.Body)
	res.Body.Close()
	if err != nil {
		fmt.Println("Error reading data:", err)
	}

	err = json.Unmarshal(events, &mispevents)
	if err != nil {
		fmt.Println("Error unmarshaling:", err)
	}
	for i := 0; i < len(mispevents); i++ {
		event := mispevents[i]
		escaped_info := strings.ReplaceAll(event.Info, "\"", "\"\"")
		fmt.Printf("%s,%s,\"%s\"\n", event.Id, event.Uuid, escaped_info)
	}
	
}
