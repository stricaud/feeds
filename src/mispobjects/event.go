package mispobjects

type Event struct {
	Id string `json:"id"`
	Info string `json:"info"`
	OrgId string `json:"org_id"`
	OrgcId string `json:"orgc_id"`
	Distribution string `json:"distribution"`
	SharingGroupId string `json:"sharing_group_id"`
	Tags []Tag `json:"Tag,omitempy"`
	PublishTimestamp string `json:"publish_timestamp,omitempy"`
	Timestamp string `json:"timestamp,omitempty"`
	Analysis string `json:"analysis"`
	Attributes []Attribute `json:"Attribute,omitempty"`
	ExtendsUuid string `json:"extends_uuid,omitempty"`
	Published bool `json:"published"`
	Date string `json:"date"`
	Orgc Org `json:"Orgc"` // Orgc = The Organization that Created the Event, not Org which is the one handling it.
	ThreatLevelId string `json:"threat_level_id"`
	Uuid string `json:"uuid"`
	Galaxy []Galaxy `json:"Galaxy,omitempty"`
	GalaxyClusters []GalaxyCluster `json:"GalaxyCluster,omitempty"`
	AttributeCount string `json:"attribute_count,omitempty"`
	ProposalEmailLock bool `json:"proposal_email_lock,omitempty"`
	Locked bool `json:"locked,omitempty"`
	SightingTimestamp string `json:"sighting_timestamp,omitempty"`
	DisableCorrelation bool `json:"disable_correlation,omitempty"`
	Org Org `json:"Org,omitempty"`
	EventTag []EventTag `json:"EventTag,omitempty"`
	UserId string `json:"user_id,omitempty"`
}

type EventForJson struct {
	Event Event `json:"Event"`
}

type EventArrayForJson struct {
	Event []Event
}
