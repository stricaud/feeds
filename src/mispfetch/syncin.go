package mispfetch

// import "fmt"
import "io/ioutil"
import "net/http"
import "encoding/json"
// import "net/url"
import "strconv"

import "../mispobjects"
import "../context"
import "../sightingdb"


func SyncIn(context *context.FeedsContext) {
	var misporgs []mispobjects.OrgJsonNamed
	
	misp_orgs_url := context.Config.Section("DEFAULT").Key("misp_url").String() + "/organisations"

	client := &http.Client{}
	sdb := sightingdb.SightingDB{}

	context.Logger.Println("Pulling Organisations from ", misp_orgs_url)
	req, _ := http.NewRequest("GET", misp_orgs_url, nil)
	req.Header.Set("Authorization", context.Config.Section("DEFAULT").Key("misp_key").String())
	req.Header.Set("Content-type", "application/json")
	req.Header.Set("Accept", "application/json")	
	res, _ := client.Do(req)
	data, err := ioutil.ReadAll(res.Body)
	res.Body.Close()
	if err != nil {
		context.Logger.Println("SyncIn: Error reading data:", err)
	}	
	err = json.Unmarshal(data, &misporgs)
	if err != nil {
		context.Logger.Println("SyncIn: Error Unmarshaling data:", err)
	}

	//
	// ORGS
	//
	context.Logger.Println("Inserting Organisations into SightingDB")

	sdb.New(context.Config.Section("DEFAULT").Key("sightingdb_host").String(),
		context.Config.Section("DEFAULT").Key("sightingdb_port").String(),
		context.Config.Section("DEFAULT").Key("sightingdb_key").String(),
		context.Config.Section("DEFAULT").Key("sightingdb_namespace").String())

	sightingdata := sightingdb.CreateDataObject()
	for i := 0; i < len(misporgs); i++ {
		org := misporgs[i].Organisations
		//		fmt.Println(org.Name)

		sightingdb.AppendDataObject(&sdb, sightingdata, "orgs", org.Name)
		sightingdb.AppendDataObject(&sdb, sightingdata, "orgs/id", org.Id)
		sightingdb.AppendDataObject(&sdb, sightingdata, "orgs/" + org.Id + "/data_created", org.DateCreated)
		sightingdb.AppendDataObject(&sdb, sightingdata, "orgs/" + org.Id + "/data_modified", org.DateModified)
		sightingdb.AppendDataObject(&sdb, sightingdata, "orgs/" + org.Id + "/description", org.Description)
		sightingdb.AppendDataObject(&sdb, sightingdata, "orgs/" + org.Id + "/type", org.Type)
		sightingdb.AppendDataObject(&sdb, sightingdata, "orgs/" + org.Id + "/nationality", org.Nationality)
		sightingdb.AppendDataObject(&sdb, sightingdata, "orgs/" + org.Id + "/sector", org.Sector)
		sightingdb.AppendDataObject(&sdb, sightingdata, "orgs/" + org.Id + "/created_by", org.CreatedBy)
		sightingdb.AppendDataObject(&sdb, sightingdata, "orgs/" + org.Id + "/uuid", org.Uuid)
		sightingdb.AppendDataObject(&sdb, sightingdata, "orgs/" + org.Id + "/contacts", org.Contacts)		
		org_local := "false"
		if org.Local {
			org_local = "true"
		}
		sightingdb.AppendDataObject(&sdb, sightingdata, "orgs/" + org.Id + "/local", org_local)		
		sightingdb.AppendDataObject(&sdb, sightingdata, "orgs/" + org.Id + "/landing_page", org.LandingPage)		
		sightingdb.AppendDataObject(&sdb, sightingdata, "orgs/" + org.Id + "/created_by_email", org.CreatedByEmail)		
	}

	sdb.Submit(sightingdata)
	
	context.Logger.Println("Finished Inserting Organisations into SightingDB")

	//
	// TAGS
	//
	context.Logger.Println("Inserting Tags into SightingDB")	
	misp_tags_url := context.Config.Section("DEFAULT").Key("misp_url").String() + "/tags"
	context.Logger.Println("Pulling Tags from ", misp_tags_url)

	req, err = http.NewRequest("GET", misp_tags_url, nil)
	if err != nil {
		context.Logger.Println("Error pulling Tags from ", misp_tags_url, " :", err)		
	}
	req.Header.Set("Authorization", context.Config.Section("DEFAULT").Key("misp_key").String())
	req.Header.Set("Content-type", "application/json")
	req.Header.Set("Accept", "application/json")	
	res, _ = client.Do(req)
	data, err = ioutil.ReadAll(res.Body)
	res.Body.Close()
	if err != nil {
		context.Logger.Println("SyncIn: Error reading data:", err)
	}
	
	var misptags mispobjects.TagForJson
	err = json.Unmarshal(data, &misptags)
	if err != nil {
		context.Logger.Println("SyncIn: Error Unmarshaling data:", err)
	}


	sightingdb.ClearDataObject(sightingdata)
	max_tag_id := 0
	for i := 0; i < len(misptags.Tags); i++ {
		tag := misptags.Tags[i]

		tag_id, _ := strconv.Atoi(tag.Id)
		if tag_id > max_tag_id {
			max_tag_id = tag_id
		}
		
		// TODO: Delete frist to avoid having multiple values
		sightingdb.AppendDataObject(&sdb, sightingdata, "tags", tag.Name)
		sightingdb.AppendDataObject(&sdb, sightingdata, "tags/id", tag.Id)
		
		sightingdb.AppendDataObject(&sdb, sightingdata, "tags/" + tag.Id + "/colour", tag.Colour)
		sightingdb.AppendDataObject(&sdb, sightingdata, "tags/" + tag.Id + "/name", tag.Name)
		exportable := "false"
		if tag.Exportable {
			exportable = "true"
		}
		sightingdb.AppendDataObject(&sdb, sightingdata, "tags/" + tag.Id + "/exportable", exportable)
		sightingdb.AppendDataObject(&sdb, sightingdata, "tags/" + tag.Id + "/org_id", tag.OrgId)
		sightingdb.AppendDataObject(&sdb, sightingdata, "tags/" + tag.Id + "/user_id", tag.UserId)
		hide_tag := "false"
		if tag.HideTag {
			hide_tag = "true"
		}
		sightingdb.AppendDataObject(&sdb, sightingdata, "tags/" + tag.Id + "/hide_tag", hide_tag)
		sightingdb.AppendDataObject(&sdb, sightingdata, "tags/" + tag.Id + "/numerical_value", tag.Id)
		sightingdb.AppendDataObject(&sdb, sightingdata, "tags/" + tag.Id + "/count", strconv.Itoa(tag.Count))
		sightingdb.AppendDataObject(&sdb, sightingdata, "tags/" + tag.Id + "/attribute_count", strconv.Itoa(tag.AttributeCount))

		favourite := "false"
		if tag.Favourite {
			favourite = "true"
		}
		sightingdb.AppendDataObject(&sdb, sightingdata, "tags/" + tag.Id + "/favourite", favourite)

		// fmt.Println(tag.Name)
	}
	sightingdb.AppendDataObject(&sdb, sightingdata, "tags/max_id", strconv.Itoa(max_tag_id))
	sdb.Submit(sightingdata)

	context.Logger.Println("Finished Inserting Tags into SightingDB")

	//
	// EVENTS
	//
 	context.Logger.Println("Inserting Events into SightingDB")
	
	misp_events_url := context.Config.Section("DEFAULT").Key("misp_url").String() + "/events"
	context.Logger.Println("Pulling Events from ", misp_events_url)

	req, err = http.NewRequest("GET", misp_events_url, nil)
	if err != nil {
		context.Logger.Println("Error pulling Tags from ", misp_events_url, " :", err)		
	}
	req.Header.Set("Authorization", context.Config.Section("DEFAULT").Key("misp_key").String())
	req.Header.Set("Content-type", "application/json")
	req.Header.Set("Accept", "application/json")	
	res, _ = client.Do(req)
	data, err = ioutil.ReadAll(res.Body)
	res.Body.Close()
	if err != nil {
		context.Logger.Println("SyncIn: Error reading data:", err)
	}

	// fmt.Println("++++++++++")
	// fmt.Println(string(data))
	// fmt.Println("----------")
	var mispevents []mispobjects.Event
	err = json.Unmarshal(data, &mispevents)
	if err != nil {
		context.Logger.Println("SyncIn: Error Unmarshaling data:", err)
	}
	
	sightingdb.ClearDataObject(sightingdata)
	max_event_id := 0
	for i := 0; i < len(mispevents); i++ {
		event := mispevents[i]
		// fmt.Println("Event Info:", event.Info)
		event_id, _ := strconv.Atoi(event.Id)
		if event_id > max_event_id {
			max_event_id = event_id
		}
		sightingdb.AppendDataObject(&sdb, sightingdata, "events", event.Info)
		sightingdb.AppendDataObject(&sdb, sightingdata, "events/id", event.Id)
 		sightingdb.AppendDataObject(&sdb, sightingdata, "events/" + event.Id + "/info", event.Info)
 		sightingdb.AppendDataObject(&sdb, sightingdata, "events/" + event.Id + "/org_id", event.OrgId)
		sightingdb.AppendDataObject(&sdb, sightingdata, "events/" + event.Id + "/org_id", event.OrgId)
		sightingdb.AppendDataObject(&sdb, sightingdata, "events/" + event.Id + "/date", event.Date)
		sightingdb.AppendDataObject(&sdb, sightingdata, "events/" + event.Id + "/uuid", event.Uuid)
		published := "false"
		if event.Published {
			published = "true"
		}
		sightingdb.AppendDataObject(&sdb, sightingdata, "events/" + event.Id + "/published", published)
		sightingdb.AppendDataObject(&sdb, sightingdata, "events/" + event.Id + "/analysis", event.Analysis)
		sightingdb.AppendDataObject(&sdb, sightingdata, "events/" + event.Id + "/attribute_count", event.AttributeCount)
		sightingdb.AppendDataObject(&sdb, sightingdata, "events/" + event.Id + "/orgc_id", event.OrgcId)
		sightingdb.AppendDataObject(&sdb, sightingdata, "events/" + event.Id + "/timestamp", event.Timestamp)
		sightingdb.AppendDataObject(&sdb, sightingdata, "events/" + event.Id + "/distribution", event.Distribution)
		sightingdb.AppendDataObject(&sdb, sightingdata, "events/" + event.Id + "/sharing_group_id", event.SharingGroupId)
		proposal_email_lock := "false"
		if event.ProposalEmailLock {
			proposal_email_lock = "true"
		}
		sightingdb.AppendDataObject(&sdb, sightingdata, "events/" + event.Id + "/proposal_email_lock", proposal_email_lock)
		locked := "false"
		if event.Locked {
			locked = "true"
		}
		sightingdb.AppendDataObject(&sdb, sightingdata, "events/" + event.Id + "/locked", locked)
		sightingdb.AppendDataObject(&sdb, sightingdata, "events/" + event.Id + "/threat_level_id", event.ThreatLevelId)
		sightingdb.AppendDataObject(&sdb, sightingdata, "events/" + event.Id + "/publish_timestamp", event.PublishTimestamp)
		sightingdb.AppendDataObject(&sdb, sightingdata, "events/" + event.Id + "/sighting_timestamp", event.SightingTimestamp)
		disable_correlation := "false"
		if event.DisableCorrelation {
			disable_correlation = "true"
		}
		sightingdb.AppendDataObject(&sdb, sightingdata, "events/" + event.Id + "/disable_correlation", disable_correlation)
		sightingdb.AppendDataObject(&sdb, sightingdata, "events/" + event.Id + "/extends_uuid", event.ExtendsUuid)		
	}
	sightingdb.AppendDataObject(&sdb, sightingdata, "events/max_id", strconv.Itoa(max_event_id))
	sdb.Submit(sightingdata)
	
	context.Logger.Println("Finished Inserting Events into SightingDB")

	
	
}
