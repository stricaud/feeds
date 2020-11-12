package main

import "os"
import "fmt"
import "strings"
// import "encoding/json"
// import "net/http"
// import "io/ioutil"
import "strconv"
import "time"
// import "encoding/csv"

import "gopkg.in/ini.v1"

import "./mispobjects"
import "./storage"
import "./feeds"
import "./submit"
import "./mispfetch"
import "./uuidizer"
// import "./enrich"
import "./context"
import "./feedsfetch"

func fetch_feed(context *context.FeedsContext, feed feeds.Feed) {	
	fmt.Println("Fetching feed:", feed.Name)
	storage.CreateFeedCacheDir(context.Config, feed)
	feedtype := feeds.DetectFeedType(&feed)
	
	// storage.LoadSentFile(context.Config, feed)	
	
	// client := &http.Client{}

	for {
		switch(feedtype) {		
		case feeds.Csv:
			context.Logger.Println("Starting reading feed ", feed.Name, " Feed Type: CSV")
		case feeds.Misp:
			context.Logger.Println("Starting reading feed ", feed.Name, " Feed Type: MISP")
			feedsfetch.HandleMISPFeed(context, feed)
		case feeds.MispResponseJson:
			context.Logger.Println("Starting reading feed ", feed.Name, " Feed Type: MISP Response Json")
		default:
			context.Logger.Println("Error reading feed ", feed.Name, " Feed Type: Unknown feed. Ignored.")
		}


		
	// 	misp_manifest := feed.Url + "/manifest.json"
	// 	// fmt.Println("Manifest:", misp_manifest)
	// 	req, err := http.NewRequest("GET", misp_manifest, nil)
	// 	if err != nil {
	// 		fmt.Println("Error making request!")
	// 	}
	// 	resp, err := client.Do(req)
	// 	defer resp.Body.Close()

	// 	if err != nil {
	// 		fmt.Println("Error with response!")
	// 	}

	// 	if resp.StatusCode != 200 {
	// 		fetch_non_misp_feed(context, client, feed)
	// 	} else {		
	// 		body, err := ioutil.ReadAll(resp.Body)
	// 		if err != nil {
	// 			fmt.Println("Error reading manifest file:", err)
	// 		} 
		
	// 		manifest_file := storage.GetFeedCacheDir(context.Config, feed) + string(os.PathSeparator) + "manifest.json"
	// 		manifest_fp, err := os.OpenFile(manifest_file, os.O_WRONLY|os.O_CREATE, 0600)
	// 		if err != nil {
	// 			fmt.Println("Cannot open manifest file for writing.")
	// 		}
	// 		manifest_fp.Write(body)
	// 		manifest_fp.Close()	
	
	// 		evt := make(map[string]*mispobjects.ManifestEvent)
			
	// 		err = json.Unmarshal(body, &evt)
	// 		if err != nil {
	// 			fmt.Println("Error unmarshaling Event.")
	// 		}
	// 		for k, _ := range evt {
	// 			fetch_misp_event(context, client, feed, string(k))
	// 			// fmt.Printf("%#v-->%#v\n", k, e)
	// 		}
	// 	} // if resp.StatusCode != 200 { ... else {

		time.Sleep(time.Duration(feed.Schedule) * time.Second)
	} // for {

}

func HandleArguments(context *context.FeedsContext) {
	if len(os.Args) > 1 {
		switch os.Args[1] {
		case "--help":
			fmt.Println("Syntax: feeds [options]")
			fmt.Println("Where options could be one of:")
			fmt.Println("\t--misp-tags: to fetch the misp tags from your MISP instance")
			fmt.Println("\t--misp-events: to fetch the misp events from your MISP instance")
			fmt.Println("\t--misp-feeds: Dump an ini file to be added to feeds.ini from your MISP instance")
			fmt.Println("\t--misp-sync-in: Read everything from MISP to update your feeds instance")
			break
		case "--misp-tags":
			mispfetch.FetchTagsIds(context.Config)
			break
		case "--misp-events":
			mispfetch.FetchEventsIds(context.Config)
			break
		case "--misp-feeds":
			mispfetch.FetchFeeds(context.Config)
			break
		case "--misp-sync-in":
			mispfetch.SyncIn(context)
			break
		default:
			fmt.Println("No such argument:", os.Args[1])
		}
		os.Exit(0)
	}
}

func main() {
	feeds_conf, err := ini.Load("feeds.ini")
	if err != nil {
		fmt.Printf("Fail to read feeds config file: %v", err)
		os.Exit(1)
	}
	// filters_conf, err := ini.Load("feeds.ini")
	// if err != nil {
	// 	fmt.Printf("Fail to read filters config file: %v", err)
	// 	os.Exit(1)
	// }	
	
	sections := feeds_conf.SectionStrings()

	storage.CreateStorageDir(feeds_conf.Section("DEFAULT").Key("cache_storage").String())

	devosender := submit.DevoSender{}
	devosender.NewDevoSender(
		feeds_conf.Section("DEFAULT").Key("devo_host").String(),
		feeds_conf.Section("DEFAULT").Key("devo_port").String(),
		feeds_conf.Section("DEFAULT").Key("devo_chain").String(),
		feeds_conf.Section("DEFAULT").Key("devo_cert").String(),
		feeds_conf.Section("DEFAULT").Key("devo_key").String(),
		false)

	devosender.Conn, err = devosender.Connect()
	if (err != nil) {
		fmt.Println("Error connecting:", err)
		os.Exit(1)
	} else {
		fmt.Println("Connected")
	}

	context := context.FeedsContext{}
	context.New(feeds_conf, &devosender)
	defer context.Close()

	HandleArguments(&context)	

	
	context.Logger.Println("Starting Feeds process.")
	
	// devosender.Send(conn, "dns.bind.query", "0,22-Jun-2020 13:27:16.000000 queries: info: client 192.168.38.173#4096: query: example.com A IN +")

	// all_feeds := feeds.Feeds{}
	
	// for index, item := range sections {
	for _, item := range sections {
		if ( strings.Compare(item, "DEFAULT") != 0 ) {
			feed := feeds.NewFeed(item, feeds_conf)
			if feed.Enabled == true {
				// all_feeds.Feeds = append(all_feeds.Feeds, *feed)
				go fetch_feed(&context, *feed)
			}
		}
	}


	for {
		time.Sleep(1 * time.Second)
	}	
}

func submit_event(context *context.FeedsContext, feed feeds.Feed, event *mispobjects.Event) {
	eventid := storage.GetEventId(context.Config, event)
	eventid_str := strconv.Itoa(eventid)
	event.Id = eventid_str

	uuidizer.EventUuidize(event)

	send := true
	if strings.Compare(strings.ToUpper(context.Config.Section("DEFAULT").Key("send").String()),"FALSE") == 0 {
		send = false;
	}

	
	// event_json, _ := json.Marshal(event)
	// fmt.Println(string(event_json))

	// fmt.Printf("Eventtags:%v\n", event.EventTag)
	// eventtag_json, _ := json.Marshal(event.EventTag)
	// fmt.Printf("%s\n", eventtag_json)
	
	events := submit.BuildDevoMispFromEvent(context.Config, event)
	for _, ev := range events {
		// event_json, _ := json.Marshal(ev)
		// fmt.Printf("%s\n", event_json)
		// // fmt.Println(string(event_json))
		// os.Exit(0)
		if send {
			context.DevoSender.Send(context.DevoSender.Conn, "threatintel.misp.attributes", ev)
		}
	}
}
