package mispobjects

type Attribute struct {
	Id                 string `json:"id,omitempty"`
	EventId            string `json:"event_id,omitempty"`
	ObjectId           string `json:"object_id,omitempty"`
	ObjectRelation     string `json:"object_relation"`
	Comment            string `json:"comment"`
	Category           string `json:"category"`
	Value1             string `json:"value1"`
	Value2             string `json:"value2"`
	Uuid               string `json:"uuid"`
	Timestamp          string `json:"timestamp"`
	ToIds              bool   `json:"to_ids"`
	Distribution       string `json:"distribution"`
	SharingGroupId     string `json:"sharing_group_id"`
	Value              string `json:"value"`
	DisableCorrelation bool   `json:"disable_correlation"`
	Deleted            bool   `json:"deleted"`
	FirstSeen          string `json:"first_seen"`
	LastSeen           string `json:"last_seen"`
	Type               string `json:"type"`
	Tags               []Tag  `json:"Tag,omitempty"`
}


