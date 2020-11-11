package storage

import "os"
import "fmt"
import "encoding/base64"
import "strings"
import "io/ioutil"
import "bufio"
import "io"

import "gopkg.in/ini.v1"

import "../feeds"

func CreateStorageDir(storage_path string) {
	os.MkdirAll(storage_path, 0700)
}

func GetFeedCacheDir(config *ini.File, feed feeds.Feed) string {
	feed_name_b64 := []byte(feed.Name) 
	feed_cache_dir := config.Section("DEFAULT").Key("cache_storage").String() + string(os.PathSeparator) + base64.StdEncoding.EncodeToString(feed_name_b64)
	
	return feed_cache_dir
}

func GetEventCacheFile(config *ini.File, feed feeds.Feed, event_uuid string) string {
	f := GetFeedCacheDir(config, feed) + string(os.PathSeparator) + event_uuid
	return f
}

func CreateFeedCacheDir(config *ini.File, feed feeds.Feed) {
	os.MkdirAll(GetFeedCacheDir(config, feed), 0700)
}

func LoadSentFile(config *ini.File, feed feeds.Feed) {
	feed_cache := GetFeedCacheDir(config, feed)

	files, err := ioutil.ReadDir(feed_cache)
	if err != nil {
		fmt.Println("Error reading:", feed_cache)
		return
	}

	for _, file := range files {
		if strings.Compare("manifest.json", file.Name()) != 0 {
			file_path := feed_cache + string(os.PathSeparator) + file.Name()
			event_fp, err := os.OpenFile(file_path, os.O_RDONLY, 0600)
			if err != nil {
				fmt.Println("Cannot open event file for reading:", err)
			}
			defer event_fp.Close()

			reader := bufio.NewReader(event_fp)
			for {
				line, _, err := reader.ReadLine()
				if err != nil {
					if err != io.EOF {
						fmt.Println(file_path, ": Error reading line:", err)
					}
					break
				}
				// fmt.Println(file_path, "->",string(line))
				feed.SentAttributes[string(line)] = 1
			}
			
		}
	}

}

func GetEventIdFile(config *ini.File) string {
	event_id_file := config.Section("DEFAULT").Key("cache_storage").String() + string(os.PathSeparator) + "event-id.csv"
	return event_id_file
}

func GetAttributeIdFile(config *ini.File) string {
	attribute_id_file := config.Section("DEFAULT").Key("cache_storage").String() + string(os.PathSeparator) + "attribute.id"
	return attribute_id_file
}

func GetTagIdFile(config *ini.File) string {
	tag_id_file := config.Section("DEFAULT").Key("cache_storage").String() + string(os.PathSeparator) + "tag-id.csv"
	return tag_id_file
}

// func CompareHash(config *ini.File, feed feeds.Feed, buffer []bytes, event_uuid string) int
// {
// 	event_file := GetEventCacheFile(config, feed, event_uuid)
// 	event_file_buffer, err := ioutil.ReadFile(event_file)
	
// 	return bytes.Equal(buffer, event_file_buffer)
	
// }
