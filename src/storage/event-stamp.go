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

var eventidfile_mutex sync.Mutex

func GetEventId(config *ini.File, event *mispobjects.Event) int {
	eventidfile_mutex.Lock()
	defer eventidfile_mutex.Unlock()
	
	event_id_csv_file := GetEventIdFile(config)
	
	_, err := os.Stat(event_id_csv_file)
	if os.IsNotExist(err) {
		// The file does not exists? We create it and we have our Event #1, congrats!
		escaped_event_info := strings.ReplaceAll(event.Info, "\"", "\"\"")
		filebuf := "1," + event.Uuid + ",\"" + escaped_event_info + "\"\n"
		err = ioutil.WriteFile(event_id_csv_file, []byte(filebuf), 0600)
		if err != nil {
			fmt.Println("Error writing to CSV Event ID file:", err)
		}
		return 1
	} else {
		file, err := os.OpenFile(event_id_csv_file, os.O_APPEND|os.O_CREATE|os.O_RDWR, 0600)
		if err != nil {
			fmt.Println("Error reader from CSV Event ID file:", err)
		}
		defer file.Close()
		
		reader := csv.NewReader(file)
		records, _ := reader.ReadAll()

		last_id := 0
		for _, line := range records {
			id, _ := strconv.Atoi(line[0])
			uuid  := line[1]
			if id > last_id {
				last_id = id
			}
			if strings.Compare(uuid, event.Uuid) == 0 {
				return id
			}
		}

		escaped_event_info := strings.ReplaceAll(event.Info, "\"", "\"\"")
		filebuf := strconv.Itoa(last_id + 1) + "," + event.Uuid + ",\"" + escaped_event_info + "\"\n"
		file.Write([]byte(filebuf))
		
		return last_id + 1
	}
	
	return 0
}
