package feedsfetch

import "encoding/json"
import "net/http"
import "os"
import "io/ioutil"

import "../mispobjects"
import "../storage"
import "../context"
import "../feeds"


// func fetch_misp_event(context *context.FeedsContext, client *http.Client, feed feeds.Feed, event_uuid string) {
// 	event_uri := feed.Url + "/" + event_uuid + ".json"
// 	fmt.Println(event_uri)

// 	var evt *mispobjects.EventForJson
	
// 	// fmt.Printf("%T\n", client)
// 	event_cache_file := storage.GetEventCacheFile(context.Config, feed, event_uuid)
// 	event_cache_fp, err := os.OpenFile(event_cache_file, os.O_CREATE|os.O_APPEND|os.O_WRONLY, 0600)
// 	if (err != nil) {
// 		fmt.Println("Error creating cache file:", err)
// 	}
// 	defer event_cache_fp.Close()
	
// 	req, err := http.NewRequest("GET", event_uri, nil)
// 	if err != nil {
// 		fmt.Println("Error making request!")
// 	}
// 	resp, err := client.Do(req)
// 	defer resp.Body.Close()

// 	if err != nil {
// 		fmt.Println("Error with response!")
// 	}
// 	body, err := ioutil.ReadAll(resp.Body)

// 	// fmt.Println(string(body))
	
// 	err = json.Unmarshal(body, &evt)
// 	if err != nil {
// 		fmt.Println("Error unmarshaling")
// 	}
	
// 	attributes := evt.Event.Attributes
// 	for _, attr := range attributes {
// 		exists := feed.SentAttributes[attr.Uuid]
// 		if exists == 0 {
// 			// This value does not exists, it must be stored and sent
// 			feed.SentAttributes[attr.Uuid] = 1
// 			event_cache_fp.WriteString(attr.Uuid + "\n")

// 			submit_event(context, feed, &evt.Event)
			
// 			// fmt.Println(string(attr.Uuid))
// 		} // else {
// 		// 	fmt.Println("This value exists. We have nothing to do")
// 		// }
// 	}
	
// 	// fmt.Printf("+++++++++++++++++++ %#v\n", evt)
// }

// func HandleMISPFeed(context *context.FeedsContext, feed feeds.Feed, submit_cb func(context *context.FeedsContext, feed feeds.Feed, event *mispobjects.Event)) {
func HandleMISPFeed(context *context.FeedsContext, feed feeds.Feed) {
	client := &http.Client{}

	misp_manifest := feed.Url + "/manifest.json"
	// fmt.Println("Manifest:", misp_manifest)
	req, err := http.NewRequest("GET", misp_manifest, nil)
	if err != nil {
		context.Logger.Println("HandleMISPFeed:", feed.Name ,"; Error making request:", err)
		return
	}
	resp, err := client.Do(req)
	if err != nil {
		context.Logger.Println("HandleMISPFeed:", feed.Name ,"; Error with response:", err)
		return
	}
	defer resp.Body.Close()

	if resp.StatusCode != 200 {
		context.Logger.Println("HandleMISPFeed:", feed.Name ,"; Error with Feed URL. Status code != 200")
		return
	} else {
		body, err := ioutil.ReadAll(resp.Body)
		if err != nil {
			context.Logger.Println("Error reading manifest file:", err)
		} 
		
		manifest_file := storage.GetFeedCacheDir(context.Config, feed) + string(os.PathSeparator) + "manifest.json"
		manifest_fp, err := os.OpenFile(manifest_file, os.O_WRONLY|os.O_CREATE, 0600)
		if err != nil {
			context.Logger.Println("Cannot open manifest file for writing:", err)
		}
		manifest_fp.Write(body)
		manifest_fp.Close()	
		
		evt := make(map[string]*mispobjects.ManifestEvent)
		
		err = json.Unmarshal(body, &evt)
		if err != nil {
			context.Logger.Println("Error unmarshaling Event: ", err)
		}
		for k, _ := range evt {
			// fetch_misp_event(context, client, feed, string(k))
			context.Logger.Printf("%#v\n", k)
		}
		
	}
	
}
