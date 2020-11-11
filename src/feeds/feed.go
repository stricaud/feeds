package feeds

import "strings"
import "strconv"

import "gopkg.in/ini.v1"

type Feed struct {
	Name string `json:"name"`
	Provider string `json:"provider"`
	Input string `json:"input"`
	Headers string `json:"headers"`
	Url string `json:"url"`
	Distribution string `json:"distribution"`
	Tags string `json:"tags"`
	Schedule int `json:"schedule"`
	CsvFields []int
	Delimiter string
	ExclusionRegex string
	TargetEvent string
	SentAttributes map[string]int
	Enabled bool
	Types []string
}

func NewFeed(name string, config *ini.File) *Feed {
	sent_attributes := make(map[string]int)

	sched_time, _ := config.Section(name).Key("schedule").Int()

	var csv_fields []int
	strarray := strings.Split(config.Section(name).Key("csv_fields").String(),",")
	for _, v := range strarray {
		if strings.Compare(v, "") != 0 {
			v_i, _ := strconv.Atoi(v)
			csv_fields = append(csv_fields, v_i)
		}
	}

	typesarray := strings.Split(config.Section(name).Key("csv_fields_type").String(),",")
	
	enabled := true
	if strings.Compare(strings.ToUpper(config.Section(name).Key("enabled").String()),"FALSE") == 0 {
		enabled = false;
	}
	
	return &Feed{name,
		config.Section(name).Key("provider").String(),
		config.Section(name).Key("input").String(),
		config.Section(name).Key("headers").String(),
		config.Section(name).Key("url").String(),
		config.Section(name).Key("distribution").String(),
		config.Section(name).Key("tags").String(),
		sched_time,
		csv_fields,
		config.Section(name).Key("delimiter").String(),
		config.Section(name).Key("exclusion_regex").String(),
		config.Section(name).Key("target_event").String(),
		sent_attributes,
		enabled,
		typesarray,
	}
}

type Feeds struct {
	Feeds []Feed `json:"feeds"`
}
