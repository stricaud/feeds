package storage

import "fmt"
import "io/ioutil"
import "sync"
import "os"
import "bufio"
import "strconv"

import "gopkg.in/ini.v1"

var attributeidfile_mutex sync.Mutex

func GetAttributeId(config *ini.File) uint64 {
	attributeidfile_mutex.Lock()
	defer attributeidfile_mutex.Unlock()

	attribute_id_file := GetAttributeIdFile(config)
	_, err := os.Stat(attribute_id_file)
	if os.IsNotExist(err) {
		// FIXME, in the future I should remove this code and create this file with the number 1
		// upon startup. This would avoid having to check things here.
		err = ioutil.WriteFile(attribute_id_file, []byte("1\n"), 0600)
		if err != nil {
			fmt.Println("Error writing the attribute id file:", err)
		}
		return 1
	} else {
		file_r, err := os.OpenFile(attribute_id_file, os.O_RDONLY, 0600)
		if err != nil {
			fmt.Println("Error reading from Attribute ID file:", err)
		}
		defer file_r.Close()

		file_w, err := os.OpenFile(attribute_id_file, os.O_WRONLY, 0600)
		if err != nil {
			fmt.Println("Error writing to Attribute ID file:", err)
		}
		defer file_w.Close()

		
		scanner := bufio.NewScanner(file_r)
		scanner.Scan()

		attr_id_str := scanner.Text()
		
		attr_id, _ := strconv.ParseUint(attr_id_str, 10, 64)
		attr_id += 1
		new_attr_id_str := string(strconv.FormatUint(attr_id, 10)) + "\n"
		file_w.Write([]byte(new_attr_id_str))
		
		return attr_id
	}

	return 0
}
