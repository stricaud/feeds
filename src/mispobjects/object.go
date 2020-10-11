package mispobjects

type Object struct {
	Id string `json:"id"`
	Name string `json:"name"`
	MetaCategory string `json:"meta-category"`
	Description string `json:"description"`
	TemplateUuid string `json:"template_uuid"`
	TemplateVersion string `json:"template_version"`
	EventId string `json:"event_id"`
	Uuid string `json:"uuid"`
	Timestamp string `json:"timestamp"`
	Distribution string `json:"distribution"`
	SharingGroupId string `json:"sharing_group_id"`
	Comment string `json:"comment"`
	Deleted string `json:"deleted"`
	First string `json:"first_seen"`
	LastSeen string `json:"last_seen"`
}
