package feeds

import "fmt"
import "net/http"
import "encoding/json"
import "io/ioutil"
import "encoding/csv"
import "strings"

import "../mispobjects"

func DetectMispFeed(feed *Feed) bool {

	client := &http.Client{}

	misp_manifest := feed.Url + "/manifest.json"

	req, err := http.NewRequest("GET", misp_manifest, nil)
	if err != nil {
		return false
	}
	resp, err := client.Do(req)	
	if err != nil {
		return false
	}
	defer resp.Body.Close()

	if resp.StatusCode == 200 {
		return true
	}
	
	return false
}

func DetectMispResponseJson(feed *Feed) bool {
	client := &http.Client{}

	req, err := http.NewRequest("GET", feed.Url, nil)
	if err != nil {
		return false
	}
	resp, err := client.Do(req)
	if err != nil {
		return false
	}
	defer resp.Body.Close()

	if resp.StatusCode == 200 {
		var misp_resp_json mispobjects.MispResponse

		body, err := ioutil.ReadAll(resp.Body)
		if err != nil {			
			return false
		}
		
		err = json.Unmarshal(body, &misp_resp_json)
		if err != nil {
			return false
		}
		return true

	}

	return false	
}

func DetectCsvFeed(feed *Feed) bool {
	client := &http.Client{}

	req, err := http.NewRequest("GET", feed.Url, nil)
	if err != nil {
		return false
	}
	resp, err := client.Do(req)
	if err != nil {
		return false
	}

	defer resp.Body.Close()

	if resp.StatusCode == 200 {
		body, err := ioutil.ReadAll(resp.Body)
		if err != nil {			
			return false
		}
		
		reader := csv.NewReader(strings.NewReader(string(body)))
		_, err = reader.ReadAll()
		if err != nil {
			return false
		}
		return true
	}

	return false
}

func DetectFeedType(feed *Feed) FeedType {
	fmt.Println("Detecting feed:", feed.Name)

	is_csv := DetectCsvFeed(feed)
	if is_csv {
		return Csv
	}	
	is_misp := DetectMispFeed(feed)
	if is_misp {
		return Misp
	}

	is_misp_response_json := DetectMispResponseJson(feed)
	if is_misp_response_json {
		return MispResponseJson
	}
	
	return NotFound
}
