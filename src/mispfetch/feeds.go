package mispfetch

import "fmt"
import "io/ioutil"
import "net/http"
import "encoding/json"
import "strings"

import "gopkg.in/ini.v1"

import "../mispobjects"
import "../enrich"

type AllFeeds struct {
	Feed mispobjects.Feed `json:"Feed"`
	Tag mispobjects.Tag `json:"Tag"`
	Orgc mispobjects.Orgc `json:"Orgc"`	
}

// {"csv":{"value":"1","delimiter":""},"common":{"excluderegex":""}}",
type Settings struct {
	Csv CsvSettings `json:"csv"`
	Common CommonSettings `json:"common"`
}

type CsvSettings struct {
	Value string `json:"value"`
	Delimiter string `json:"delimiter"`
}

type CommonSettings struct {
	ExcludeRegex string `json:"excluderegex"`
}

func FetchFeeds(config *ini.File) {
	misp_feeds_url := config.Section("DEFAULT").Key("misp_url").String() + "/feeds"

	client := &http.Client{}
	req, _ := http.NewRequest("GET", misp_feeds_url, nil)
	req.Header.Set("Authorization", config.Section("DEFAULT").Key("misp_key").String())
	req.Header.Set("Content-type", "application/json")
	req.Header.Set("Accept", "application/json")	
	res, _ := client.Do(req)

	feedsbuf, err := ioutil.ReadAll(res.Body)
	res.Body.Close()
	if err != nil {
		fmt.Println("Error reading data:", err)
	}
	// fmt.Println(string(feedsbuf))

	var feeds []AllFeeds
	json.Unmarshal(feedsbuf, &feeds)
	
	for i := 0; i < len(feeds); i++ {
		if strings.Compare(feeds[i].Feed.Name, "") == 0 { continue } // I see empty feeds!
		fmt.Printf("[%s]\n", feeds[i].Feed.Name)
		enabled := "false"
		if feeds[i].Feed.Enabled == true {
			enabled = "true"
		}
		fmt.Printf("enabled=%s\n", enabled)
		fmt.Printf("provider=%s\n", feeds[i].Feed.Provider)
		fmt.Printf("input=%s\n", feeds[i].Feed.InputSource)
		fmt.Printf("url=%s\n", feeds[i].Feed.Url)

		if strings.Compare(feeds[i].Feed.SourceFormat, "csv") == 0 {
			var settings Settings
			json.Unmarshal([]byte(feeds[i].Feed.Settings), &settings)
			fmt.Printf("csv_fields=%s\n", settings.Csv.Value)
			fmt.Printf("#csv_fields_type=ip-src\n")
			fmt.Printf("delimiter=%s\n", settings.Csv.Delimiter)
			fmt.Printf("exclusion_regex=%s\n", settings.Common.ExcludeRegex)
		}

		tag := enrich.EnrichTagFromId(config, feeds[i].Feed.TagId)
		
		fmt.Printf("tags=%s\n", tag.Name)
		

		
		fmt.Println("schedule=86400") // Defaults to everyday

		fmt.Println("")
	}
	
}
