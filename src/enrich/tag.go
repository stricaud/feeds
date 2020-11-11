package enrich

import "fmt"
import "sync"
import "os"
import "encoding/csv"
import "strings"

import "gopkg.in/ini.v1"

import "../mispobjects"
import "../storage"

var tagidfile_mutex sync.Mutex

func EnrichTag(config *ini.File, tag_name string) mispobjects.Tag {
	tagidfile_mutex.Lock()
	defer tagidfile_mutex.Unlock()

	tag_id_csv_file := storage.GetTagIdFile(config)
	file_r, err := os.OpenFile(tag_id_csv_file, os.O_RDONLY, 0600)
	if err != nil {
		fmt.Println("Error reading from Attribute ID file:", err)
	}
	defer file_r.Close()

	reader := csv.NewReader(file_r)
	records, _ := reader.ReadAll()

	var tag mispobjects.Tag
	tag.Name = tag_name // If we cannot enrich, we keep the name
	
	for _, line := range records {
		id     := line[0]
		colour := line[1]
		name   := line[2]
		
		if strings.Compare(name, tag_name) == 0 {
			tag.Id = id
			tag.Colour = colour
			break
		}
	}
	
	return tag
}

func EnrichTagFromId(config *ini.File, tag_id string) mispobjects.Tag {
	tagidfile_mutex.Lock()
	defer tagidfile_mutex.Unlock()

	tag_id_csv_file := storage.GetTagIdFile(config)
	file_r, err := os.OpenFile(tag_id_csv_file, os.O_RDONLY, 0600)
	if err != nil {
		fmt.Println("Error reading from Attribute ID file:", err)
	}
	defer file_r.Close()

	reader := csv.NewReader(file_r)
	records, _ := reader.ReadAll()

	var tag mispobjects.Tag
	tag.Id = tag_id // If we cannot enrich, we keep the id
	
	for _, line := range records {
		id     := line[0]
		colour := line[1]
		name   := line[2]
		
		if strings.Compare(id, tag_id) == 0 {
			tag.Name = name
			tag.Colour = colour
			break
		}
	}
	
	return tag	
}
