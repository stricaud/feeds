package storage

import "fmt"
import "encoding/csv"
import "strings"
import "sync"
import "os"
import "io/ioutil"
import "strconv"

import "gopkg.in/ini.v1"

import "../mispobjects"

var tagidfile_mutex sync.Mutex

func GetTagId(config *ini.File, tag *mispobjects.Tag) int {
	tagidfile_mutex.Lock()
	defer tagidfile_mutex.Unlock()

	tag_id_csv_file := GetTagIdFile(config)

	tag_exportable := "0"
	
	_, err := os.Stat(tag_id_csv_file)
	if os.IsNotExist(err) {
		if tag.Exportable {
			tag_exportable = "1"
		}
		escaped_name := strings.ReplaceAll(tag.Name, "\"", "\"\"")
		filebuf := "1," + tag.Colour + ",\"" + escaped_name + "\"," + tag_exportable + "\n"
		err = ioutil.WriteFile(tag_id_csv_file, []byte(filebuf), 0600)
		if err != nil {
			fmt.Println("Error writing to Tag ID file:", err)
		}
		return 1		
	} else {
		file, err := os.OpenFile(tag_id_csv_file, os.O_APPEND|os.O_CREATE|os.O_RDWR, 0600)
		if err != nil {
			fmt.Println("Error reader from CSV Tag ID file:", err)
		}
		defer file.Close()
		
		reader := csv.NewReader(file)
		records, _ := reader.ReadAll()

		last_id := 0
		for _, line := range records {
			id, _ := strconv.Atoi(line[0])
			name  := line[2]
			if id > last_id {
				last_id = id
			}
			if strings.Compare(name, tag.Name) == 0 {
				return id
			}
		}

		if tag.Exportable {
			tag_exportable = "1"
		}
		escaped_name := strings.ReplaceAll(tag.Name, "\"", "\"\"")
		filebuf := strconv.Itoa(last_id + 1) + "," + tag.Colour + ",\"" + escaped_name + "\"," + tag_exportable + "\n"
		file.Write([]byte(filebuf))
		
		return last_id + 1
	}
	
	return 0
}
