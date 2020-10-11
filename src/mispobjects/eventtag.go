package mispobjects

type EventTag struct {
	Id             string `json:"id"`
	Name           string `json:"name"`
	Colour         string `json:"colour"`
	Exportable     bool   `json:"exportable"`
	OrgId          string `json:"org_id"`
	UserId         string `json:"user_id"`
	HideTag        bool   `json:"hide_tag"`
	NumericalValue string `json:"numerical_value"`
	Local          bool   `json:"local,omitempty"`
	EventId        bool   `json:"event_id,omitempty"`
	TagId          string `json:"tag_id"`
	Tag            Tag    `json:"Tag"`
}
