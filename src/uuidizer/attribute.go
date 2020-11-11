package uuidizer

import "strings"
import "github.com/google/uuid"

import "../mispobjects"

func AttributeUuidize(event *mispobjects.Event, attribute *mispobjects.Attribute) {
	if strings.Compare(event.Uuid, "") == 0 {
		event_hash_str := event.Id + event.Info + event.Timestamp + event.OrgId
		event_uuid := uuid.NewSHA1(uuid.NameSpaceURL, []byte(string(event_hash_str)))
		event.Uuid = event_uuid.String()
	}

	if strings.Compare(attribute.Uuid, "") == 0 {
		attribute_hash_str := event.Uuid + attribute.Value + attribute.Timestamp
		attribute_uuid := uuid.NewSHA1(uuid.NameSpaceURL, []byte(string(attribute_hash_str)))
		attribute.Uuid = attribute_uuid.String()
	}	
}

