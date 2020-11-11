package uuidizer

import "strings"

import "github.com/google/uuid"

import "../mispobjects"

func EventUuidize(event *mispobjects.Event) {
	if strings.Compare(event.Uuid, "") == 0 {
		event_hash_str := event.Id + event.Info + event.Timestamp + event.OrgId
		event_uuid := uuid.NewSHA1(uuid.NameSpaceURL, []byte(string(event_hash_str)))
		event.Uuid = event_uuid.String()
	}
}
