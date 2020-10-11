package mispobjects


type DevoMispAttribute struct {
	Attribute Attribute   `json:"Attribute"`
	Event     Event       `json:"Event"`
	Object    Object      `json:"Object"`
	EventTags []EventTag `json:"EventTags"`
}

